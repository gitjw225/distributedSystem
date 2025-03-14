import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LiftRideStore {

  private static final Map<String, Integer> skierLiftRides = new ConcurrentHashMap<>();

  public static void addLiftRide(String message) {
    String skierID = extractSkierID(message);
    skierLiftRides.merge(skierID, 1, Integer::sum);
  }

  public static int getLiftRides(String skierID) {
    return skierLiftRides.getOrDefault(skierID, 0);
  }

  private static String extractSkierID(String message) {
    return message.contains("skierID") ? message.split("skierID\":\"")[1].split("\"")[0] : "unknown";
  }
}
