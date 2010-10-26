/**
 * 
 */
package nl.surfnet.coin.teams.service;

import java.io.IOException;

import org.opensocial.RequestException;

/**
 * @author steinwelberg
 * 
 */
public interface ShindigActivityService {

  /**
   * Add the activity to Shindig
   * 
   * @param title
   *          the title of the activity
   * @param body
   *          the body of the activity
   * 
   * @throws RequestException
    * @throws IOException
   */
  void addActivity(String person, String title, String body)
      throws RequestException, IOException;

}
