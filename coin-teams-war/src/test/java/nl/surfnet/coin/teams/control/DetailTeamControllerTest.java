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

package nl.surfnet.coin.teams.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.opensocial.models.Person;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.opensocial.service.PersonService;
import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.domain.TeamExternalGroup;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.JoinTeamRequestService;
import nl.surfnet.coin.teams.service.TeamExternalGroupDao;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.TokenUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DetailTeamController}
 *
 */
public class DetailTeamControllerTest extends AbstractControllerTest {

  private DetailTeamController detailTeamController = new DetailTeamController();

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

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findAdmins(mockTeam)).thenReturn(admins);

    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
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

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findAdmins(mockTeam)).thenReturn(admins);

    JoinTeamRequestService joinTeamRequestService = mock(JoinTeamRequestService.class);
    when(joinTeamRequestService.findPendingRequests(mockTeam)).thenReturn(
        Collections.EMPTY_LIST);

    TeamExternalGroupDao teamExternalGroupDao = mock(TeamExternalGroupDao.class);
    when(teamExternalGroupDao.getByTeamIdentifier("team-1")).thenReturn(new ArrayList<TeamExternalGroup>());

    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(detailTeamController, joinTeamRequestService,
        JoinTeamRequestService.class);
    autoWireMock(detailTeamController, teamExternalGroupDao, TeamExternalGroupDao.class);
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

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findAdmins(mockTeam)).thenReturn(admins);

    JoinTeamRequestService joinTeamRequestService = mock(JoinTeamRequestService.class);
    when(joinTeamRequestService.findPendingRequests(mockTeam)).thenReturn(
        Collections.EMPTY_LIST);

    TeamExternalGroupDao teamExternalGroupDao = mock(TeamExternalGroupDao.class);
    when(teamExternalGroupDao.getByTeamIdentifier("team-1")).thenReturn(new ArrayList<TeamExternalGroup>());

    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(detailTeamController, joinTeamRequestService,
        JoinTeamRequestService.class);
    autoWireMock(detailTeamController, teamExternalGroupDao, TeamExternalGroupDao.class);

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

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findAdmins(mockTeam)).thenReturn(admins);

    JoinTeamRequestService joinTeamRequestService = mock(JoinTeamRequestService.class);
    when(joinTeamRequestService.findPendingRequests(mockTeam)).thenReturn(
        Collections.EMPTY_LIST);

    TeamExternalGroupDao teamExternalGroupDao = mock(TeamExternalGroupDao.class);
    when(teamExternalGroupDao.getByTeamIdentifier("team-1")).thenReturn(new ArrayList<TeamExternalGroup>());

    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(detailTeamController, joinTeamRequestService,
        JoinTeamRequestService.class);
    autoWireMock(detailTeamController, teamExternalGroupDao, TeamExternalGroupDao.class);

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
    String token = TokenUtil.generateSessionToken();
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

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findAdmins(mockTeam)).thenReturn(admins);

    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController
        .leaveTeam(getModelMap(), request, token, token, new SimpleSessionStatus());

    assertEquals("home.shtml?teams=my&view=app", result.getUrl());
  }

  @Test
  public void testLeaveTeam() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
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

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findAdmins(mockTeam)).thenReturn(admins);

    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController
        .leaveTeam(getModelMap(), request, token, token, new SimpleSessionStatus());

    assertEquals(
        "detailteam.shtml?team=team-1&view=app&mes=error.AdminCannotLeaveTeam",
        result.getUrl());
  }

  @Test
  public void testDeleteTeamHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // add team
    request.setParameter("team", "team-1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Manager);
    roles.add(Role.Member);
    roles.add(Role.Admin);

    Member member = new Member(roles, "John Doe", "member-1", "john@doe.com");
      
    Team team = mock(Team.class);
    team.setName("team-1");

    TeamInviteService teamInviteService = mock(TeamInviteService.class);
    when(teamInviteService.findAllInvitationsForTeam(team)).thenReturn(Collections.<Invitation>emptyList());

    autoWireMock(detailTeamController, teamInviteService, TeamInviteService.class);

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findMember("team-1", "member-1")).thenReturn(member);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(team);
      
    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);

    TeamExternalGroupDao teamExternalGroupDao = mock(TeamExternalGroupDao.class);
    when(teamExternalGroupDao.getByTeamIdentifier("team-1")).thenReturn(Collections.<TeamExternalGroup>emptyList());
    autoWireMock(detailTeamController, teamExternalGroupDao, TeamExternalGroupDao.class);

    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController.deleteTeam(getModelMap(), 
            request, token, token, new SimpleSessionStatus());

    assertEquals("home.shtml?teams=my&view=app", result.getUrl());
  }

  @Test
  public void testDeleteTeam() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // add team
    request.setParameter("team", "team-1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);

    Member member = new Member(roles, "John Doe", "member-1", "john@doe.com");

    autoWireMock(detailTeamController, new Returns(member), GrouperTeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController.deleteTeam(getModelMap(),
        request, token, token, new SimpleSessionStatus());

    assertEquals("detailteam.shtml?team=team-1&view=app", result.getUrl());
  }

  @Test(expected = RuntimeException.class)
  public void testDeleteTeamException() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // do NOT add the team

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);

    Member member = new Member(roles, "John Doe", "member-1", "john@doe.com");

    autoWireMock(detailTeamController, new Returns(member), GrouperTeamService.class);
    autoWireRemainingResources(detailTeamController);

    detailTeamController.deleteTeam(getModelMap(), request, token, token, new SimpleSessionStatus());
  }

  @Test
  public void testDeleteMemberHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // add the team & member
    request.addParameter("team", "team-1");
    request.addParameter("member", "member-2");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Manager);
    roles.add(Role.Member);
    roles.add(Role.Admin);

    Member owner = new Member(roles, "John Doe", "member-1", "john@doe.com");
    Member member = new Member(roles, "Jane Doe", "member-2", "jane@doe.com");

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findMember("team-1", "member-2")).thenReturn(member);
    when(grouperTeamService.findMember("team-1", "member-1")).thenReturn(owner);

    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController.deleteMember(getModelMap(),
        request, token, token, new SimpleSessionStatus());

    assertEquals("detailteam.shtml?team=team-1&view=app", result.getUrl());
  }

  @Test
  public void testDeleteMember() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // add the team & member
    request.addParameter("team", "team-1");
    request.addParameter("member", "member-1");

    Member member = getMember();

    autoWireMock(detailTeamController, new Returns(member), GrouperTeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController.deleteMember(getModelMap(),
        request, token, token, new SimpleSessionStatus());

    assertEquals(
        "detailteam.shtml?team=team-1&mes=error.NotAuthorizedToDeleteMember&view=app",
        result.getUrl());
  }

  @Test(expected = RuntimeException.class)
  public void testDeleteMemberException() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // do NOT add the team & member

    Member member = getMember();

    autoWireMock(detailTeamController, new Returns(member), GrouperTeamService.class);
    autoWireRemainingResources(detailTeamController);

    detailTeamController.deleteMember(getModelMap(), request, token, token, new SimpleSessionStatus());
  }

  @Test
  public void testAddRoleHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    request.addParameter("teamId", "team-1");
    request.addParameter("memberId", "member-1");
    request.addParameter("roleId", Role.Manager.toString());
    request.addParameter("doAction", "add");

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    Member member = getMember();
    when(grouperTeamService.findMember("team-1", "member-1")).thenReturn(member);
    when(grouperTeamService.addMemberRole("team-1", "member-1", Role.Manager, "member-1"))
        .thenReturn(true);
    autoWireMock(detailTeamController, new Returns(true), ControllerUtil.class);
    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView view = detailTeamController.addOrRemoveRole(getModelMap(),
        request, token, token, new SimpleSessionStatus());
    assertEquals(
        "detailteam.shtml?team=team-1&view=app&mes=role.added&offset=0",
        view.getUrl());
  }

  @Test
  public void testAddRoleNotAuthorized() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // Add the team, member & role
    request.addParameter("teamId", "team-1");
    request.addParameter("memberId", "member-1");
    request.addParameter("roleId", Role.Manager.toString());
    request.addParameter("doAction", "add");

    autoWireMock(detailTeamController, new Returns(false), GrouperTeamService.class);
    autoWireRemainingResources(detailTeamController);

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    Member member = getMember();
    when(grouperTeamService.findMember("team-1", "member-1")).thenReturn(member);
    when(grouperTeamService.addMemberRole("team-1", "member-1", Role.Manager, "member-1"))
        .thenReturn(false);
    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(detailTeamController, new Returns(false), ControllerUtil.class);

    RedirectView view = detailTeamController.addOrRemoveRole(getModelMap(),
        request, token, token, new SimpleSessionStatus());
    assertEquals(
        "detailteam.shtml?team=team-1&view=app&mes=no.role.added&offset=0",
        view.getUrl());
  }

  @Test
  public void testRemoveRoleHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
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

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findAdmins(mockTeam)).thenReturn(admins);
    when(
        grouperTeamService.removeMemberRole("team-1", "member-1", Role.Manager, "member-1"))
        .thenReturn(true);

    autoWireMock(detailTeamController, new Returns(true), ControllerUtil.class);
    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView view = detailTeamController.addOrRemoveRole(getModelMap(),
        request, token, token, new SimpleSessionStatus());

    assertEquals(
        "detailteam.shtml?team=team-1&view=app&mes=role.removed&offset=0",
        view.getUrl());
  }

  @Test
  public void testRemoveRoleOneAdmin() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
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

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findAdmins(mockTeam)).thenReturn(admins);
    when(grouperTeamService.removeMemberRole("team-1", "member-1", Role.Admin, "member-1"))
        .thenReturn(false);

    autoWireMock(detailTeamController, new Returns(true), ControllerUtil.class);
    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView view = detailTeamController.addOrRemoveRole(getModelMap(),
        request, token, token, new SimpleSessionStatus());

    assertEquals("detailteam.shtml?team=team-1&view=app&mes=no.role.added.admin.status&offset=0", view.getUrl());

  }

  @Test
  public void testRemoveRoleException() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // do NOT add the team, member & role

    autoWireRemainingResources(detailTeamController);

    RedirectView view = detailTeamController.addOrRemoveRole(getModelMap(),
        request, token, token, new SimpleSessionStatus());

    assertEquals("home.shtml?teams=my&view=app", view.getUrl());
  }

  @Test(expected = RuntimeException.class)
  public void testApproveRequestNoPendingRequest() throws Exception {
    RequestContextHolder.setRequestAttributes(getRequestAttributes(), true);
    
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    // Add the team, member & role
    request.addParameter("team", "team-1");
    request.addParameter("member", "potential-member-1");
    request.addParameter("role", "0");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);
    roles.add(Role.Manager);
    roles.add(Role.Admin);

    List<Member> members = new ArrayList<Member>();
    Member loggedInMember = new Member(roles, "Jane Doe", "member-1",
        "jane@doe.com");
    members.add(loggedInMember);

    Person loggedInPerson = mock(Person.class);
    when(loggedInPerson.getId()).thenReturn("member-1");

    Person memberToAdd = mock(Person.class);
    when(memberToAdd.getId()).thenReturn("potential-member-1");

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);
    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(grouperTeamService.findMember("team-1", "member-1")).thenReturn(
        loggedInMember);

    JoinTeamRequestService joinTeamRequestService = mock(JoinTeamRequestService.class);
    when(joinTeamRequestService.findPendingRequest(memberToAdd, mockTeam))
        .thenReturn(null);

    PersonService personService = mock(PersonService.class);
    when(personService.getPerson("member-2",null)).thenReturn(loggedInPerson);
    when(personService.getPerson("potential-member-1",null)).thenReturn(
            memberToAdd);

    autoWireMock(detailTeamController, new Returns(true), ControllerUtil.class);
    autoWireMock(detailTeamController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(detailTeamController, personService,
        PersonService.class);
    autoWireMock(detailTeamController, joinTeamRequestService,
        JoinTeamRequestService.class);
    autoWireRemainingResources(detailTeamController);

    RedirectView result = detailTeamController.deleteRequest(request,
            getModelMap(), token, token, new SimpleSessionStatus());
    assertEquals("detailteam.shtml?team=team-1&view=app", result.getUrl());
  }

  @Override
  public void setup() throws Exception {
    super.setup();
  }
  
  /**
   * @return
   */
  private RequestAttributes getRequestAttributes() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    Person person = new Person();
    person.setField("id","test");
    session.setAttribute(LoginInterceptor.PERSON_SESSION_KEY, person);
    request.setSession(session);
    return new ServletRequestAttributes(request);
  }
}
