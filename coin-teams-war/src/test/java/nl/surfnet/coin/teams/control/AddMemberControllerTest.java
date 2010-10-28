/**
 * 
 */
package nl.surfnet.coin.teams.control;

import static org.junit.Assert.assertEquals;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author steinwelberg
 *
 */
public class AddMemberControllerTest extends AbstractControllerTest {
  
  private AddMemberController addMemberController = new AddMemberController();

  @Test
  public void testStartHappyFlow() throws Exception {
    
    MockHttpServletRequest request = getRequest();
    // request team
    request.setParameter("team", "team-1");
        
    Team team1 = new Team("team-1", "Team 1", "description");
    
    autoWireMock(addMemberController, new Returns(team1), TeamService.class);
    
    addMemberController.start(getModelMap(), request);
    
    Team team = (Team) getModelMap().get("team");
    
    assertEquals("team-1", team.getId());
  }

  @Test (expected=RuntimeException.class)
  public void testStart() throws Exception {
    
    MockHttpServletRequest request = getRequest();
    // request team
        
    Team team1 = new Team("team-1", "Team 1", "description");
    
    autoWireMock(addMemberController, new Returns(team1), TeamService.class);
    
    addMemberController.start(getModelMap(), request);
  }
  

  @Test
  public void testAddMemberHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // request team
    request.setParameter("team", "team-1");
    request.setParameter("memberEmail", "john@doe.com");
    request.setParameter("message", "Nice description");
        
    Team team1 = new Team("team-1", "Team 1", "description");
    
    autoWireMock(addMemberController, new Returns(team1), TeamService.class);
    
    addMemberController.start(getModelMap(), request);
    
    Team team = (Team) getModelMap().get("team");
    
    assertEquals("team-1", team.getId());
  }
  
  @Test (expected=RuntimeException.class)
  public void testAddMember() throws Exception {
    MockHttpServletRequest request = getRequest();
    // request team
    request.setParameter("description", "Nice description");
        
    Team team1 = new Team("team-1", "Team 1", "description");
    
    autoWireMock(addMemberController, new Returns(team1), TeamService.class);
    
    addMemberController.start(getModelMap(), request);
    }
  
  
}
