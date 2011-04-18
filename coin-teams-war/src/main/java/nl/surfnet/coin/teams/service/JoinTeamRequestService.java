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
   * Searches for pending {@link JoinTeamRequest}'s in the database
   *
   * @param team {@link Team}
   * @return List of JoinTeamRequest, can be empty, not null
   */
  List<JoinTeamRequest> findPendingRequests(Team team);

  /**
   * Searches for the pending {@link JoinTeamRequest}
   *
   * @param person {@link Person} who wants to join
   * @param team   the {@link Team} he wants to join
   * @return {@link JoinTeamRequest} if there is any,
   *         otherwise {@literal null}
   */
  JoinTeamRequest findPendingRequest(Person person, Team team);
}
