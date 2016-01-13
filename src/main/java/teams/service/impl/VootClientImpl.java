package teams.service.impl;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import teams.domain.ExternalGroup;
import teams.domain.ExternalGroupProvider;
import teams.service.VootClient;

public class VootClientImpl implements VootClient {

  private static final Logger LOG = LoggerFactory.getLogger(VootClientImpl.class);

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
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> maps = vootService.getForObject(serviceUrl + "/internal/external-groups/{userId}", List.class, userId);

    return maps.stream().map(map -> {
      String sourceId = (String) map.get("sourceID");
      return new ExternalGroup((String) map.get("id"), (String) map.get("displayName"), (String) map.get("description"), new ExternalGroupProvider(sourceId, sourceId));
    }).collect(toList());
  }

  private OAuth2ProtectedResourceDetails vootConfiguration() {
    ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
    LOG.debug("clientId: {}", clientId);
    details.setId("voot");
    details.setClientId(clientId);
    details.setClientSecret(clientSecret);
    details.setAccessTokenUri(accessTokenUri);
    details.setScope(Arrays.asList(spaceDelimitedScopes.split(" ")));
    return details;
  }

}
