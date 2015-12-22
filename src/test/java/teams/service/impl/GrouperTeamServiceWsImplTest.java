package teams.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Rule;
import org.junit.Test;

import teams.domain.Team;

public class GrouperTeamServiceWsImplTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8877);

  private GrouperTeamServiceWsImpl subject = new GrouperTeamServiceWsImpl(null, "defaultstem", "poweruser");

  @Test
  public void findPublicTeamsWithNoResultsShouldGiveEmptyList() throws Exception {
    stubFor(post(urlEqualTo("/grouper-ws/servicesRest/VERSION/groups"))
        .withHeader(CONTENT_TYPE, equalTo("text/xml; charset=UTF-8"))
        .willReturn(okResponse("empty_find_groups_response.xml")));

      List<Team> publicTeams = subject.findPublicTeams("person::id", "groupname::part");

      assertTrue(publicTeams.isEmpty());
  }

  @Test
  public void findPublicTeamsWithResultsShouldGiveATeam() throws Exception {
    stubFor(post(urlEqualTo("/grouper-ws/servicesRest/VERSION/groups"))
        .withHeader(CONTENT_TYPE, equalTo("text/xml; charset=UTF-8"))
        .willReturn(okResponse("non_empty_find_groups_response.xml")));

      List<Team> publicTeams = subject.findPublicTeams("person::id", "groupname::part");

      assertThat(publicTeams, hasSize(1));
  }

  private ResponseDefinitionBuilder okResponse(String filename) throws IOException, URISyntaxException {
    return aResponse()
            .withStatus(200)
            .withHeader("X-Grouper-success", "T")
            .withHeader("X-Grouper-resultCode", "200")
            .withBody(readResponseFile(filename));
  }

  private byte[] readResponseFile(String filename) throws IOException, URISyntaxException {
    return Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("grouper/" + filename).toURI()));
  }

}
