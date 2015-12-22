package teams.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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

import teams.domain.Role;
import teams.domain.Team;
import teams.domain.TeamResultWrapper;

public class GrouperTeamServiceWsImplTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8877);

  private GrouperTeamServiceWsImpl subject = new GrouperTeamServiceWsImpl(null, "defaultstem", "poweruser");

  @Test
  public void findPublicTeamsWithNoResultsShouldGiveEmptyList() throws Exception {
    stubGrouper("groups", "empty_find_groups_response.xml");

    List<Team> publicTeams = subject.findPublicTeams("person::id", "groupname::part");

    assertTrue(publicTeams.isEmpty());
  }

  @Test
  public void findPublicTeamsWithResultsShouldGiveATeam() throws Exception {
    stubGrouper("groups", "non_empty_find_groups_response.xml");

    List<Team> publicTeams = subject.findPublicTeams("person::id", "groupname::part");

    assertThat(publicTeams, hasSize(1));
  }

  @Test
  public void findTeamsByMemberGivesResults() throws Exception {
    stubGrouper("subjects", "non_empty_get_groups_response.xml");
    stubGrouper("memberships", "non_empty_get_memberships_response.xml");
    stubGrouper("grouperPrivileges", "non_empty_get_grouper_privileges_response.xml");

    TeamResultWrapper resultWrapper = subject.findTeamsByMember("person::id", "groupname::part", 0, 10);

    assertThat(resultWrapper.getOffset(), is(0));
    assertThat(resultWrapper.getPageSize(), is(10));
    assertThat(resultWrapper.getTeams(), hasSize((int) resultWrapper.getTotalCount()));

    Team team = resultWrapper.getTeams().get(0);

    assertThat(team.getId(), is("groupname"));
    assertThat(team.getName(), is("displayExtension"));
    assertThat(team.getNumberOfMembers(), is(2));
    assertThat(team.getViewerRole(), is(Role.Admin));
  }

  private void stubGrouper(String requestSuffix, String responseFile) throws IOException, URISyntaxException {
    stubFor(post(urlEqualTo("/grouper-ws/servicesRest/VERSION/" + requestSuffix))
        .withHeader(CONTENT_TYPE, equalTo("text/xml; charset=UTF-8"))
        .willReturn(okResponse(responseFile)));
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
