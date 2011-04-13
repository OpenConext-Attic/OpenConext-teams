/**
 * 
 */
package nl.surfnet.coin.teams.service;

/**
 * @author steinwelberg
 * 
 */
public interface ShindigActivityService {

  /**
   * Add the activity to Shindig
   * 
   * @param personId
   *          The id of the {@link org.opensocial.models.Person}
   * @param teamId
   *          the id of the {@link nl.surfnet.coin.teams.domain.Team}
   * @param title
   *          the title of the activity
   * @param body
   *          the body of the activity
   * 
   */
  void addActivity(String personId, String teamId, String title, String body);


}
