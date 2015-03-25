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
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.TokenUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.servlet.view.RedirectView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author steinwelberg
 */
public class EditTeamControllerTest extends AbstractControllerTest {

  private EditTeamController editTeamController = new EditTeamController();
  private Team mockTeam;
  private Member mockAdminMember;
  private Member mockMember;

  @Before
  public void prepareData() {
    mockTeam = getTeam1();
    mockAdminMember = getAdministrativeMember();
    mockMember = getMember();
  }

  @Test
  public void testStartHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the team
    request.addParameter("team", "team-1");

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findMember("team-1", "member-1")).thenReturn(
      mockAdminMember);
    autoWireMock(editTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(editTeamController, new Returns(true), ControllerUtil.class);
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

    ListAppender auditAppender = getAuditLogAppender();
    auditAppender.list.clear();

    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // Add the teamId, team name, description & token
    request.addParameter("teamName", "Another name");
    request.addParameter("team", "team-1");
    request.addParameter("description", "description");
    request.addParameter("token", token);

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findMember("team-1", "member-1")).thenReturn(
      mockAdminMember);
    autoWireMock(editTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(editTeamController, new Returns(true), ControllerUtil.class);
    autoWireRemainingResources(editTeamController);

    RedirectView result = editTeamController.editTeam(getModelMap(), request, token, token, new SimpleSessionStatus());

    assertEquals("detailteam.shtml?team=team-1&view=app", result.getUrl());


    assertEquals("An audit event should be appended to audit log", 1, auditAppender.list.size());
    LoggingEvent auditEvent = (LoggingEvent) auditAppender.list.get(0);
    assertTrue("Audit event should contain old team name", auditEvent.getFormattedMessage().contains("Team 1"));
  }

  @Test(expected = RuntimeException.class)
  public void testEditTeamNoPrivileges() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // Add the teamId, team name, description & token
    request.addParameter("teamName", "Team 1");
    request.addParameter("team", "team-1");
    request.addParameter("description", "description");
    request.addParameter("token", token);

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findMember("team-1", "member-1")).thenReturn(
      mockMember);
    autoWireMock(editTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(editTeamController, new Returns(false), ControllerUtil.class);
    autoWireRemainingResources(editTeamController);

    editTeamController.editTeam(getModelMap(), request, token, token, new SimpleSessionStatus());
  }

  @Test(expected = RuntimeException.class)
  public void testEditTeamNoMember() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // Add the teamId, team name, description & token
    request.addParameter("teamName", "Team 1");
    request.addParameter("team", "team-1");
    request.addParameter("description", "description");
    request.addParameter("token", token);

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(null);
    when(grouperTeamService.findMember("team-1", "member-1")).thenReturn(
      mockAdminMember);
    autoWireMock(editTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(editTeamController, new Returns(true), ControllerUtil.class);
    autoWireRemainingResources(editTeamController);

    editTeamController.editTeam(getModelMap(), request, token, token, new SimpleSessionStatus());
  }

  @Test(expected = SecurityException.class)
  public void testEditTeamWrongToken() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();

    // do NOT add the team id, but do add the team name, team description & token
    request.addParameter("team", "Team 1");
    request.addParameter("description", "description");
    request.addParameter("token", token);

    autoWireMock(editTeamController, new Returns(mockTeam), GrouperTeamService.class);
    autoWireMock(editTeamController, new Returns(true), ControllerUtil.class);
    autoWireRemainingResources(editTeamController);

    editTeamController.editTeam(getModelMap(), request, token, "asfjkhsdjkfhsd", new SimpleSessionStatus());
  }

}
