/**
 * 
 */
package nl.surfnet.coin.teams.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.service.TeamsAPIService;
import nl.surfnet.coin.teams.util.HttpClientProvider;
import nl.surfnet.coin.teams.util.TeamEnvironment;

/**
 * @author steinwelberg
 *
 */
@Component("teamsAPIService")
public class TeamsAPIServiceImpl implements TeamsAPIService {

  private static String invitationUrl = "?request=invitations";
  private static String inviteUrl = "?request=invite";
  private static String joinUrl = "?request=join";

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
      throws IllegalStateException, IOException {

    if (teamId != null) {
      String url = environment.getTeamsAPIUrl() + invitationUrl + "&group="
          + URLEncoder.encode(teamId, "utf-8");
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
   * nl.surfnet.coin.teams.service.TeamsAPIService#sendInvitations(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  @Override
  public boolean sendInvitations(String emails, String teamId, String message,
                                 String subject) throws IllegalStateException,
    IOException {

    String url = environment.getTeamsAPIUrl() + inviteUrl;
    
    HttpPost httppost = new HttpPost(url);

    // Add your data
    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
        4);
    nameValuePairs.add(new BasicNameValuePair("group", teamId));
    nameValuePairs.add(new BasicNameValuePair("addresses", emails));
    nameValuePairs.add(new BasicNameValuePair("body", message));
    nameValuePairs.add(new BasicNameValuePair("subject", subject));
    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
     
    HttpResponse response = httpClientProvider.getHttpClient().execute(httppost);
    int statusCode = response.getStatusLine().getStatusCode();
    return statusCode == HttpStatus.SC_OK;
  }


  @SuppressWarnings("unchecked")
  private List<Invitation> doGetInvitations(String teamId,
      InputStream inputStream) throws  IOException {
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
