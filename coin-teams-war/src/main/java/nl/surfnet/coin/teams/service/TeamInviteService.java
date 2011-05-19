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
   * Searches for an {@link Invitation} by email address and team.
   * May check for expiration.
   *
   * @param email address to send invitation to
   * @param team  {@link Team}
   * @return {@link Invitation} or {@literal null}
   */
  Invitation findInvitation(String email, Team team);

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
   * Searches for pending {@link Invitation}'s by email address
   *
   * @param email {@link String}
   * @return List of Invitation's, can be empty
   */
  List<Invitation> findPendingInvitationsByEmail(String email);

  /**
   * Cleans up expired {@link Invitation}'s
   */
  void cleanupExpiredInvitations();
}
