package Servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SkiersServlet")
public class SkiersServlet extends HttpServlet {

  protected void doPost(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();
    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    //   POST:  /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
    if (!isSkierURLValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      //res.getWriter().write("Resort Servlet works!");
    }
  }
  protected void doGet(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing paramterers");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isSkierURLValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
//      if (!isGetVertical(urlParts)) {
//        Integer response = 200;
//        String json = new Gson().toJson(response);
//        res.getWriter().write(json);
//      } else {
//        Integer response = 200;
//        String json = new Gson().toJson(response);
//        res.getWriter().write(json);
//      }
    }
  }
  protected boolean isGetVertical(String[] urlPath) {
    if (urlPath.length == 4) return true;
    return false;
  }

    protected boolean isSkierURLValid(String[] urlPath) {
    // sample : [, 1, seasons, 2019, days, 1, skiers, 23]
      if (urlPath.length == 3) {
        return true;
      } else if (urlPath.length == 8) {
        return validateLiftRide(urlPath);
      }
      return true;
    }

  private boolean validateLiftRide(String[] urlPath) {
    String resortID = urlPath[1];
    String seasons = urlPath[2];
    String seasonID = urlPath[3];
    String days = urlPath[4];
    String dayID = urlPath[5];
    String skiers = urlPath[6];
    String skierID = urlPath[7];
    if (!validate32Int(skierID)) return false;
    if (!validateYear(seasonID)) return false;
    if (!validateDay(dayID)) return false;
    return true;
  }

  private Integer strToNum(String str) {
      Integer num = null;
      try {
        num = Integer.parseInt(str);
      } catch (NumberFormatException nfe) {
        System.out.println("Wrong Format");
      } catch (NullPointerException npe) {
        System.out.println("Null Pointer");
      }
      return num;
    }

  private boolean validate32Int(String str) {
    if (strToNum(str) == null) return false;
    return true;
  }
  private boolean validateYear(String str) {
    Integer year = strToNum(str);
    if (year == null) return false;
    // year validation
    if (year.intValue() < 1500 || year.intValue() > 3000) return false;
    return true;
  }
  private boolean validateDay(String str) {
    Integer day = strToNum(str);
    if (day == null) return false;
    // year validation
    if (day.intValue() < 1 || day.intValue() > 366) return false;
    return true;
  }
  }
