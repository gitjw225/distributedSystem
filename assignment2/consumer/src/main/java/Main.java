public class Main {
  public static void main(String[] args) {
    RabbitMQConsumer consumer = new RabbitMQConsumer();
    try {
      consumer.startConsumer();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
