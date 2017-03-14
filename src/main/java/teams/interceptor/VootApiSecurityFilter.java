package teams.interceptor;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.Base64;

public class VootApiSecurityFilter extends HandlerInterceptorAdapter {

  private String user;
  private String password;

  public VootApiSecurityFilter(String user, String password) {
    this.user = user;
    this.password = password;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String requestURI = request.getRequestURI();
    if (requestURI.contains(LoginInterceptor.API_VOOT_URL)) {
      String header = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (header == null || !header.startsWith("Basic ")) {
        throw new BadCredentialsException("No or incorrect Authorization header for " + requestURI);
      }
      byte[] base64Token = header.substring(6).getBytes(Charset.defaultCharset());
      byte[] decoded = Base64.getDecoder().decode(base64Token);
      String token = new String(decoded, Charset.defaultCharset());
      int delim = token.indexOf(":");
      if (delim == -1) {
        throw new BadCredentialsException("Invalid basic authentication token for " + requestURI);
      }
      if (!user.equals(token.substring(0, delim)) || !password.equals(token.substring(delim + 1))) {
        throw new BadCredentialsException("Invalid username / password for " + requestURI);
      }
    }
    return true;

  }
}
