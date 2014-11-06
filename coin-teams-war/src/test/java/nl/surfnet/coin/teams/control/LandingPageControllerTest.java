package nl.surfnet.coin.teams.control;

import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.*;

public class LandingPageControllerTest {

  private LandingPageController controller;
  private MockHttpServletResponse response;

  @Before
  public void setUp() throws Exception {
    controller = new LandingPageController();
    response = new MockHttpServletResponse();
  }

  @Test
  public void testStoreCookie() throws Exception {
    controller.storeCookie(response);

    assertEquals(Integer.MAX_VALUE, response.getCookie(LoginInterceptor.TEAMS_COOKIE).getMaxAge());
  }
}
