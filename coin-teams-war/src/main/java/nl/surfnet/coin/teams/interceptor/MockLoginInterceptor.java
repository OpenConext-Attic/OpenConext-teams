package nl.surfnet.coin.teams.interceptor;

import javax.servlet.http.HttpServletRequest;

/**
 * Like the LoginInterceptor but gets the user id from the environment instead
 * of Shibboleth.
 */
public class MockLoginInterceptor extends LoginInterceptor {

 
  @Override
  protected String getRemoteUser(HttpServletRequest request) {
    return getTeamEnvironment().getMockLogin();
  }

 
}
