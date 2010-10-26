/**
 * 
 */
package nl.surfnet.coin.teams.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.surfnet.coin.mock.MockHandler;
import nl.surfnet.coin.mock.MockHtppServer;

import org.junit.Test;
import org.mortbay.jetty.Server;
import org.springframework.core.io.ClassPathResource;

/**
 * @author steinwelberg
 *
 */
public class DummyTeamsAPI {
  
  @Test
  public void mockTeamsAPI() {
    final ClassPathResource invitations = new ClassPathResource(
        "mocks/invitations.json");
    final ClassPathResource success = new ClassPathResource(
        "mocks/success.html");
    new MockHtppServer(8083) {
      protected MockHandler createHandler(Server server) {
        return new MockHandler(server) {
          public void handle(String target, HttpServletRequest request,
              HttpServletResponse response, int dispatch) throws IOException,
              ServletException {
            String requestURI = request.getRequestURI();
            if (requestURI.startsWith("/api")) {
              String req = request.getParameter("request");
              if (req.equals("invitations")) {
                setResponseResource(invitations);
              } else if (req.equals("invite")) {
                setResponseResource(success);
                // TODO insert correct request
              } else if (req.equals("asfsfssdfsdfsdfsd")) {
                setResponseResource(success);
              }
            } else {
              response.sendError(404);
            }
            super.handle(target, request, response, dispatch);
          }
        };
      }
    }.startServerSync();
  }

}
