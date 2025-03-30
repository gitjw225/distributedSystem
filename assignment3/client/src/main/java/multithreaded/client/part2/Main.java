package multithreaded.client.part2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import com.google.common.util.concurrent.RateLimiter;

public class Main {
  private static final int TOTAL_REQUESTS = 200000;
  private static final int INITIAL_THREADS = 32;
  private static final int INITIAL_REQUESTS_PER_THREAD = 1000;
  private static final int SUBSEQUENT_THREADS = 300;

  public static void main(String[] args) throws InterruptedException, IOException {
    long startTime = System.currentTimeMillis();
    // Initialize queue and counter
    BlockingQueue<SkierLiftRide> queue = new LinkedBlockingQueue<>();
    Counter counter = new Counter();
    RateLimiter limiter = RateLimiter.create(50000);

    // Store the response code of the requests
    List<RequestRecord> requestRecords = Collections.synchronizedList(new ArrayList<>());

    Thread producerThread = new Thread(new EventProducer(queue, TOTAL_REQUESTS));
    producerThread.start();
    producerThread.join();

    ExecutorService executor = Executors.newCachedThreadPool();

    List<Future<?>> initialFutures = new ArrayList<>();
    for (int i = 0; i < INITIAL_THREADS; i++) {
      Future<?> future = executor.submit(new PostThread(queue, counter, INITIAL_REQUESTS_PER_THREAD,
          requestRecords, limiter));
      initialFutures.add(future);
    }

    Thread monitorThread = new Thread(() -> {
      while (true) {
        for (Future<?> future : initialFutures) {
          if (future.isDone()) {
            int remainingRequests = TOTAL_REQUESTS - (INITIAL_THREADS * INITIAL_REQUESTS_PER_THREAD);
            int baseRequestsPerThread = remainingRequests / SUBSEQUENT_THREADS;
            int extraRequests = remainingRequests % SUBSEQUENT_THREADS;

            for (int i = 0; i < SUBSEQUENT_THREADS; i++) {
              int requestsForThisThread = baseRequestsPerThread + (i < extraRequests ? 1 : 0);
              executor.submit(new PostThread(queue, counter, requestsForThisThread, requestRecords,
                  limiter));
            }
            return;
          }
        }
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    });
    monitorThread.start();

    monitorThread.join();

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);

    long totalTime = System.currentTimeMillis() - startTime;

    writeRecordsToCSV(requestRecords);

    calculateAndPrintMetrics(requestRecords, totalTime, TOTAL_REQUESTS);

    System.out.println("Successful requests: " + counter.getSuccess());
    System.out.println("Failed requests: " + counter.getFailures());
    System.out.println("Total time (s): " + totalTime / 1000.0);
  }

  private static void writeRecordsToCSV(List<RequestRecord> requestRecords) throws IOException {
    try (FileWriter writer = new FileWriter("request_records.csv")) {
      writer.write("StartTime,RequestType,Latency,ResponseCode\n");
      for (RequestRecord record : requestRecords) {
        writer.write(record.toCSV() + "\n");
      }
    }
  }

  private static void calculateAndPrintMetrics(List<RequestRecord> requestRecords, long totalTime,
      int totalRequests) {

    if (requestRecords.isEmpty()) {
      System.out.println("No requests were recorded. Skipping metrics.");
      return;
    }

    List<Long> latencies = requestRecords.stream()
        .map(RequestRecord::getLatency)
        .sorted()
        .collect(Collectors.toList());

    double meanLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
    double medianLatency = latencies.get(latencies.size() / 2);
    double p99Latency = latencies.get((int) (latencies.size() * 0.99));
    long minLatency = latencies.get(0);
    long maxLatency = latencies.get(latencies.size() - 1);

    System.out.println("Number of subsequent threads used: " + SUBSEQUENT_THREADS);
    System.out.printf("Mean response time (ms): %.2f\n", meanLatency);
    System.out.printf("Median response time (ms): %.2f\n", medianLatency);
    System.out.printf("Throughput (req/s): %.2f\n", totalRequests / (totalTime / 1000.0));
    System.out.printf("P99 response time (ms): %.2f\n", p99Latency);
    System.out.printf("Min response time (ms): %d\n", minLatency);
    System.out.printf("Max response time (ms): %d\n", maxLatency);
  }

}
