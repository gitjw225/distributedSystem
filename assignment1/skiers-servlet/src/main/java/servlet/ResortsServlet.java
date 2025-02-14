package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.LinkedHashMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ResortsServlet", urlPatterns = {"/resorts/*"})
public class ResortsServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    String pathInfo = req.getPathInfo();
    String[] pathParts = pathInfo == null ? new String[]{} : pathInfo.split("/");

    try {
      if (pathParts.length == 0) {
        // Handle /resorts
        handleGetResorts(resp);
      } else if (pathParts.length == 7 && isInteger(pathParts[1]) &&
          "seasons".equals(pathParts[2]) && isInteger(pathParts[3]) && "day".equals(pathParts[4]) &&
          isInteger(pathParts[5]) && "skiers".equals(pathParts[6])) {
        // Handle /resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
        handleGetResortSkiersDay(resp);
      } else if (pathParts.length == 3 && isInteger(pathParts[1]) &&
          "seasons".equals(pathParts[2])) {
        // Handle /resorts/{resortID}/seasons
        handleGetResortSeasons(resp);
      } else {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write("{\"error\":\"Invalid URL format.\"}");
      }
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"error\":\"Invalid parameters.\"}");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    String pathInfo = req.getPathInfo();
    String[] pathParts = pathInfo == null ? new String[]{} : pathInfo.split("/");

    if (pathParts.length == 3 && isInteger(pathParts[1]) && "seasons".equals(pathParts[2])) {
      handleAddSeason(req, resp);
    } else {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"error\":\"Invalid URL format.\"}");
    }
  }

  private void handleGetResorts(HttpServletResponse resp) throws IOException {
    // Create the response object
    Map<String, Object> response = new LinkedHashMap<>();

    // Create the resorts list with proper structure
    List<Map<String, Object>> resorts = new ArrayList<>();

    // Add resorts with expected keys ("resortName" and "resortID")
    Map<String, Object> resort1 = new LinkedHashMap<>();
    resort1.put("resortName", "Whistler");
    resort1.put("resortID", 1);

    Map<String, Object> resort2 = new LinkedHashMap<>();
    resort2.put("resortName", "Vail");
    resort2.put("resortID", 2);

    resorts.add(resort1);
    resorts.add(resort2);

    // Put the list into the response object
    response.put("resorts", resorts);

    // Convert to JSON and send the response
    ObjectMapper mapper = new ObjectMapper();
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("application/json");
    resp.getWriter().write(mapper.writeValueAsString(response));
  }

  private void handleGetResortSkiersDay(HttpServletResponse resp) throws IOException {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("time", "Mission Ridge");
    response.put("numSkiers", 78999);

    ObjectMapper mapper = new ObjectMapper();

    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("application/json");
    resp.getWriter().write(mapper.writeValueAsString(response)); // Convert list to JSON
  }

  private void handleGetResortSeasons(HttpServletResponse resp) throws IOException {
    Map<String, Object> response = new HashMap<>();
    response.put("seasons", Arrays.asList("2018", "2019"));

    ObjectMapper mapper = new ObjectMapper();

    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("application/json");
    resp.getWriter().write(mapper.writeValueAsString(response));
  }

  private void handleAddSeason(HttpServletRequest req, HttpServletResponse resp) throws IOException
  {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Map<String, String> requestBody = mapper.readValue(req.getReader(), Map.class);

      if (!requestBody.containsKey("year") || !isValidYear(requestBody.get("year"))) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write("{\"error\":\"Invalid year format.\"}");
        return;
      }

      // Mock response
      resp.setStatus(HttpServletResponse.SC_CREATED);
      Map<String, String> response = new HashMap<>();
      response.put("message", "new season created");
      resp.getWriter().write(mapper.writeValueAsString(response));
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"error\":\"Invalid parameters.\"}");
    }
  }

  private boolean isInteger(String value) {return value.matches("-?\\d+");}

  private boolean isValidYear(String year) {
    return year.matches("\\d{4}");
  }
}
