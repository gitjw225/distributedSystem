package cs6650.skierscontroller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

  @GetMapping
  public Map<String, Object> getStatistics() {
    List<Map<String, Object>> stats = new ArrayList<>();

    stats.add(Map.of(
        "URL", "/resorts",
        "operation", "GET",
        "mean", 22,
        "max", 198
    ));

    stats.add(Map.of(
        "URL", "/resorts",
        "operation", "POST",
        "mean", 12,
        "max", 89
    ));

    return Map.of("endpointStats", stats);
  }
}

