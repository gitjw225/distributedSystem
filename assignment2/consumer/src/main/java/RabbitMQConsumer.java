import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class RabbitMQConsumer {

  private static final String QUEUE_NAME = "lift_ride_queue1";
  private static final String EXCHANGE_NAME = "lift_ride_exchange";
  private static final String RABBITMQ_HOST = "172.31.24.166"; // Change to RabbitMQ host
  private static final String ROUTING_KEY = "skier.liftRide";
  private static final String USERNAME = "guest"; // Change if needed
  private static final String PASSWORD = "guest"; // Change if needed
  private static final int THREAD_POOL_SIZE = 200; // Number of consumer threads

  private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
  private Connection connection;
  private Channel channel;

  public void startConsumer() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RABBITMQ_HOST);
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);

    connection = factory.newConnection();
    channel = connection.createChannel();
    channel.queueDeclare(QUEUE_NAME, true, false, false, null);
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
    System.out.println(" [*] Waiting for messages...");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      executor.submit(() -> {
        try {
          String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
          System.out.println(" [x] Received '" + message + "'");

          LiftRideStore.addLiftRide(message);

          synchronized (channel) {
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          }

        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    };

    while (true) {
      try {
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});
        break;
      } catch (Exception e) {
        System.err.println(" [!] Consumer failed, retrying...");
        Thread.sleep(5000);
      }
    }
  }

  public void shutdown() throws Exception {
    executor.shutdown();
    if (connection != null) {
      connection.close();
    }
  }
}
