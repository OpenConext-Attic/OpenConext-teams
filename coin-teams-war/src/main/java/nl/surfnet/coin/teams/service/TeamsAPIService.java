package nl.surfnet.coin.teams.service;

import java.io.IOException;
import java.util.List;

import nl.surfnet.coin.teams.domain.Invitation;

/**
 * Main interface for dealing with the SURFteams API
 *
 */
@Deprecated
public interface TeamsAPIService  {

  /**
   * Get the {@link List} of {@link Invitation} instances from SURFteams
   *
   * @param teamId for which to retrieve the invitations
   *
   * @return {@link List} of {@link Invitation} instances, an empty {@link List}
   *         if there are no invitations for a team
   * @throws java.io.IOException
   * @throws IllegalStateException
   */
  List<Invitation> getInvitations(String teamId) throws IllegalStateException,
      IOException;

}
