/**
 * 
 */
package nl.surfnet.coin.teams.control;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;

/**
 * Test for {@link AddMemberController}
 *
 */
public class AddMemberControllerTest extends AbstractControllerTest {
  
  private AddMemberController addMemberController = new AddMemberController();
  final MessageSource messageSource = new ResourceBundleMessageSource() {
    {setBasename("messages");}
  };

  @Test
  public void testStartHappyFlow() throws Exception {

    MockHttpServletRequest request = getRequest();
    // request team
    request.setParameter("team", "team-1");


    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    Team team1 = new Team("team-1", "Team 1", "description", true);

    autoWireMock(addMemberController, new Returns(team1), TeamService.class);

    addMemberController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("description", team.getDescription());
  }

  @Test (expected=RuntimeException.class)
  public void testStart() throws Exception {

    MockHttpServletRequest request = getRequest();
    // request team

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

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

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    Team team1 = new Team("team-1", "Team 1", "description", false);

    autoWireMock(addMemberController, new Returns(team1), TeamService.class);
    autoWireRemainingResources(addMemberController);

    addMemberController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("description", team.getDescription());
  }
  
//  @Test (expected=RuntimeException.class)
//  public void testAddMember() throws Exception {
//    MockHttpServletRequest request = getRequest();
//    // request team
//    request.setParameter("description", "Nice description");
//
//    Team team1 = new Team("team-1", "Team 1", "description");
//
//    autoWireMock(addMemberController, new Returns(team1), TeamService.class);
//    autoWireRemainingResources(addMemberController);
//
//    addMemberController.start(getModelMap(), request);
//    }
//  
  
}
