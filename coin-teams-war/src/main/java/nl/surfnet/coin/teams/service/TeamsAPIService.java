package nl.surfnet.coin.teams.service;

import java.io.IOException;
import java.util.List;

import nl.surfnet.coin.teams.domain.Invitation;

/**
 * Main interface for dealing with the SURFteams API
 *
 */
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

  /**
   * Sends the invitations for a {@link nl.surfnet.coin.teams.domain.Team} to SURFteams
   *
   * @param emails
   * @param teamId for which to send the invitations
   * @param message
   *          The message to be sent to invitees in the email body
   * @param subject
   *          The subject to be sent in the invitees email subject
   * 
   * @return {@link boolean} true if the invitations are sent successfully,
   *         false if something went wrong.
   * @throws java.io.IOException
   * @throws IllegalStateException
   */
  boolean sendInvitations(String emails, String teamId,
                          String message, String subject) throws IllegalStateException, IOException;

}
