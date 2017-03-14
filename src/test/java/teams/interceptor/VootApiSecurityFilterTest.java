package teams.interceptor;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;

import javax.servlet.http.HttpServletRequest;

import java.util.Base64;

import static org.junit.Assert.*;

public class VootApiSecurityFilterTest {

  private VootApiSecurityFilter subject = new VootApiSecurityFilter("user", "secret");

  @Test
  public void preHandleHappyFlow() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", LoginInterceptor.API_VOOT_URL);
    request.addHeader("Authorization", "Basic " + new String(Base64.getEncoder().encode("user:secret".getBytes())));
    boolean proceed = subject.preHandle(request, null, null);
    assertTrue(proceed);
  }

  @Test
  public void preHandleNoApiCall() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
    boolean proceed = subject.preHandle(request, null, null);
    assertTrue(proceed);
  }

  @Test(expected = BadCredentialsException.class)
  public void preHandleWrongPassword() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", LoginInterceptor.API_VOOT_URL);
    request.addHeader("Authorization", "Basic " + new String(Base64.getEncoder().encode("user:bogus".getBytes())));
    subject.preHandle(request, null, null);
  }

  @Test(expected = BadCredentialsException.class)
  public void preHandleNoHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", LoginInterceptor.API_VOOT_URL);
    request.addHeader("Authorization", "Bogus ");
    subject.preHandle(request, null, null);
  }

}
