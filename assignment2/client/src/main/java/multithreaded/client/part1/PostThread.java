package multithreaded.client.part1;

import io.swagger.client.api.SkiersApi;
import java.util.concurrent.BlockingQueue;

public class PostThread implements Runnable {
  private final BlockingQueue<SkierLiftRide> queue;
  private final Counter counter;
  private final int maxRequests;
  private final SkiersApi api;

  public PostThread(BlockingQueue<SkierLiftRide> queue, Counter counter, int maxRequests) {
    this.queue = queue;
    this.counter = counter;
    this.maxRequests = maxRequests;
    this.api = new SkiersApi();
  }

  @Override
  public void run() {
    int requestsSent = 0;
    while (requestsSent < maxRequests && !Thread.currentThread().isInterrupted()) {
      try {
        SkierLiftRide event = queue.poll();
        if (event == null) break;

        sendWithRetry(event);
        requestsSent++;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private void sendWithRetry(SkierLiftRide event) throws InterruptedException {
    int retries = 0;
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
        return;
      } catch (Exception e) {
        retries++;
        if (retries > 5) {
          counter.incrementFailure();
          return;
        }
        Thread.sleep(1000);
      }
    }
  }
}