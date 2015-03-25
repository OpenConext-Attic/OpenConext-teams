package nl.surfnet.coin.teams.service.impl;

import nl.surfnet.coin.teams.domain.ExternalGroup;
import nl.surfnet.coin.teams.service.VootClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.web.client.RestOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VootClientImpl implements VootClient {

  @Value("${voot.accessTokenUri}")
  private String accessTokenUri;

  @Value("${voot.clientId}")
  private String clientId;

  @Value("${voot.clientSecret}")
  private String clientSecret;

  @Value("${voot.redirectUri}")
  private String redirectUri;

  @Value("${voot.scopes}")
  private String spaceDelimitedScopes;

  @Value("${voot.serviceUrl}")
  private String serviceUrl;

  private RestOperations vootService;

  public VootClientImpl() {
    vootService = new OAuth2RestTemplate(vootConfiguration());
  }

  public List<ExternalGroup> groups(String userId) {
    List forObject = vootService.getForObject(serviceUrl + "/internal/external-groups/{userId}", List.class, userId);
    return Collections.emptyList();
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
