/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package teams.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import teams.domain.Invitation;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Team;
import teams.service.TeamInviteService;
import teams.util.ControllerUtil;
import teams.util.TokenUtil;

/**
 * Test for {@link InvitationController}
 */
public class InvitationControllerTest extends AbstractControllerTest {
  private InvitationController controller;
  private Invitation invitation;
  private Team team;
  private ControllerUtil controllerUtil;

  @Before
  public void setup() throws Exception {
    super.setup();

    controller = new InvitationController();

    Person person = getPersonFromSession();
    invitation = new Invitation(person.getEmail(), "team-1");
    team = new Team(invitation.getTeamId());

    String invitationHash = invitation.getInvitationHash();

    getRequest().setParameter("id", invitationHash);
    getRequest().getSession().setAttribute(PERSON_SESSION_KEY, person);

    Team mockTeam = mock(Team.class);
    when(mockTeam.getId()).thenReturn("team-1");

    TeamInviteService teamInviteService = mock(TeamInviteService.class);
    when(teamInviteService.findInvitationByInviteId(invitationHash)).thenReturn(Optional.of(invitation));
    when(teamInviteService.findPendingInvitationsByEmail(invitation.getEmail())).thenReturn(ImmutableList.of(invitation));

    autoWireMock(controller, teamInviteService, TeamInviteService.class);

    controllerUtil = mock(ControllerUtil.class);
    when(controllerUtil.getTeamById("team-1")).thenReturn(mockTeam);

    autoWireMock(controller, controllerUtil, ControllerUtil.class);
    autoWireRemainingResources(controller);
  }

  @Test
  public void testAccept() throws Exception {
    String page = controller.accept(getModelMap(), getRequest());
    assertEquals("acceptinvitation", page);
  }

  @Test
  public void testDecline() throws Exception {
    String view = controller.decline(getModelMap(), getRequest());

    assertTrue("Declined invitation", invitation.isDeclined());
    assertEquals("invitationdeclined", view);
  }

  @Test
  public void testDoAccept() throws Exception {

    ModelAndView modelAndView = controller.doAccept(new ModelMap(), getRequest());

    assertTrue("Accepted invitation", invitation.isAccepted());

    String redirectUrl = "detailteam.shtml?team=team-1";
    assertEquals(redirectUrl, ((RedirectView)modelAndView.getView()).getUrl());
  }

  @Test
  public void testDoAcceptAdmin() throws Exception {
    ModelAndView modelAndView = controller.doAccept(new ModelMap(), getRequest());
    invitation.setIntendedRole(Role.Admin);
    assertTrue("Accepted invitation", invitation.isAccepted());

    assertEquals(invitation.getIntendedRole(), Role.Admin);

    String redirectUrl = "detailteam.shtml?team=team-1";
    assertEquals(redirectUrl, ((RedirectView)modelAndView.getView()).getUrl());
  }

  @Test
  public void testDoAcceptAdminAsGuest() throws Exception {
    Person fromSession = getPersonFromSession();
    Person person = new Person(fromSession.getId(),fromSession.getName(),fromSession.getEmail(),fromSession.getSchacHomeOrganization(),"guest", fromSession.getDisplayName());

    getRequest().getSession().setAttribute(PERSON_SESSION_KEY, person);

    invitation.setIntendedRole(Role.Admin);

    ModelAndView modelAndView = controller.doAccept(new ModelMap(), getRequest());
    assertTrue("Accepted invitation", invitation.isAccepted());

    assertEquals(Role.Manager, invitation.getIntendedRole());

    String redirectUrl = "detailteam.shtml?team=team-1";
    assertEquals(redirectUrl, ((RedirectView)modelAndView.getView()).getUrl());
  }

  @Test
  public void testDoAcceptTwice() throws Exception {
    controller.doAccept(new ModelMap(), getRequest());
    assertTrue("Accepted invitation", invitation.isAccepted());
    ModelAndView modelAndView = controller.doAccept(new ModelMap(), getRequest());
    assertEquals("invitationexception", modelAndView.getViewName());
  }

  @Test
  public void testDeleteAsAnAdmin() throws Exception {
    when(controllerUtil.getTeamById(team.getId())).thenReturn(team);
    when(controllerUtil.hasUserAdministrativePrivileges(getPersonFromSession(), team)).thenReturn(true);

    String token = TokenUtil.generateSessionToken();

    RedirectView view = controller.deleteInvitation(getRequest(), token, token, invitation.getInvitationHash(), new SimpleSessionStatus(), getModelMap());

    String redirectUrl = "detailteam.shtml?team=team-1";

    assertEquals(redirectUrl, view.getUrl());
  }

  @Test(expected = RuntimeException.class)
  public void testCannotDeleteWhenNoAdminPrivileges() throws Exception {
    when(controllerUtil.hasUserAdministrativePrivileges(getPersonFromSession(), team)).thenReturn(false);

    String token = TokenUtil.generateSessionToken();
    controller.deleteInvitation(getRequest(), token, token, invitation.getInvitationHash(), new SimpleSessionStatus(), getModelMap());
  }

  @Test
  public void testMyInvitations() throws Exception {
    String view = controller.myInvitations(getModelMap(), getRequest());
    assertEquals("myinvitations", view);
    @SuppressWarnings("unchecked") List<Invitation> myInvitations = (List<Invitation>) getModelMap().get("invitations");
    assertEquals(1, myInvitations.size());
  }


  private Person getPersonFromSession() {
    return (Person) getRequest().getSession().getAttribute(PERSON_SESSION_KEY);
  }

}
