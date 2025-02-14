package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "StatisticsServlet", urlPatterns = {"/statistics"})
public class StatisticsServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
      IOException {
    Map<String, Object> response = new LinkedHashMap<>();
    List<Map<String, Object>> stats = new ArrayList<>();

    Map<String, Object> stat1 = new LinkedHashMap<>();
    stat1.put("URL", "/resorts");
    stat1.put("operation", "GET");
    stat1.put("mean", 22);
    stat1.put("max", 198);

    Map<String, Object> stat2 = new LinkedHashMap<>();
    stat2.put("URL", "/resorts");
    stat2.put("operation", "POST");
    stat2.put("mean", 12);
    stat2.put("max", 89);

    stats.add(stat1);
    stats.add(stat2);

    response.put("endpointStats", stats);

    ObjectMapper mapper = new ObjectMapper();
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("application/json");
    resp.getWriter().write(mapper.writeValueAsString(response));
  }
}
