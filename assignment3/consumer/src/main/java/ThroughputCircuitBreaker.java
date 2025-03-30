public class ThroughputCircuitBreaker {
  private final int maxRequestsPerSecond;
  private final long cooldownMillis;

  private long currentSecond;
  private int countThisSecond;
  private boolean open = false;
  private long lastOpenedTime = 0;

  public ThroughputCircuitBreaker(int maxRequestsPerSecond, long cooldownMillis) {
    this.maxRequestsPerSecond = maxRequestsPerSecond;
    this.cooldownMillis = cooldownMillis;
    this.currentSecond = System.currentTimeMillis() / 1000;
  }

  public synchronized boolean isOpen() {
    long now = System.currentTimeMillis();
    if (open) {
      if (now - lastOpenedTime > cooldownMillis) {
        open = false;
        countThisSecond = 0;
        currentSecond = now / 1000;
        System.out.println(" [✓] CircuitBreaker recovered.");
        return false;
      }
      return true;
    }
    return false;
  }

  public synchronized void record() {
    long now = System.currentTimeMillis();
    long thisSec = now / 1000;

    if (thisSec != currentSecond) {
      currentSecond = thisSec;
      countThisSecond = 0;
    }

    countThisSecond++;
    if (countThisSecond > maxRequestsPerSecond) {
      open = true;
      lastOpenedTime = now;
      System.out.println(" [!] CircuitBreaker opened: " + countThisSecond + " rps");
    }
  }
}
