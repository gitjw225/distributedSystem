package cs6650.skierscontroller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/resorts")
public class ResortController {

  @GetMapping
  public Map<String, Object> getResorts() {
    List<Map<String, Object>> resorts = new ArrayList<>();
    resorts.add(Map.of("resortName", "Whistler", "resortID", 1));
    resorts.add(Map.of("resortName", "Vail", "resortID", 2));

    return Map.of("resorts", resorts);
  }

  @GetMapping("/{resortID}/seasons")
  public Map<String, Object> getResortSeasons(@PathVariable String resortID) {
    validateInteger(resortID);
    return Map.of("seasons", List.of("2018", "2019"));
  }

  @GetMapping("/{resortID}/seasons/{seasonID}/day/{dayID}/skiers")
  public Map<String, Object> getResortSkiersDay(@PathVariable String resortID,
      @PathVariable String seasonID,
      @PathVariable String dayID) {
    validateInteger(resortID, seasonID, dayID);
    return Map.of("time", "Mission Ridge", "numSkiers", 78999);
  }

  @PostMapping("/{resortID}/seasons")
  public Map<String, String> addSeason(@PathVariable String resortID, @RequestBody Map<String, String> body) {
    validateInteger(resortID);

    if (!body.containsKey("year") || !body.get("year").matches("\\d{4}")) {
      throw new IllegalArgumentException("Invalid year format.");
    }

    return Map.of("message", "New season created");
  }

  private void validateInteger(String... values) {
    for (String value : values) {
      if (!value.matches("-?\\d+")) {
        throw new IllegalArgumentException("Invalid parameter: " + value + " is not a valid integer.");
      }
    }
  }
}

