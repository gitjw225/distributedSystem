package cs6650.skierscontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@RestController
@RequestMapping("/skiers")
public class SkierController {
  private static final String EXCHANGE_NAME = "lift_ride_exchange";
  private static final String ROUTING_KEY = "skier.liftRide";
  private static final String RABBITMQ_HOST = "172.31.24.166"; // Change to RabbitMQ host
  private static final String USERNAME = "guest"; // Change if needed
  private static final String PASSWORD = "guest"; // Change if needed

  private final ObjectMapper objectMapper = new ObjectMapper();
  private Connection connection;
  private BlockingQueue<Channel> channelPool;

  @PostConstruct
  public void init() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RABBITMQ_HOST);  // RabbitMQ 服务器 IP
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);

    this.connection = factory.newConnection();

    this.channelPool = new LinkedBlockingQueue<>(5);
    for (int i = 0; i < 5; i++) {
      this.channelPool.offer(connection.createChannel());
    }
  }

  @PostMapping("/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}")
  public Map<String, String> recordLiftRide(
      @PathVariable String resortID,
      @PathVariable String seasonID,
      @PathVariable String dayID,
      @PathVariable String skierID,
      @RequestBody Map<String, Object> requestBody) throws Exception {

    validateInteger(resortID, seasonID, skierID);
    validateDay(dayID);

    if (!requestBody.containsKey("time") || !requestBody.containsKey("liftID")) {
      throw new IllegalArgumentException("Invalid request body.");
    }

    requestBody.put("resortID", resortID);
    requestBody.put("seasonID", seasonID);
    requestBody.put("dayID", dayID);
    requestBody.put("skierID", skierID);

    String jsonMessage = objectMapper.writeValueAsString(requestBody);

    Channel channel = channelPool.take();

    try {
      channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, jsonMessage.getBytes());
    } finally {
      channelPool.offer(channel);
    }
    return Map.of("message", "Request accepted and queued for processing");
  }

  @PreDestroy
  public void cleanup() throws IOException, TimeoutException {
    for (Channel channel : channelPool) {
      channel.close();
    }
    connection.close();
  }

  @GetMapping("/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}")
  public Map<String, Object> getSkiDayVertical(
      @PathVariable String resortID,
      @PathVariable String seasonID,
      @PathVariable String dayID,
      @PathVariable String skierID) {

    validateInteger(resortID, seasonID, skierID);
    validateDay(dayID);

    return Map.of("totalVertical", 34507);
  }

  @GetMapping("/{skierID}/vertical")
  public Map<String, Object> getSkierResortTotals(
      @PathVariable String skierID,
      @RequestParam(required = false) String resort,
      @RequestParam(required = false) List<String> season) {

    validateInteger(skierID);

    if (resort == null || resort.isEmpty()) {
      throw new IllegalArgumentException("Missing resort query parameter.");
    }

    List<Map<String, Object>> resorts = new ArrayList<>();
    if (season == null || season.isEmpty()) {
      resorts.add(Map.of("seasonID", "2017", "totalVert", 1234566));
      resorts.add(Map.of("seasonID", "2018", "totalVert", 787888));
    } else {
      for (String s : season) {
        resorts.add(Map.of("seasonID", s, "totalVert", 12345));
      }
    }

    return Map.of("resorts", resorts);
  }

  private void validateInteger(String... values) {
    for (String value : values) {
      if (!value.matches("-?\\d+")) {
        throw new IllegalArgumentException("Invalid parameter: " + value + " is not a valid integer.");
      }
    }
  }

  private void validateDay(String dayID) {
    try {
      int day = Integer.parseInt(dayID);
      if (day < 1 || day > 366) {
        throw new IllegalArgumentException("Invalid day: " + dayID + " must be between 1 and 366.");
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid day: " + dayID + " is not a valid integer.");
    }
  }
}

