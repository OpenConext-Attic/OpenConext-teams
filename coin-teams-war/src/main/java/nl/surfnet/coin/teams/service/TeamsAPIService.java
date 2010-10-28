package nl.surfnet.coin.teams.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.springframework.stereotype.Component;

import nl.surfnet.coin.teams.domain.Invitation;

/**
 * Main interface for dealing with the SURFteams API
 * 
 */
public interface TeamsAPIService {

  /**
   * Get the {@link list} of {@link Invitation} instances from SURFteams
   * 
   * @param The
   *          teamId for which to retrieve the invitations
   * 
   * @return {@link list} of {@link Invitation} instances, an empty {@link list}
   *         if there are no invitations for a team
   */
  List<Invitation> getInvitations(String teamId) throws IllegalStateException,
      ClientProtocolException, IOException;

  /**
   * Sent the invitations for a {@link team} to SURFteams
   * 
   * @param {@link list} of {@link Invitation} instances
   * @param teamId
   *          for which to send the invitations
   * @param message
   *          The message to be sent to invitees in the email body
   * @param subject
   *          The subject to be sent in the invitees email subject
   * 
   * @return {@link boolean} true if the invitations are sent successfully,
   *         false if something went wrong.
   */
  boolean sentInvitations(String emails, String teamId,
      String message, String subject) throws IllegalStateException, ClientProtocolException, IOException;

  /**
   * Request Membership for a {@link team}
   * 
   * @param teamId
   * @param personId
   * @param message
   * @param subject
   * 
   * @return {@link boolean} true if the invitations are sent successfully,
   *         false if something went wrong.
   */
  boolean requestMembership(String teamId, String personId, String message, String subject) throws ClientProtocolException, IOException;

}
