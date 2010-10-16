/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.util;

/**
 * We will use a real LoginInterceptor, but that will be added by Stein;-) Temp for testing purposes
 *
 */
public class TempLoginInterceptor {

  private static final ThreadLocal<String> loggedInUser = new ThreadLocal<String>();

  
  
  /**
   * 
   */
  public TempLoginInterceptor() {
    super();
    loggedInUser.set("urn:collab:person:surfnet.nl:hansz");
  }



  /**
   * @return the loggedinuser
   */
  public static String getLoggedInUser() {
    return loggedInUser.get();
  }
  
}
