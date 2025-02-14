package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "SkiersServlet", urlPatterns = {"/skiers/*"})
public class SkiersServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws
      IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    String[] pathParts = req.getPathInfo().split("/");
    if (pathParts.length != 8 || !isInteger(pathParts[1]) || !"seasons".equals(pathParts[2])
        || pathParts[3] == null || !"days".equals(pathParts[4]) || !isValidDay(pathParts[5])
        || !"skiers".equals(pathParts[6]) || !isInteger(pathParts[7]))
    {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"error\":\"Invalid URL format. ");
      return;
    }

    try {
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> requestBody = mapper.readValue(req.getReader(), Map.class);

      if (!isValidLiftRideRequest(requestBody)) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write("{\"error\":\"Invalid request body.\"}");
        return;
      }

      resp.setStatus(HttpServletResponse.SC_CREATED);
      Map<String, String> response = new HashMap<>();
      response.put("message", "Write successful");
      resp.getWriter().write(mapper.writeValueAsString(response));
    } catch (Exception e) {
      e.printStackTrace();
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"error\":\"Invalid parameters.\"}");
    }
  }

  private boolean isValidLiftRideRequest(Map<String, Object> requestBody) {
    try {
      if (!requestBody.containsKey("time") || !requestBody.containsKey("liftID")) {
        return false;
      }
      Object timeObj = requestBody.get("time");
      Object liftIDObj = requestBody.get("liftID");

      return timeObj instanceof Integer && liftIDObj instanceof Integer;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isValidDay(String dayID) {
    try {
      int day = Integer.parseInt(dayID);
      return day > 0 && day <= 366;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    String pathInfo = req.getPathInfo();
    String[] pathParts = pathInfo.split("/");

    try {
      if (pathParts.length == 8 && isInteger(pathParts[1]) && "seasons".equals(pathParts[2]) &&
          pathParts[3] != null && "days".equals(pathParts[4]) && isValidDay(pathParts[5]) &&
          "skiers".equals(pathParts[6]) && isInteger(pathParts[7])) {
        handleGetSkiDayVertical(resp);
      } else if (pathParts.length == 3 && isInteger(pathParts[1]) &&
          "vertical".equals(pathParts[2])) {
        handleGetSkierResortTotals(req, resp);
      } else {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write("{\"error\":\"Invalid URL format.\"}");
      }
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"error\":\"Invalid parameters.\"}");
    }
  }

  private void handleGetSkiDayVertical(HttpServletResponse resp)
      throws IOException {
      int totalVertical = 34507;

      ObjectMapper mapper = new ObjectMapper();

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType("application/json");
      resp.getWriter().write(mapper.writeValueAsString(totalVertical));
  }

  private void handleGetSkierResortTotals(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String resort = req.getParameter("resort"); // Query parameter (required)
    String[] seasons = req.getParameterValues("season"); // Extract multiple values

    if (resort == null || resort.isEmpty()) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"error\":\"Missing resort query parameter.\"}");
      return;
    }

    Map<String, Object> response = new LinkedHashMap<>();
    List<Map<String, Object>> resorts = new ArrayList<>();

    if (seasons == null || seasons.length == 0) {
      // return predefined seasons
      Map<String, Object> resort1 = new LinkedHashMap<>();
      resort1.put("seasonID", "2017");
      resort1.put("totalVert", 1234566);

      Map<String, Object> resort2 = new LinkedHashMap<>();
      resort2.put("seasonID", "2018");
      resort2.put("totalVert", 787888);

      resorts.add(resort1);
      resorts.add(resort2);
    } else {
      // return all requested seasons with a fixed vertical value
      for (String season : seasons) {
        Map<String, Object> currResort = new LinkedHashMap<>();
        currResort.put("seasonID", season);
        currResort.put("totalVert", 12345);
        resorts.add(currResort);
      }
    }

    response.put("resorts", resorts);

    ObjectMapper mapper = new ObjectMapper();
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("application/json");
    resp.getWriter().write(mapper.writeValueAsString(response));
  }

  private boolean isInteger(String value) {return value.matches("-?\\d+");}
}
