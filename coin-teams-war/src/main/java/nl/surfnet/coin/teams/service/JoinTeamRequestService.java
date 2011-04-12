package nl.surfnet.coin.teams.service;

import java.util.List;

import org.opensocial.models.Person;

import nl.surfnet.coin.shared.service.GenericService;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Team;

/**
 * Interface to handle {@link JoinTeamRequest} CRUD operations
 */
public interface JoinTeamRequestService extends GenericService<JoinTeamRequest> {

  /**
   * Checks if this Person has no pending requests to join the team
   *
   * @param person {@link org.opensocial.models.Person} who wants to join
   * @param team   the {@link Team} he wants to join
   * @return {@literal true} if there is no pending request
   */
  boolean isNewRequestForTeam(Person person, Team team);

  /**
   * Searches for pending {@link JoinTeamRequest}'s in the database
   *
   * @param team {@link Team}
   * @return List of JoinTeamRequest, can be empty, not null
   */
  List<JoinTeamRequest> findPendingRequests(Team team);
}
