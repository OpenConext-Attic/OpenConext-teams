package nl.surfnet.coin.teams.service.impl;

import nl.surfnet.coin.teams.domain.ExternalGroup;
import nl.surfnet.coin.teams.domain.ExternalGroupProvider;
import nl.surfnet.coin.teams.service.VootClient;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VootClientImpl implements VootClient {

  private String accessTokenUri;

  private String clientId;

  private String clientSecret;

  private String spaceDelimitedScopes;

  private String serviceUrl;

  private OAuth2RestTemplate vootService;

  public VootClientImpl(String accessTokenUri, String clientId, String clientSecret, String spaceDelimitedScopes, String serviceUrl) {
    this.accessTokenUri = accessTokenUri;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.spaceDelimitedScopes = spaceDelimitedScopes;
    this.serviceUrl = serviceUrl;
    vootService = new OAuth2RestTemplate(vootConfiguration());
  }

  public List<ExternalGroup> groups(String userId) {
    List<Map<String, Object>> maps = vootService.getForObject(serviceUrl + "/internal/external-groups/{userId}", List.class, userId);
    List<ExternalGroup> groups = new ArrayList<ExternalGroup>();
    for (Map<String, Object> map : maps) {
      String sourceId = (String) map.get("sourceID");
      groups.add(new ExternalGroup((String) map.get("id"), (String) map.get("displayName"), (String) map.get("description"), new ExternalGroupProvider(sourceId, sourceId)));
    }
    return groups;
  }

  private OAuth2ProtectedResourceDetails vootConfiguration() {
    ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
    details.setId("voot");
    details.setClientId(clientId);
    details.setClientSecret(clientSecret);
    details.setAccessTokenUri(accessTokenUri);
    details.setScope(Arrays.asList(spaceDelimitedScopes.split(" ")));
    return details;
  }

}
