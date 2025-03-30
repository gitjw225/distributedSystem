import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;

public class RabbitMQConsumer {

  private static final String QUEUE_NAME = "lift_ride_queue1";
  private static final String EXCHANGE_NAME = "lift_ride_exchange";
  private static final String RABBITMQ_HOST = "172.31.24.166";
  private static final String ROUTING_KEY = "skier.liftRide";
  private static final String USERNAME = "guest";
  private static final String PASSWORD = "guest";
  private static final int THREAD_POOL_SIZE = 200;
  private final ThroughputCircuitBreaker breaker = new ThroughputCircuitBreaker(
      4000, 2000); // max 4000 rps, pause 2s


  private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
  private Connection connection;
  private Channel consumeChannel;

  public void startConsumer() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RABBITMQ_HOST);
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);

    connection = factory.newConnection();
    consumeChannel = connection.createChannel();
    consumeChannel.queueDeclare(QUEUE_NAME, true, false, false, null);
    consumeChannel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
    consumeChannel.basicQos(THREAD_POOL_SIZE);

    System.out.println(" [*] Waiting for messages...");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      executor.submit(() -> {
        try {
          String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
          System.out.println(" [x] Received: " + message);

          Map<String, Object> rideData = new ObjectMapper().readValue(message, Map.class);

          if (breaker.isOpen()) {
            System.out.println(" [!] Circuit open. Sleeping 2s.");
            Thread.sleep(2000);
            return;
          }

          DynamoDBClient.insertLiftRide(rideData);
          breaker.record();

          synchronized (consumeChannel) {
            consumeChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          }
          System.out.println(" [✓] Acked: " + delivery.getEnvelope().getDeliveryTag());

        } catch (Exception e) {
          System.err.println(" [!] Failed to process message.");
          e.printStackTrace();
          try {
            synchronized (consumeChannel) {
              consumeChannel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
          } catch (Exception nackEx) {
            nackEx.printStackTrace();
          }
        }
      });
    };

    consumeChannel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});
  }

  public void shutdown() throws Exception {
    executor.shutdown();
    if (consumeChannel != null) consumeChannel.close();
    if (connection != null) connection.close();
  }
}
