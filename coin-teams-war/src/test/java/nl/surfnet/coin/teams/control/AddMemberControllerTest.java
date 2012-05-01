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

/**
 *
 */
package nl.surfnet.coin.teams.control;

import java.util.Locale;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.opensocial.models.Person;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.servlet.LocaleResolver;

import nl.surfnet.coin.teams.domain.InvitationForm;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.TokenUtil;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link AddMemberController}
 */
public class AddMemberControllerTest extends AbstractControllerTest {

  private AddMemberController addMemberController = new AddMemberController();
  final MessageSource messageSource = new ResourceBundleMessageSource() {
    {
      setBasename("messages");
    }
  };

  @Test
  public void testStartHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    Team team1 = getTeam1();
    Person person = getPerson1();

    // request team
    request.setParameter("team", team1.getId());

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    ControllerUtil controllerUtil = mock(ControllerUtil.class);
    when(controllerUtil.getTeam(request)).thenReturn(team1);
    when(controllerUtil.hasUserAdministrativePrivileges(person, team1.getId())).thenReturn(true);

    autoWireMock(addMemberController, controllerUtil, ControllerUtil.class);
    autoWireRemainingResources(addMemberController);

    addMemberController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals(team1.getId(), team.getId());
    assertEquals(team1.getName(), team.getName());
    assertEquals(team1.getDescription(), team.getDescription());
  }

  @Test(expected = RuntimeException.class)
  public void testStart() throws Exception {

    MockHttpServletRequest request = getRequest();

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    Team team1 = getTeam1();

    autoWireMock(addMemberController, new Returns(team1), GrouperTeamService.class);

    addMemberController.start(getModelMap(), request);
  }


  @Test
  public void testAddMemberHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    Team team1 = getTeam1();
    Person person = getPerson1();

    // request team
    request.setParameter("team", team1.getId());
    request.setParameter("memberEmail", "john@doe.com");
    request.setParameter("message", "Nice description");

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    ControllerUtil controllerUtil = mock(ControllerUtil.class);
    when(controllerUtil.getTeam(request)).thenReturn(team1);
    when(controllerUtil.hasUserAdministrativePrivileges(person, team1.getId())).thenReturn(true);
    autoWireMock(addMemberController, controllerUtil, ControllerUtil.class);
    autoWireRemainingResources(addMemberController);

    addMemberController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals(team1.getId(), team.getId());
    assertEquals(team1.getName(), team.getName());
    assertEquals(team1.getDescription(), team.getDescription());
  }

  @Test(expected = RuntimeException.class)
  public void testAddMemberWrongPrivileges() throws Exception {
    MockHttpServletRequest request = getRequest();
    Team team1 = getTeam1();
    Person person = getPerson1();

    // request team
    request.setParameter("team", team1.getId());
    request.setParameter("memberEmail", "john@doe.com");
    request.setParameter("message", "Nice description");

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);


    ControllerUtil controllerUtil = mock(ControllerUtil.class);
    when(controllerUtil.getTeam(request)).thenReturn(team1);
    when(controllerUtil.hasUserAdministrativePrivileges(person, "team-1")).thenReturn(false);

    autoWireMock(addMemberController, controllerUtil, ControllerUtil.class);
    autoWireRemainingResources(addMemberController);

    addMemberController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals(team1.getId(), team.getId());
    assertEquals(team1.getId(), team.getName());
    assertEquals(team1.getDescription(), team.getDescription());
  }

  @Test
  public void testDoAddMemberHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    Team team1 = getTeam1();
    Person person = getPerson1();

    // request team
    String token = TokenUtil.generateSessionToken();
    request.setParameter("team", team1.getId());

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    InvitationForm form = new InvitationForm();
    form.setEmails("nonmember@example.com");
    form.setInviter(person);
    form.setMessage("A nice invite message");
    form.setTeamId(team1.getId());

    ControllerUtil controllerUtil = mock(ControllerUtil.class);
    when(controllerUtil.hasUserAdministrativePrivileges(person, team1.getId())).thenReturn(true);
    when(controllerUtil.getTeamById(team1.getId())).thenReturn(team1);

    autoWireMock(addMemberController, controllerUtil, ControllerUtil.class);
    autoWireRemainingResources(addMemberController);

    String result = addMemberController.addMembersToTeam(token,
            form,
            new DirectFieldBindingResult(form, "invitationForm"),
            request,
            token,
            new SimpleSessionStatus(),
            getModelMap());

    assertEquals("redirect:detailteam.shtml?team=" + team1.getId() + "&view=app", result);
  }

  @Test(expected = RuntimeException.class)
  public void testDoAddMemberNoPrivileges() throws Exception {
    MockHttpServletRequest request = getRequest();
    Team team1 = getTeam1();
    Member member1 = getMember();
    Person person = getPerson1();

    // request team
    String token = TokenUtil.generateSessionToken();
    request.setParameter("team", team1.getId());

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    InvitationForm form = new InvitationForm();
    form.setEmails("nonmember@example.com");
    form.setInviter(person);
    form.setMessage("A nice invite message");
    form.setTeamId(getTeam1().getId());

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById(team1.getId())).thenReturn(team1);
    when(grouperTeamService.findMember(team1.getId(), member1.getId())).thenReturn(member1);
    when(grouperTeamService.findTeamById(team1.getId())).thenReturn(team1);

    autoWireMock(addMemberController, grouperTeamService, GrouperTeamService.class);
    autoWireRemainingResources(addMemberController);

    addMemberController.addMembersToTeam(token, form, new DirectFieldBindingResult(form, "invitationForm"), request,
        token, new SimpleSessionStatus(), getModelMap());
  }

  @Test(expected = RuntimeException.class)
  public void testDoAddMemberNoMember() throws Exception {
    MockHttpServletRequest request = getRequest();
    Team team1 = getTeam1();
    Member member1 = getAdministrativeMember();
    Person person = getPerson1();

    // request team
    String token = TokenUtil.generateSessionToken();
    request.setParameter("team", getTeam1().getId());

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    InvitationForm form = new InvitationForm();
    form.setEmails("nonmember@example.com");
    form.setInviter(person);
    form.setMessage("A nice invite message");
    form.setTeamId(team1.getId());

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById(team1.getId())).thenReturn(team1);
    when(grouperTeamService.findMember(team1.getId(), member1.getId())).thenReturn(null);
    when(grouperTeamService.findTeamById(team1.getId())).thenReturn(team1);

    autoWireMock(addMemberController, grouperTeamService, GrouperTeamService.class);
    autoWireRemainingResources(addMemberController);

    addMemberController.addMembersToTeam(token,
            form,
            new DirectFieldBindingResult(form, "invitationForm"),
            request,
            token,
            new SimpleSessionStatus(),
            getModelMap());
  }
}
