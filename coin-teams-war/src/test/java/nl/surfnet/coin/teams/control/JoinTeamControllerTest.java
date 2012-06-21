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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

import freemarker.template.Configuration;
import nl.surfnet.coin.api.client.domain.Email;
import nl.surfnet.coin.api.client.domain.Person;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.TeamEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link JoinTeamController}
 */
public class JoinTeamControllerTest extends AbstractControllerTest {

  private static final Logger log = LoggerFactory.getLogger(JoinTeamControllerTest.class);
  private JoinTeamController joinTeamController = new JoinTeamController();
  private Team mockTeam;
  private Team mockPrivateTeam;

  @Before
  public void prepareData() {
    mockTeam = new Team("team-1", "Team 1", "description", true);
    mockPrivateTeam = new Team("team-2", "Team 2", "description", false);
  }

  @Test
  public void testStartHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the team
    request.addParameter("team", "team-1");

    autoWireMock(joinTeamController, new Returns(mockTeam), GrouperTeamService.class);
    autoWireRemainingResources(joinTeamController);

    joinTeamController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("description", team.getDescription());
  }

  @Test(expected = RuntimeException.class)
  public void testStart() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Do NOT add the team

    autoWireRemainingResources(joinTeamController);

    joinTeamController.start(getModelMap(), request);
  }

  @Test
  public void testJoinTeamHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();

    Person requester = new Person();
    requester.setId("urn:collab:person:com.example:john.doe");
    requester.setDisplayName("John Doe");
    Set<Email> emails = new TreeSet<Email>();
    emails.add(new Email("john.doe@example.com"));
    requester.setEmails(emails);
    request.getSession().setAttribute(LoginInterceptor.PERSON_SESSION_KEY, requester);

    TeamEnvironment environment = new TeamEnvironment();
    environment.setTeamsURL("http://localhost:8060/teams");
    joinTeamController.setTeamEnvironment(environment);

    Member admin = getAdministrativeMember();
    Set<Member> admins = new HashSet<Member>();
    admins.add(admin);

    GrouperTeamService mockGrouperTeamService = mock(GrouperTeamService.class);
    when(mockGrouperTeamService.findAdmins(mockTeam)).thenReturn(admins);
    autoWireMock(joinTeamController, mockGrouperTeamService, GrouperTeamService.class);

    LocaleResolver localeResolver = mock(LocaleResolver.class);
    when(localeResolver.resolveLocale(request)).thenReturn(Locale.ENGLISH);
    autoWireMock(joinTeamController, localeResolver, LocaleResolver.class);

    JoinTeamRequest joinTeamRequest = new JoinTeamRequest("ID2345", "team-2");
    joinTeamRequest.setMessage("Hello,\ncan I please join this team?");

    Configuration freemarkerConfiguration = getFreemarkerConfig();
    autoWireMock(joinTeamController, freemarkerConfiguration, Configuration.class);

    autoWireMock(joinTeamController, new Returns(mockTeam), ControllerUtil.class);
    autoWireRemainingResources(joinTeamController);

    RedirectView result = joinTeamController.joinTeam(getModelMap(), joinTeamRequest, request);

    assertEquals("home.shtml?teams=my&view=app", result.getUrl());
  }

  @Test(expected = RuntimeException.class)
  public void testJoinTeam() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Do NOT add the team

    autoWireRemainingResources(joinTeamController);

    joinTeamController.joinTeam(getModelMap(), null, request);
  }
    
  @Test(expected = IllegalStateException.class)
  public void testJoinPrivateTeam() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the team
    request.addParameter("team", "team-2");
    request.addParameter("message", "message");

    GrouperTeamService mockGrouperTeamService = mock(GrouperTeamService.class);
    autoWireMock(joinTeamController, mockGrouperTeamService, GrouperTeamService.class);

    JoinTeamRequest joinTeamRequest = new JoinTeamRequest("ID2345", "team-2");

    autoWireMock(joinTeamController, new Returns(mockPrivateTeam), ControllerUtil.class);
    autoWireRemainingResources(joinTeamController);
    joinTeamController.joinTeam(getModelMap(), joinTeamRequest, request);

  }

  @Test
  public void testComposeJoinRequestMailMessage_html() throws Exception {
    Person requester = getPerson1();
    requester.setDisplayName("Humble User");
    Set<Email> emails = new TreeSet<Email>();
    emails.add(new Email("humble.user@example.com"));
    requester.setEmails(emails);

    Configuration freemarkerConfiguration = getFreemarkerConfig();
    autoWireMock(joinTeamController, freemarkerConfiguration, Configuration.class);

    TeamEnvironment environment = new TeamEnvironment();
    environment.setTeamsURL("http://localhost:8060/teams");
    joinTeamController.setTeamEnvironment(environment);

    String message = "Hello admin,\n\ncan I join this team please?\n\nRegards,\nHumble User";
    final String body = joinTeamController.composeJoinRequestMailMessage(mockPrivateTeam, requester, message, Locale.ENGLISH, "html");

    assertNotNull(body);
    log.debug(body);

    assertTrue(body.contains("Humble User (humble.user@example.com) would like to join team <strong>Team 2</strong>."));
    assertTrue(body.contains("<strong>Personal message from Humble User:</strong><br /> \"Hello admin,<br /><br />" +
        "can I join this team please?<br /><br />Regards,<br />Humble User\""));

  }

  @Test
  public void testComposeJoinRequestMailMessage_text() throws Exception {
    Person requester = getPerson1();
    requester.setDisplayName("Humble User");
    Set<Email> emails = new TreeSet<Email>();
    emails.add(new Email("humble.user@example.com"));
    requester.setEmails(emails);

    Configuration freemarkerConfiguration = getFreemarkerConfig();
    autoWireMock(joinTeamController, freemarkerConfiguration, Configuration.class);

    TeamEnvironment environment = new TeamEnvironment();
    environment.setTeamsURL("http://localhost:8060/teams");
    joinTeamController.setTeamEnvironment(environment);

    String message = "Hello admin,\n\ncan I join this team please?\n\nRegards,\nHumble User";
    final String body = joinTeamController.composeJoinRequestMailMessage(mockPrivateTeam, requester, message, Locale.ENGLISH, "plaintext");

    assertNotNull(body);
    log.debug(body);
    assertTrue(body.contains("Humble User (humble.user@example.com) would like to join team *Team 2*."));
    assertTrue(body.contains("*Personal message from Humble User:*" + System.getProperty("line.separator") + "\"" +
        message + "\""));
  }
}
