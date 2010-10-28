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
   * @param personId
   *          The id of the {@link person}
   * @param teamId
   *          the id of the {@link team}
   * @param title
   *          the title of the activity
   * @param body
   *          the body of the activity
   * 
   * @throws RequestException
   * @throws IOException
   */
  void addActivity(String personId, String teamId, String title, String body)
      throws RequestException, IOException;

}
