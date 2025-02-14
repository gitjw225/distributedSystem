package multithreaded.client.part1;

import io.swagger.client.model.LiftRide;

public class SkierLiftRide {
  private final LiftRide liftRide;
  private final int resortID;
  private final String seasonID;
  private final String dayID;
  private final int skierID;

  public SkierLiftRide(LiftRide liftRide, int resortID, String seasonID, String dayID, int skierID)
  {
    this.liftRide = liftRide;
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.skierID = skierID;
  }

  public LiftRide getLiftRide() {
    return liftRide;
  }

  public int getResortID() {
    return resortID;
  }

  public String getSeasonID() {
    return seasonID;
  }

  public String getDayID() {
    return dayID;
  }

  public int getSkierID() {
    return skierID;
  }
}
