/**
 * 
 */
package nl.surfnet.coin.teams.control;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.teams.service.TeamService;

/**
 * @author steinwelberg
 *
 */
public class AddTeamControllerTest extends AbstractControllerTest{
  
  private AddTeamController addTeamController = new AddTeamController();
  
  @Test
  public void testStart() {
    MockHttpServletRequest request = getRequest();

    String result = addTeamController.start(getModelMap(), request);
    
    assertEquals("addteam", result);
  }

  @Test
  public void testAddTeamHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // request team
    request.setParameter("team", "Team 1");
    request.setParameter("description", "description team 1");

    TeamService teamService = mock(TeamService.class);
    when(teamService.addTeam("Team 1", "Team 1", "description team 1",
            null)).thenReturn("team-1");
    
    autoWireMock(addTeamController, teamService, TeamService.class);
    autoWireRemainingResources(addTeamController);
    
    RedirectView view = addTeamController.addTeam(getModelMap(), request);
        
    assertEquals("detailteam.shtml?team=team-1&view=app", view.getUrl());
  }
  
  @Test (expected=RuntimeException.class)
  public void testAddTeam() throws Exception {
    MockHttpServletRequest request = getRequest();
    // request team
    request.setParameter("description", "description team 1");

    TeamService teamService = mock(TeamService.class);
    when(teamService.addTeam("Team 1", "Team 1", "description team 1",
            null)).thenReturn("team-1");
    
    autoWireMock(addTeamController, teamService, TeamService.class);
    autoWireRemainingResources(addTeamController);
    
    addTeamController.addTeam(getModelMap(), request);
    }
}