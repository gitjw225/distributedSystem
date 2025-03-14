package multithreaded.client.part1;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class Main {
  private static final int TOTAL_REQUESTS = 200000;
  private static final int INITIAL_THREADS = 32;
  private static final int INITIAL_REQUESTS_PER_THREAD = 1000;
  private static final int SUBSEQUENT_THREADS = 168;

  public static void main(String[] args) throws InterruptedException {
    long startTime = System.currentTimeMillis();
    // Initialize queue and counter
    BlockingQueue<SkierLiftRide> queue = new LinkedBlockingQueue<>();
    Counter counter = new Counter();

    // Initialize event producer to produce events
    Thread producerThread = new Thread(new EventProducer(queue, TOTAL_REQUESTS));
    producerThread.start();
    producerThread.join(); // wait for all events to be produced

    // Initialize thread pool
    ExecutorService executor = Executors.newCachedThreadPool();

    // Submit initial tasks and save future objects
    List<Future<?>> initialFutures = new ArrayList<>();
    for (int i = 0; i < INITIAL_THREADS; i++) {
      Future<?> future = executor.submit(new PostThread(queue, counter, INITIAL_REQUESTS_PER_THREAD));
      initialFutures.add(future);
    }

    // Initialize monitor thread
    Thread monitorThread = new Thread(() -> {
      while (true) {
        for (Future<?> future : initialFutures) {
          if (future.isDone()) {
            int remainingRequests = TOTAL_REQUESTS - (INITIAL_THREADS * INITIAL_REQUESTS_PER_THREAD);
            int baseRequestsPerThread = remainingRequests / SUBSEQUENT_THREADS;
            int extraRequests = remainingRequests % SUBSEQUENT_THREADS;

            // Submit subsequent requests
            for (int i = 0; i < SUBSEQUENT_THREADS; i++) {
              int requestsForThisThread = baseRequestsPerThread + (i < extraRequests ? 1 : 0);
              executor.submit(new PostThread(queue, counter, requestsForThisThread));
            }
            return; // Monitor thread exiting
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

    // Wait monitor thread to complete
    monitorThread.join();

    // Shutdown the thread pool to let all threads complete
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);

    // Output the result
    long totalTime = System.currentTimeMillis() - startTime;
    System.out.println("Number of subsequent threads used: " + SUBSEQUENT_THREADS);
    System.out.println("Successful requests: " + counter.getSuccess());
    System.out.println("Failed requests: " + counter.getFailures());
    System.out.println("Total time (s): " + totalTime / 1000.0);
    System.out.printf("Throughput (req/s): %.2f\n", TOTAL_REQUESTS / (totalTime / 1000.0));
  }
}
