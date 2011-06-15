/*
 * Copyright 2011 SURFnet bv, The Netherlands
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

package nl.surfnet.coin.teams.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.opensocial.models.Person;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.JoinTeamRequestService;
import nl.surfnet.coin.teams.service.TeamPersonService;
import nl.surfnet.coin.teams.service.TeamService;

/**
 * Tests for {@link DetailTeamController}
 * 
 */
public class DetailTeamControllerTest extends AbstractControllerTest {

  private DetailTeamController detailTeamController = new DetailTeamController();
  private MockHttpServletResponse response;

  @Test(expected = RuntimeException.class)
  public void testStart() throws Exception {
    MockHttpServletRequest request = getRequest();
    // do NOT add team

    autoWireRemainingResources(detailTeamController);

    detailTeamController.start(getModelMap(), request);
  }

  @Test
  public void testStartNotMember() throws Exception {
    MockHttpServletRequest request = getRequest();
    // add team
    request.setParameter("team", "team-1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);

    HashSet<Member> admins = new HashSet<Member>();
    admins.add(new Member(new HashSet<Role>(), "Jane Doe", "member-2",
        "jane@doe.com"));

    List<Member> members = new ArrayList<Member>();
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    Member member = team.getMembers().get(0);

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("team description", team.getDescription());

    assertEquals(1, team.getMembers().size());
    assertEquals("Jane Doe", member.getName());
    assertEquals("jane@doe.com", member.getEmail());
    assertEquals("member-2", member.getId());
    assertTrue(member.getRoles().contains(Role.Member));
    assertFalse(member.getRoles().contains(Role.Admin));
    assertFalse(member.getRoles().contains(Role.Manager));
    assertEquals("detailteam", result);
  }

  @Test
  public void testStartMember() throws Exception {
    MockHttpServletRequest request = getRequest();
    // add team
    request.setParameter("team", "team-1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);

    HashSet<Member> admins = new HashSet<Member>();
    admins.add(new Member(new HashSet<Role>(), "Jane Doe", "member-2",
        "jane@doe.com"));

    List<Member> members = new ArrayList<Member>();
    members.add(new Member(roles, "John Doe", "member-1", "john@doe.com"));
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    JoinTeamRequestService joinTeamRequestService = mock(JoinTeamRequestService.class);
    when(joinTeamRequestService.findPendingRequests(mockTeam)).thenReturn(
        Collections.EMPTY_LIST);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireMock(detailTeamController, joinTeamRequestService,
        JoinTeamRequestService.class);
    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("team description", team.getDescription());
    assertEquals("detailteam", result);
  }

  @Test
  public void testStartManager() throws Exception {
    MockHttpServletRequest request = getRequest();
    // add team
    request.setParameter("team", "team-1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Manager);
    roles.add(Role.Member);

    HashSet<Member> admins = new HashSet<Member>();
    admins.add(new Member(new HashSet<Role>(), "Jane Doe", "member-2",
        "jane@doe.com"));

    List<Member> members = new ArrayList<Member>();
    members.add(new Member(roles, "John Doe", "member-1", "john@doe.com"));
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    JoinTeamRequestService joinTeamRequestService = mock(JoinTeamRequestService.class);
    when(joinTeamRequestService.findPendingRequests(mockTeam)).thenReturn(
        Collections.EMPTY_LIST);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireMock(detailTeamController, joinTeamRequestService,
        JoinTeamRequestService.class);

    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("team description", team.getDescription());
    assertEquals("detailteam", result);
  }

  @Test
  public void testStartAdmin() throws Exception {
    MockHttpServletRequest request = getRequest();
    // add team
    request.setParameter("team", "team-1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Manager);
    roles.add(Role.Member);
    roles.add(Role.Admin);

    HashSet<Member> admins = new HashSet<Member>();
    admins.add(new Member(new HashSet<Role>(), "Jane Doe", "member-2",
        "jane@doe.com"));

    List<Member> members = new ArrayList<Member>();
    members.add(new Member(roles, "John Doe", "member-1", "john@doe.com"));
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    JoinTeamRequestService joinTeamRequestService = mock(JoinTeamRequestService.class);
    when(joinTeamRequestService.findPendingRequests(mockTeam)).thenReturn(
        Collections.EMPTY_LIST);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireMock(detailTeamController, joinTeamRequestService,
        JoinTeamRequestService.class);

    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");
    boolean onlyAdmin = (Boolean) getModelMap().get("onlyAdmin");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("team description", team.getDescription());
    assertTrue(onlyAdmin);
    assertEquals("detailteam", result);
  }

  @Test
  public void testLeaveTeamHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // add team
    request.setParameter("team", "team-1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Manager);
    roles.add(Role.Member);
    roles.add(Role.Admin);

    HashSet<Member> admins = new HashSet<Member>();
    admins.add(new Member(new HashSet<Role>(), "Jane Doe", "member-2",
        "jane@doe.com"));
    admins.add(new Member(new HashSet<Role>(), "John Doe", "member-1",
        "john@doe.com"));

    List<Member> members = new ArrayList<Member>();
    members.add(new Member(roles, "John Doe", "member-1", "john@doe.com"));
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController
        .leaveTeam(getModelMap(), request);

    assertEquals("home.shtml?teams=my&view=app", result.getUrl());
  }

  @Test
  public void testLeaveTeam() throws Exception {
    MockHttpServletRequest request = getRequest();
    // add team
    request.setParameter("team", "team-1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Manager);
    roles.add(Role.Member);
    roles.add(Role.Admin);

    HashSet<Member> admins = new HashSet<Member>();
    admins.add(new Member(new HashSet<Role>(), "John Doe", "member-1",
        "john@doe.com"));

    List<Member> members = new ArrayList<Member>();
    members.add(new Member(roles, "John Doe", "member-1", "john@doe.com"));
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController
        .leaveTeam(getModelMap(), request);

    assertEquals(
        "detailteam.shtml?team=team-1&view=app&mes=error.AdminCannotLeaveTeam",
        result.getUrl());
  }

  @Test
  public void testDeleteTeamHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // add team
    request.setParameter("team", "team-1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Manager);
    roles.add(Role.Member);
    roles.add(Role.Admin);

    Member member = new Member(roles, "John Doe", "member-1", "john@doe.com");

    autoWireMock(detailTeamController, new Returns(member), TeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController.deleteTeam(getModelMap(),
        request);

    assertEquals("home.shtml?teams=my&view=app", result.getUrl());
  }

  @Test
  public void testDeleteTeam() throws Exception {
    MockHttpServletRequest request = getRequest();
    // add team
    request.setParameter("team", "team-1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);

    Member member = new Member(roles, "John Doe", "member-1", "john@doe.com");

    autoWireMock(detailTeamController, new Returns(member), TeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController.deleteTeam(getModelMap(),
        request);

    assertEquals("detailteam.shtml?team=team-1&view=app", result.getUrl());
  }

  @Test(expected = RuntimeException.class)
  public void testDeleteTeamException() throws Exception {
    MockHttpServletRequest request = getRequest();
    // do NOT add the team

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);

    Member member = new Member(roles, "John Doe", "member-1", "john@doe.com");

    autoWireMock(detailTeamController, new Returns(member), TeamService.class);
    autoWireRemainingResources(detailTeamController);

    detailTeamController.deleteTeam(getModelMap(), request);
  }

  @Test
  public void testDeleteMemberHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // add the team & member
    request.addParameter("team", "team-1");
    request.addParameter("member", "member-2");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Manager);
    roles.add(Role.Member);
    roles.add(Role.Admin);

    Member owner = new Member(roles, "John Doe", "member-1", "john@doe.com");
    Member member = new Member(roles, "Jane Doe", "member-2", "jane@doe.com");

    TeamService teamService = mock(TeamService.class);
    when(teamService.findMember("team-1", "member-2")).thenReturn(member);
    when(teamService.findMember("team-1", "member-1")).thenReturn(owner);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController.deleteMember(getModelMap(),
        request);

    assertEquals("detailteam.shtml?team=team-1&view=app", result.getUrl());
  }

  @Test
  public void testDeleteMember() throws Exception {
    MockHttpServletRequest request = getRequest();
    // add the team & member
    request.addParameter("team", "team-1");
    request.addParameter("member", "member-1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);

    Member member = new Member(roles, "John Doe", "member-1", "john@doe.com");

    autoWireMock(detailTeamController, new Returns(member), TeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController.deleteMember(getModelMap(),
        request);

    assertEquals(
        "detailteam.shtml?team=team-1&mes=error.NotAuthorizedToDeleteMember&view=app",
        result.getUrl());
  }

  @Test(expected = RuntimeException.class)
  public void testDeleteMemberException() throws Exception {
    MockHttpServletRequest request = getRequest();
    // do NOT add the team & member

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);

    Member member = new Member(roles, "John Doe", "member-1", "john@doe.com");

    autoWireMock(detailTeamController, new Returns(member), TeamService.class);
    autoWireRemainingResources(detailTeamController);

    detailTeamController.deleteMember(getModelMap(), request);
  }

  @Test
  public void testAddRoleHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    request.addParameter("teamId", "team-1");
    request.addParameter("memberId", "member-1");
    request.addParameter("roleId", Role.Manager.toString());
    request.addParameter("doAction", "add");

    TeamService teamService = mock(TeamService.class);
    Set<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);
    Member member = new Member(roles, "Member One", "member-1",
        "member1@example.com");
    when(teamService.findMember("team-1", "member-1")).thenReturn(member);
    when(teamService.addMemberRole("team-1", "member-1", Role.Manager, false))
        .thenReturn(true);
    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView view = detailTeamController.addOrRemoveRole(getModelMap(),
        request, response);
    assertEquals(
        "detailteam.shtml?team=team-1&view=app&mes=role.added&offset=0",
        view.getUrl());
  }

  @Test
  public void testAddRoleNotAuthorized() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the team, member & role
    request.addParameter("teamId", "team-1");
    request.addParameter("memberId", "member-1");
    request.addParameter("roleId", Role.Manager.toString());
    request.addParameter("doAction", "add");

    autoWireMock(detailTeamController, new Returns(false), TeamService.class);
    autoWireRemainingResources(detailTeamController);

    TeamService teamService = mock(TeamService.class);
    Set<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);
    Member member = new Member(roles, "Member One", "member-1",
        "member1@example.com");
    when(teamService.findMember("team-1", "member-1")).thenReturn(member);
    when(teamService.addMemberRole("team-1", "member-1", Role.Manager, false))
        .thenReturn(false);
    autoWireMock(detailTeamController, teamService, TeamService.class);

    RedirectView view = detailTeamController.addOrRemoveRole(getModelMap(),
        request, response);
    assertEquals(
        "detailteam.shtml?team=team-1&view=app&mes=no.role.added&offset=0",
        view.getUrl());
  }

  @Test
  public void testRemoveRoleHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    request.addParameter("teamId", "team-1");
    request.addParameter("memberId", "member-1");
    request.addParameter("roleId", "1");
    request.addParameter("doAction", "remove");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);
    roles.add(Role.Manager);
    roles.add(Role.Admin);

    HashSet<Member> admins = new HashSet<Member>();
    admins.add(new Member(new HashSet<Role>(), "Jane Doe", "member-2",
        "jane@doe.com"));

    List<Member> members = new ArrayList<Member>();
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);
    when(
        teamService.removeMemberRole("team-1", "member-1", Role.Manager, false))
        .thenReturn(true);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView view = detailTeamController.addOrRemoveRole(getModelMap(),
        request, response);

    assertEquals(
        "detailteam.shtml?team=team-1&view=app&mes=role.removed&offset=0",
        view.getUrl());
  }

  @Test
  public void testRemoveRoleOneAdmin() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the team, member & role
    request.addParameter("teamId", "team-1");
    request.addParameter("memberId", "member-1");
    request.addParameter("roleId", "0");
    request.addParameter("doAction", "remove");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);
    roles.add(Role.Manager);
    roles.add(Role.Admin);

    HashSet<Member> admins = new HashSet<Member>();
    admins.add(new Member(new HashSet<Role>(), "Jane Doe", "member-2",
        "jane@doe.com"));

    List<Member> members = new ArrayList<Member>();
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);
    when(teamService.removeMemberRole("team-1", "member-1", Role.Admin, false))
        .thenReturn(false);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView view = detailTeamController.addOrRemoveRole(getModelMap(),
        request, response);

    assertEquals("detailteam.shtml?team=team-1&view=app&mes=no.role.added.admin.status&offset=0", view.getUrl());

  }

  @Test
  public void testRemoveRoleException() throws Exception {
    MockHttpServletRequest request = getRequest();
    // do NOT add the team, member & role

    autoWireRemainingResources(detailTeamController);

    RedirectView view = detailTeamController.addOrRemoveRole(getModelMap(),
        request, response);

    assertEquals("home.shtml?teams=my&view=app", view.getUrl());
  }

  @Test
  public void testDeleteRequest() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the team, member & role
    request.addParameter("team", "team-1");
    request.addParameter("member", "potential-member-1");
    request.addParameter("role", "0");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);
    roles.add(Role.Manager);
    roles.add(Role.Admin);

    HashSet<Member> admins = new HashSet<Member>();
    admins.add(new Member(new HashSet<Role>(), "Jane Doe", "member-1",
        "jane@doe.com"));

    List<Member> members = new ArrayList<Member>();
    Member loggedInMember = new Member(roles, "Jane Doe", "member-1",
        "jane@doe.com");
    members.add(loggedInMember);

    Person loggedInPerson = mock(Person.class);
    when(loggedInPerson.getId()).thenReturn("member-1");

    Person memberToAdd = mock(Person.class);
    when(memberToAdd.getId()).thenReturn("potential-member-1");

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);
    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findMember("team-1", "member-1")).thenReturn(
        loggedInMember);

    JoinTeamRequest joinTeamRequest = new JoinTeamRequest();
    joinTeamRequest.setGroupId(mockTeam.getId());
    joinTeamRequest.setPersonId(memberToAdd.getId());

    JoinTeamRequestService joinTeamRequestService = mock(JoinTeamRequestService.class);
    when(joinTeamRequestService.findPendingRequest(memberToAdd, mockTeam))
        .thenReturn(joinTeamRequest);

    TeamPersonService teamPersonService = mock(TeamPersonService.class);
    when(teamPersonService.getPerson("member-2")).thenReturn(loggedInPerson);
    when(teamPersonService.getPerson("potential-member-1")).thenReturn(
        memberToAdd);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireMock(detailTeamController, teamPersonService,
        TeamPersonService.class);
    autoWireMock(detailTeamController, joinTeamRequestService,
        JoinTeamRequestService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController.deleteRequest(getModelMap(),
        request);
    assertEquals("detailteam.shtml?team=team-1&view=app", result.getUrl());
  }

  @Override
  public void setup() throws Exception {
    super.setup();
    this.response = new MockHttpServletResponse();
  }
}
