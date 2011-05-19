/**
 * 
 */
package nl.surfnet.coin.teams.util;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.teams.interceptor.LoginInterceptor;

/**
 * @author steinwelberg
 * 
 */
public final class PermissionUtil {

  private PermissionUtil() {
    
  }

  public static boolean isGuest(HttpServletRequest request) {

    // Check if user is guest
    String userStatus = (String) request
        .getSession().getAttribute(LoginInterceptor.USER_STATUS_SESSION_KEY);
    return (userStatus == null || "guest".equals(userStatus));
  }

}
