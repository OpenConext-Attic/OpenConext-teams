package nl.surfnet.coin.teams.service;

import java.util.List;

import nl.surfnet.coin.shared.service.GenericService;
import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Team;

/**
 * Service to handle team {@link Invitation}'s
 */
public interface TeamInviteService extends GenericService<Invitation> {


  /**
   * Checks if there is already an invitation for the given email + team
   *
   * @param email address of the person to invite
   * @param team  {@link Team}
   * @return {@literal true} if an {@link Invitation} already exists
   */
  boolean alreadyInvited(String email, Team team);

  /**
   * Searches for an {@link Invitation} by its generated hash
   * (which is sent to the invitee). May check for expiration.
   *
   * @param invitationId String that was sent to the invitee
   * @return {@link Invitation} if found, otherwise {@literal null}
   */
  Invitation findInvitationByInviteId(String invitationId);

  /**
   * Searches for {@link Invitation}'s for a {@link Team}.
   *
   * @param team {@link Team}
   * @return List of Invitation's, can be empty.
   */
  List<Invitation> findInvitationsForTeam(Team team);

  /**
   * Cleans up expired {@link Invitation}'s
   */
  void cleanupExpiredInvitations();
}
