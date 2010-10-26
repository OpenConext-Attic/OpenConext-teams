/**
 * 
 */
package nl.surfnet.coin.teams.control;

import static org.junit.Assert.assertEquals;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author steinwelberg
 * 
 */
public class EditTeamControllerTest extends AbstractControllerTest {

  private EditTeamController editTeamController = new EditTeamController();
  
  private Team mockTeam;
  
  @Before
  public void prepareData() {
    mockTeam = new Team("team-1", "Team 1", "description");
  }

  @Test
  public void testStartHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the team
    request.addParameter("team", "team-1");

    autoWireMock(editTeamController, new Returns(mockTeam), TeamService.class);

    editTeamController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
  }

  @Test(expected = RuntimeException.class)
  public void testStart() throws Exception {
    MockHttpServletRequest request = getRequest();

    autoWireRemainingResources(editTeamController);

    editTeamController.start(getModelMap(), request);
  }

  @Test
  public void testEditTeamHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the teamId, team name & description
    request.addParameter("team", "Team 1");
    request.addParameter("teamId", "team-1");
    request.addParameter("description", "description");

    autoWireMock(editTeamController, new Returns(mockTeam), TeamService.class);

    RedirectView result = editTeamController.editTeam(getModelMap(), request);

    assertEquals("detailteam.shtml?team=team-1", result.getUrl());
  }

  @Test(expected = RuntimeException.class)
  public void testEditTeamNoName() throws Exception {
    MockHttpServletRequest request = getRequest();
    // do NOT add the team name, but do add the team id and team description
    request.addParameter("teamId", "team-1");
    request.addParameter("description", "description");

    autoWireMock(editTeamController, new Returns(mockTeam), TeamService.class);

    editTeamController.editTeam(getModelMap(), request);
  }
  
  @Test(expected = RuntimeException.class)
  public void testEditTeamNoId() throws Exception {
    MockHttpServletRequest request = getRequest();
    // do NOT add the team id, but do add the team name and team description
    request.addParameter("team", "Team 1");
    request.addParameter("description", "description");

    autoWireMock(editTeamController, new Returns(mockTeam), TeamService.class);

    editTeamController.editTeam(getModelMap(), request);
  }

}
