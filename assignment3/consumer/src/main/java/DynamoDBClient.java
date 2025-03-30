import java.util.Map;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

public class DynamoDBClient {
  private static final String TABLE_NAME = "LiftRides";
  private static final DynamoDbClient client = DynamoDbClient.builder()
      .region(Region.US_WEST_2)
      .credentialsProvider(DefaultCredentialsProvider.create())
      .build();

  public static void insertLiftRide(Map<String, Object> rideData) {
    try {
      System.out.println("[DynamoDB] Inserting ride: " + rideData);

      String skierID = String.valueOf(rideData.get("skierID"));
      String resortID = String.valueOf(rideData.get("resortID"));
      String day = String.valueOf(rideData.get("dayID"));
      int time = Integer.parseInt(String.valueOf(rideData.get("time")));
      int liftID = Integer.parseInt(String.valueOf(rideData.get("liftID")));
      int vertical = liftID * 10;

      PutItemRequest request = PutItemRequest.builder()
          .tableName(TABLE_NAME)
          .item(Map.of(
              "skierID", AttributeValue.fromS(skierID),
              "skiDay_resort", AttributeValue.fromS(day + "#" + resortID),
              "liftID", AttributeValue.fromN(String.valueOf(liftID)),
              "time", AttributeValue.fromN(String.valueOf(time)),
              "resortID", AttributeValue.fromS(resortID),
              "vertical", AttributeValue.fromN(String.valueOf(vertical))
          ))
          .build();

      client.putItem(request);
      System.out.println("[DynamoDB] Insert succeeded");

    } catch (Exception e) {
      System.err.println(" [!] DynamoDB insert failed: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
