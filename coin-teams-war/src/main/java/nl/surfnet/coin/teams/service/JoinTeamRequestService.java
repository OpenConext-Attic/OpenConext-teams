package nl.surfnet.coin.teams.service;

import org.opensocial.models.Person;

import nl.surfnet.coin.shared.service.GenericService;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Team;

/**
 * Created by IntelliJ IDEA.
 * User: jashaj
 * Date: 11-04-11
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
public interface JoinTeamRequestService  extends GenericService<JoinTeamRequest> {

  /**
   * Checks if this Person has no pending requests to join the team
   *
   * @param person {@link org.opensocial.models.Person} who wants to join
   * @param team the {@link nl.surfnet.coin.teams.domain.Team} he wants to join
   * @return {@literal true} if there is no pending request
   */
  boolean isNewRequestForTeam(Person person, Team team);

}
