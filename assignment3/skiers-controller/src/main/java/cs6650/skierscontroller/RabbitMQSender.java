package cs6650.skierscontroller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RabbitMQSender {

  private final RabbitTemplate rabbitTemplate;

  public RabbitMQSender(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void send(Map<String, Object> message) {
    rabbitTemplate.convertAndSend("lift_ride_exchange", "skier.liftRide", message);
    System.out.println(" [x] Sent '" + message + "'");
  }
}