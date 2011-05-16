package nl.surfnet.coin.teams.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.opensocial.models.Person;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.TeamService;

/**
 * Test for {@link InvitationController}
 */
public class InvitationControllerTest extends AbstractControllerTest {
  private InvitationController controller;
  private HttpServletRequest mockRequest;
  private Invitation invitation;

  @Test
  public void testAccept() throws Exception {

    String page = controller.accept(getModelMap(), mockRequest);

    assertEquals("acceptinvitation", page);
  }

  @Test
  public void testDecline() throws Exception {
    String view = controller.decline(getModelMap(), mockRequest);

    assertTrue("Declined invitation", invitation.isDeclined());
    assertEquals("invitationdeclined", view);
  }


  @Test
  public void testDoAccept() throws Exception {

    RedirectView view = controller.doAccept(mockRequest);

    String redirectUrl = "detailteam.shtml?team=team-1&view=app";
    assertEquals(redirectUrl, view.getUrl());
  }

  @Test
  public void testDelete() throws Exception {

    RedirectView view = controller.deleteInvitation(new ModelMap(), mockRequest);

    String redirectUrl = "detailteam.shtml?team=team-1&view=app";
    assertEquals(redirectUrl, view.getUrl());
  }

  @Before
  public void setup() throws Exception {
    super.setup();

    controller = new InvitationController();

    String invitationHash = "0b733d119c3705ae4fc284203f1ee8ec";

    Person mockPerson = mock(Person.class);
    when(mockPerson.getId()).thenReturn("person-1");

    HttpSession mockSession = mock(HttpSession.class);
    when(mockSession.getAttribute(LoginInterceptor.PERSON_SESSION_KEY)).
            thenReturn(mockPerson);

    mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getSession()).thenReturn(mockSession);
    when(mockRequest.getParameter("id")).thenReturn(invitationHash);

    Team mockTeam = mock(Team.class);
    when(mockTeam.getId()).thenReturn("team-1");

    invitation = new Invitation("test-email",
            "team-1");

    TeamInviteService teamInviteService = mock(TeamInviteService.class);
    when(teamInviteService.findInvitationByInviteId(invitationHash)).thenReturn(invitation);

    autoWireMock(controller, teamInviteService, TeamInviteService.class);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);

    autoWireMock(controller, teamService, TeamService.class);
  }

}
