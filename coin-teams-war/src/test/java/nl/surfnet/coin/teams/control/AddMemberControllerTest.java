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

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import freemarker.template.Configuration;
import nl.surfnet.coin.api.client.domain.Person;
import nl.surfnet.coin.teams.domain.*;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import nl.surfnet.coin.teams.util.TokenUtil;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link AddMemberController}
 */
public class AddMemberControllerTest extends AbstractControllerTest {

  private final static Logger log = LoggerFactory.getLogger(AddMemberControllerTest.class);

  private AddMemberController addMemberController = new AddMemberController();
  private final MessageSource messageSource = new ResourceBundleMessageSource() {
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
    ListAppender auditAppender = getAuditLogAppender();
    auditAppender.list.clear();

    MockHttpServletRequest request = getRequest();
    Team team1 = getTeam1();
    Person person = getPerson1();
    person.setDisplayName("Member 1");
    request.getSession().setAttribute(LoginInterceptor.PERSON_SESSION_KEY, person);

    // request team
    String token = TokenUtil.generateSessionToken();
    request.setParameter("team", team1.getId());

    GrouperTeamService teamService = mock(GrouperTeamService.class);
    when(teamService.findTeamById(team1.getId())).thenReturn(team1);
    autoWireMock(addMemberController, teamService, GrouperTeamService.class);

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

    Configuration freemarkerConfiguration = getFreemarkerConfig();
    autoWireMock(addMemberController, freemarkerConfiguration, Configuration.class);

    TeamEnvironment environment = new TeamEnvironment();
    environment.setTeamsURL("http://localhost:8060/teams");
    addMemberController.setTeamEnvironment(environment);

    autoWireRemainingResources(addMemberController);

    String result = addMemberController.addMembersToTeam(token,
            form,
            new DirectFieldBindingResult(form, "invitationForm"),
            request,
            token,
            new SimpleSessionStatus(),
            getModelMap());

    assertEquals("redirect:detailteam.shtml?team=" + team1.getId() + "&view=app", result);

    /*
    Assert auditing output
     */
    assertEquals("Two audit events should be appended to audit log: one detailed, one global", 2, auditAppender.list.size());
    LoggingEvent auditEvent = (LoggingEvent) auditAppender.list.get(0);
    assertTrue("Detailed audit event should contain invitee's email address", auditEvent.getFormattedMessage().contains("nonmember@example.com"));
    assertTrue("Detailed audit event should contain inviter's name", auditEvent.getFormattedMessage().contains("member-1"));
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

  @Test
  public void testComposeInvitationMailMessage_HTML() throws Exception {
    Configuration freemarkerConfiguration = getFreemarkerConfig();
    autoWireMock(addMemberController, freemarkerConfiguration, Configuration.class);
    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    TeamEnvironment environment = new TeamEnvironment();
    environment.setTeamsURL("http://localhost:8060/teams");
    addMemberController.setTeamEnvironment(environment);

    GrouperTeamService teamService = mock(GrouperTeamService.class);
    when(teamService.findTeamById(getTeam1().getId())).thenReturn(getTeam1());
    autoWireMock(addMemberController, teamService, GrouperTeamService.class);

    Invitation invitation= new Invitation("johndoe@example.com", getTeam1().getId());
    InvitationMessage message = new InvitationMessage("Hello John,\n\nplease join my team", getPerson1().getId());
    invitation.addInvitationMessage(message);

    Person inviter = getPerson1();
    inviter.setDisplayName("Member One");

    String msg = addMemberController.composeInvitationMailMessage(invitation, inviter, Locale.ENGLISH, "html");

    assertNotNull(msg);
//    log.debug(msg);
    assertTrue(msg.contains("You have been invited by Member One to join team <strong>Team 1</strong>."));
    assertTrue(msg.contains("<strong>Personal message from Member One:</strong><br /> \"Hello John,<br /><br />please join my team\""));
  }

  @Test
  public void testComposeInvitationMailMessage_plaintext() throws Exception {
    Configuration freemarkerConfiguration = getFreemarkerConfig();
    autoWireMock(addMemberController, freemarkerConfiguration, Configuration.class);
    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    TeamEnvironment environment = new TeamEnvironment();
    environment.setTeamsURL("http://localhost:8060/teams");
    addMemberController.setTeamEnvironment(environment);

    GrouperTeamService teamService = mock(GrouperTeamService.class);
    when(teamService.findTeamById(getTeam1().getId())).thenReturn(getTeam1());
    autoWireMock(addMemberController, teamService, GrouperTeamService.class);

    Invitation invitation= new Invitation("johndoe@example.com", getTeam1().getId());
    InvitationMessage message = new InvitationMessage("Hello John,\n\nplease join my team", getPerson1().getId());
    invitation.addInvitationMessage(message);

    Person inviter = getPerson1();
    inviter.setDisplayName("Member One");

    String msg = addMemberController.composeInvitationMailMessage(invitation, inviter, Locale.ENGLISH, "plaintext");

    assertNotNull(msg);
//    log.debug(msg);
    assertTrue(msg.contains("You have been invited by Member One to join team *Team 1*."));
    assertTrue(msg.contains("*Personal message from Member One:*" + System.getProperty("line.separator") + "\"Hello " +
        "John,\n\nplease join my team\""));
  }

}
