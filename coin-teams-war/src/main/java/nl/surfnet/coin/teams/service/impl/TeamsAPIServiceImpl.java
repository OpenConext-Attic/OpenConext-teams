/**
 * 
 */
package nl.surfnet.coin.teams.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.service.TeamsAPIService;
import nl.surfnet.coin.teams.util.HttpClientProvider;
import nl.surfnet.coin.teams.util.TeamEnvironment;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author steinwelberg
 * 
 */
@Component("teamsAPIService")
public class TeamsAPIServiceImpl implements TeamsAPIService {

  private static String invitationUrl = "?request=invitations";
  private static String inviteUrl = "?request=invite";

  @Autowired
  private TeamEnvironment environment;

  @Autowired
  private HttpClientProvider httpClientProvider;

  private ObjectMapper objectMapper;

  public TeamsAPIServiceImpl() {
    this.objectMapper = new ObjectMapper();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.TeamsAPIService#getInvitations(java.lang.
   * String)
   */
  @Override
  public List<Invitation> getInvitations(String teamId)
      throws IllegalStateException, ClientProtocolException, IOException {

    if (teamId != null) {
      String url = environment.getTeamsAPIUrl() + invitationUrl + "&group="
          + teamId;
      InputStream inputStream = httpClientProvider.getHttpClient()
          .execute(new HttpGet(url)).getEntity().getContent();

      return doGetInvitations(teamId, inputStream);
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.TeamsAPIService#sentInvitations(java.util
   * .List, java.lang.String, java.lang.String)
   */
  @Override
  public boolean sentInvitations(List<Invitation> invitations, String teamId,
      String message, String subject) throws IllegalStateException, ClientProtocolException, IOException {

    String emails = null;
    String url = environment.getTeamsAPIUrl() + inviteUrl + "&group=" + teamId
        + "&addresses=" + emails + "&body=" + message + "&subject=" + subject;
    
    
    HttpResponse response = httpClientProvider.getHttpClient().execute(new HttpPost(url));
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != HttpStatus.SC_OK) {
      return false;
    }
    
    // InputStream content = response.getEntity().getContent();
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.TeamsAPIService#requestMembership(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  @Override
  public boolean requestMembership(String teamId, String message, String subject) throws ClientProtocolException, IOException {

    // TODO build correct url
    String emails = null;
    String url = environment.getTeamsAPIUrl() + inviteUrl + "&group=" + teamId
        + "&addresses=" + emails + "&body=" + message + "&subject=" + subject;
    
    
    HttpResponse response = httpClientProvider.getHttpClient().execute(new HttpPost(url));
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != HttpStatus.SC_OK) {
      return false;
    }
    
    //InputStream content = response.getEntity().getContent();
    return true;
  }

  @SuppressWarnings("unchecked")
  private List<Invitation> doGetInvitations(String teamId,
      InputStream inputStream) throws JsonParseException, JsonMappingException,
      IOException {
    List<String> results = getObjectMapper().readValue(inputStream, List.class);

    List<Invitation> invites = new ArrayList<Invitation>();

    for (String result : results) {
      Invitation invite = new Invitation(teamId, result);
      invites.add(invite);
    }

    return invites;
  }

  private ObjectMapper getObjectMapper() {
    return objectMapper;
  }

}
