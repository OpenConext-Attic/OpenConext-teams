/**
 * 
 */
package nl.surfnet.coin.teams.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.JoinTeamRequestService;
import nl.surfnet.coin.teams.service.TeamService;

/**
 * @author steinwelberg
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

    Set<Member> members = new HashSet<Member>();
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");
    Member[] membersResult = team.getMembers().toArray(new Member[] {});
    Member member = membersResult[0];

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("team description", team.getDescription());
    
    assertEquals(1, membersResult.length);
    assertEquals("Jane Doe", member.getName());
    assertEquals("jane@doe.com", member.getEmail());
    assertEquals("member-2", member.getId());
    assertTrue(member.getRoles().contains(Role.Member));
    assertFalse(member.getRoles().contains(Role.Admin));
    assertFalse(member.getRoles().contains(Role.Manager));
    assertEquals("detailteam-not-member", result);
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

    Set<Member> members = new HashSet<Member>();
    members.add(new Member(roles, "John Doe", "member-1", "john@doe.com"));
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    JoinTeamRequestService joinTeamRequestService = mock(JoinTeamRequestService.class);
    when(joinTeamRequestService.findPendingRequests(mockTeam)).thenReturn(Collections.EMPTY_LIST);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireMock(detailTeamController, joinTeamRequestService, JoinTeamRequestService.class);
    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("team description", team.getDescription());
    assertEquals("detailteam-member", result);
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

    Set<Member> members = new HashSet<Member>();
    members.add(new Member(roles, "John Doe", "member-1", "john@doe.com"));
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    JoinTeamRequestService joinTeamRequestService = mock(JoinTeamRequestService.class);
    when(joinTeamRequestService.findPendingRequests(mockTeam)).thenReturn(Collections.EMPTY_LIST);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireMock(detailTeamController, joinTeamRequestService, JoinTeamRequestService.class);

    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("team description", team.getDescription());
    assertEquals("detailteam-manager", result);
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

    Set<Member> members = new HashSet<Member>();
    members.add(new Member(roles, "John Doe", "member-1", "john@doe.com"));
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    JoinTeamRequestService joinTeamRequestService = mock(JoinTeamRequestService.class);
    when(joinTeamRequestService.findPendingRequests(mockTeam)).thenReturn(Collections.EMPTY_LIST);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireMock(detailTeamController, joinTeamRequestService, JoinTeamRequestService.class);

    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");
    int onlyAdmin = (Integer) getModelMap().get("onlyAdmin");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("team description", team.getDescription());
    assertEquals(1, onlyAdmin);
    assertEquals("detailteam-admin", result);
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

    Set<Member> members = new HashSet<Member>();
    members.add(new Member(roles, "John Doe", "member-1", "john@doe.com"));
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);
    
    RedirectView result = detailTeamController.leaveTeam(getModelMap(), request);
    
    assertEquals("home.shtml?teams=my", result.getUrl());
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

    Set<Member> members = new HashSet<Member>();
    members.add(new Member(roles, "John Doe", "member-1", "john@doe.com"));
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);
    
    RedirectView result = detailTeamController.leaveTeam(getModelMap(), request);
    
    assertEquals("detailteam.shtml?team=team-1&mes=error.AdminCannotLeaveTeam", result.getUrl());
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
    
    RedirectView result = detailTeamController.deleteTeam(getModelMap(), request);
    
    assertEquals("home.shtml?teams=my", result.getUrl());
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
    
    RedirectView result = detailTeamController.deleteTeam(getModelMap(), request);
    
    assertEquals("detailteam.shtml?team=team-1", result.getUrl());
  }
  
  @Test (expected=RuntimeException.class)
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
    
    RedirectView result = detailTeamController.deleteMember(getModelMap(), request);
    
    assertEquals("detailteam.shtml?team=team-1", result.getUrl());
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
    
    RedirectView result = detailTeamController.deleteMember(getModelMap(), request);
    
    assertEquals("detailteam.shtml?team=team-1&mes=error.NotAuthorizedToDeleteMember", result.getUrl());
  }
  
  @Test (expected=RuntimeException.class)
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
    request.addParameter("team", "team-1");
    request.addParameter("member", "member-1");
    request.addParameter("role", Role.Manager.toString());

    autoWireMock(detailTeamController, new Returns(true), TeamService.class);
    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.addRole(getModelMap(), request);
    
    assertEquals("success", result);
  }
  
  @Test
  public void testAddRoleNotAuthorized() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the team, member & role
    request.addParameter("team", "team-1");
    request.addParameter("member", "member-1");
    request.addParameter("role", Role.Manager.toString());

    autoWireMock(detailTeamController, new Returns(false), TeamService.class);
    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.addRole(getModelMap(), request);
    
    assertEquals("error", result);
  }
  
  @Test
  public void testAddRoleException() throws Exception {
    MockHttpServletRequest request = getRequest();
    // do NOT add the team, member & role
    
    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.addRole(getModelMap(), request);
    
    assertEquals("error", result);
  }
  
  @Test
  public void testRemoveRoleHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    request.addParameter("team", "team-1");
    request.addParameter("member", "member-1");
    request.addParameter("role", "1");

    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);
    roles.add(Role.Manager);
    roles.add(Role.Admin);

    HashSet<Member> admins = new HashSet<Member>();
    admins.add(new Member(new HashSet<Role>(), "Jane Doe", "member-2",
        "jane@doe.com"));

    Set<Member> members = new HashSet<Member>();
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);
    when(teamService.removeMemberRole("team-1", "member-1", Role.Manager, false)).thenReturn(true);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.removeRole(getModelMap(), request);
    
    assertEquals("success", result);
  }
  
  @Test
  public void testRemoveRoleOneAdmin() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the team, member & role
    request.addParameter("team", "team-1");
    request.addParameter("member", "member-1");
    request.addParameter("role", "0");
    
    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);
    roles.add(Role.Manager);
    roles.add(Role.Admin);

    HashSet<Member> admins = new HashSet<Member>();
    admins.add(new Member(new HashSet<Role>(), "Jane Doe", "member-2",
        "jane@doe.com"));

    Set<Member> members = new HashSet<Member>();
    members.add(new Member(roles, "Jane Doe", "member-2", "jane@doe.com"));

    Team mockTeam = new Team("team-1", "Team 1", "team description", members);

    TeamService teamService = mock(TeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(mockTeam);
    when(teamService.findAdmins(mockTeam)).thenReturn(admins);
    when(teamService.removeMemberRole("team-1", "member-1", Role.Admin, false)).thenReturn(false);

    autoWireMock(detailTeamController, teamService, TeamService.class);
    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.removeRole(getModelMap(), request);
    
    assertEquals("onlyOneAdmin", result);
  }
  
  @Test
  public void testRemoveRoleException() throws Exception {
    MockHttpServletRequest request = getRequest();
    // do NOT add the team, member & role
    
    autoWireRemainingResources(detailTeamController);

    String result = detailTeamController.removeRole(getModelMap(), request);
    
    assertEquals("error", result);
  }

}
