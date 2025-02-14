package multithreaded.client.part1;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
  private final AtomicInteger success = new AtomicInteger();
  private final AtomicInteger failures = new AtomicInteger();

  public void incrementSuccess() { success.incrementAndGet(); }
  public void incrementFailure() { failures.incrementAndGet(); }
  public int getSuccess() { return success.get(); }
  public int getFailures() { return failures.get(); }
}