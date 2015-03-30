package nl.surfnet.coin.teams.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import nl.surfnet.coin.teams.domain.ExternalGroup;
import nl.surfnet.coin.teams.service.impl.VootClientImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class VootClientTest {

  private String personId = "urn:collab:person:example.org:admin";
  private VootClient client = new VootClientImpl("http://localhost:8889/oauth/token", "surf-teams", "secret", "groups", "http://localhost:8889");

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8889);

  @Test
  public void testGroups() throws Exception {
    String groupsJson = IOUtils.toString(new ClassPathResource("mocks/oauth-client-credentials.json").getInputStream());
    String oauthJson = IOUtils.toString(new ClassPathResource("mocks/voot-groups.json").getInputStream());

    stubFor(post(urlEqualTo("/oauth/token")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(groupsJson)));
    stubFor(get(urlEqualTo("/internal/external-groups/" + personId)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(oauthJson)));

    List<ExternalGroup> groups = client.groups(personId);
    assertEquals(2, groups.size());

    ExternalGroup group = groups.get(0);
    assertEquals("urn:collab:group:foo:go", group.getIdentifier());
    assertEquals("go", group.getName());
    assertEquals("Go description", group.getDescription());
    assertEquals("foo", group.getGroupProvider().getIdentifier());

  }


}
