package multithreaded.client.part2;

import io.swagger.client.api.SkiersApi;
import java.util.concurrent.BlockingQueue;
import java.util.List;
import com.google.common.util.concurrent.RateLimiter;

public class PostThread implements Runnable {
  private final BlockingQueue<SkierLiftRide> queue;
  private final Counter counter;
  private final int maxRequests;
  private final List<RequestRecord> requestRecords;
  private final SkiersApi api;
  private final RateLimiter limiter;

  public PostThread(BlockingQueue<SkierLiftRide> queue, Counter counter, int maxRequests,
      List<RequestRecord> requestRecords, RateLimiter limiter) {
    this.queue = queue;
    this.counter = counter;
    this.maxRequests = maxRequests;
    this.requestRecords = requestRecords;
    this.api = new SkiersApi();
    this.limiter = limiter;
  }

  @Override
  public void run() {
    int requestsSent = 0;
    while (requestsSent < maxRequests && !Thread.currentThread().isInterrupted()) {
      try {
        limiter.acquire();
        SkierLiftRide event = queue.poll();
        if (event == null) break;

        long startTime = System.currentTimeMillis();
        int responseCode = sendWithRetry(event);
        long endTime = System.currentTimeMillis();

        requestRecords.add(new RequestRecord(startTime, "POST",
            endTime - startTime, responseCode));

        requestsSent++;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private int sendWithRetry(SkierLiftRide event) throws InterruptedException {
    int retries = 0;
    long backoff = 200; // start with 200ms

    while (true) {
      try {
        api.writeNewLiftRide(
            event.getLiftRide(),
            event.getResortID(),
            event.getSeasonID(),
            event.getDayID(),
            event.getSkierID()
        );
        counter.incrementSuccess();
        return 201;
      } catch (Exception e) {
        retries++;
        if (retries > 5) {
          counter.incrementFailure();
          return 500;
        }

        // Exponential backoff with jitter
        long sleepTime = backoff + (long)(Math.random() * 100); // add jitter
        System.out.println(" [!] Retry " + retries + ", backing off for " + sleepTime + " ms.");
        Thread.sleep(sleepTime);
        backoff *= 2;
      }
    }
  }

}
