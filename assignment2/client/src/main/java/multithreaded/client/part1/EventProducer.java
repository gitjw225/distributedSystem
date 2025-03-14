package multithreaded.client.part1;

import io.swagger.client.model.LiftRide;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class EventProducer implements Runnable {
  private final BlockingQueue<SkierLiftRide> queue;
  private final int totalRequests;
  private final Random random = new Random();

  public EventProducer(BlockingQueue<SkierLiftRide> queue, int totalRequests) {
    this.queue = queue;
    this.totalRequests = totalRequests;
  }

  @Override
  public void run() {
    for (int i = 0; i < totalRequests; i++) {
      LiftRide ride = new LiftRide().liftID(random.nextInt(40) + 1).
          time(random.nextInt(360) + 1);
      SkierLiftRide event = new SkierLiftRide(
          ride,
          random.nextInt(10) + 1,      // resortID (1-10)
          "2025",                       // seasonID
          "1",                          // dayID
          random.nextInt(100_000) + 1   // skierID (1-100000)
      );
      try {
        queue.put(event);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}