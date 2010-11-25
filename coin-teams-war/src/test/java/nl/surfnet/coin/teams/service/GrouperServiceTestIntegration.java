/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.impl.GrouperTeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

/**
 * 
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:coin-teams-context.xml",
    "classpath:coin-teams-properties-integration-test-context.xml" })
public class GrouperServiceTestIntegration {
  
//  private GrouperTeamService teamService = new GrouperTeamService();

  @Autowired
  private GrouperTeamService teamService;
  
  @Autowired
  private TeamEnvironment environment;
  
  // Test team with one member (test-member)
  private String teamId = "test:integration-test-team";
  private String displayName = "Integration Test Team";
  private String description = "Integration Test Team Description";
  
  // Test team with two members (test-member, test-member2)
  private String teamId2 = "test:integration-test-team2";
  private String displayName2 = "Integration Test Team 2";
  private String description2 = "Integration Test Team 2 Description";
  
  private String nonExistentTeam = "team-bestaat-niet";
  
  private String memberId = "urn:collab:person:surfnet.nl:hansz";
  private String memberId2 = "urn:collab:person:test.surfguest.nl:steinwelberg";
  
  private String nonExistentMember = "member-bestaat-niet"; 

  private String clientVersion = "v1_6_000";

//  @Test
//  public void testNativeClient() {
//    GcGetGroups getGroups = new GcGetGroups();
//    getGroups.assignClientVersion(clientVersion);
//    String subjectId = "urn:collab:person:surfnet.nl:hansz";
//    getGroups.addSubjectId(subjectId);
//    WsSubjectLookup theActAsSubject = new WsSubjectLookup();
//    theActAsSubject.setSubjectId(subjectId);
//    getGroups.assignActAsSubject(theActAsSubject);
//    edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults result = getGroups
//        .execute();
//    WsGetGroupsResult[] results = result.getResults();
//    assertEquals(1, results.length);
//
//    GcGetMemberships gcMmemberships = new GcGetMemberships();
//    gcMmemberships.assignActAsSubject(theActAsSubject);
//    gcMmemberships.assignIncludeSubjectDetail(Boolean.TRUE);
//    gcMmemberships.addGroupName("test:3tu_identity_management");
//    WsMembership[] wsMembers = gcMmemberships.execute().getWsMemberships();
//
//    GcGetMembers getMember = new GcGetMembers();
//    // getMember.addSourceId(subjectId);
//    getMember.assignActAsSubject(theActAsSubject);
//    getMember.assignIncludeSubjectDetail(Boolean.TRUE);
//    getMember.assignIncludeGroupDetail(Boolean.TRUE);
//    String uuid = results[0].getWsGroups()[1].getUuid();
//    getMember.addGroupName("test:3tu_identity_management");
//    WsGetMembersResults execute = getMember.execute();
//    WsGetMembersResult[] results2 = execute.getResults();
//
//    GcFindGroups findGroups = new GcFindGroups();
//    findGroups.assignActAsSubject(theActAsSubject);
//    findGroups.assignClientVersion(clientVersion);
//    findGroups.assignIncludeGroupDetail(Boolean.TRUE);
//
//    WsQueryFilter queryFilter = new WsQueryFilter();
//    queryFilter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
//    queryFilter.setGroupName("W");
//    // queryFilter.setQueryTerm("W");
//    findGroups.assignQueryFilter(queryFilter);
//    WsFindGroupsResults findResults = findGroups.execute();
//    WsGroup[] groupResults = findResults.getGroupResults();
//
//    GcGetMemberships memberships = new GcGetMemberships();
//    memberships.assignActAsSubject(theActAsSubject);
//    memberships.assignClientVersion(clientVersion);
//    memberships.assignIncludeGroupDetail(Boolean.TRUE);
//    memberships.assignIncludeSubjectDetail(Boolean.TRUE);
//    // memberships.addWsSubjectLookup(theActAsSubject);
//    memberships.addGroupName("test:3tu_identity_management");
//    WsGetMembershipsResults execute2 = memberships.execute();
//    WsMembership[] wsMemberships = execute2.getWsMemberships();
//
//    GcGetSubjects getSubject = new GcGetSubjects();
//    getSubject.assignActAsSubject(theActAsSubject);
//    getSubject.assignClientVersion(clientVersion);
//    getSubject.assignIncludeSubjectDetail(Boolean.TRUE);
//    getSubject.assignSearchString("H");
//    getSubject.addSubjectAttributeName("mail");
//    getSubject.addSubjectAttributeName("displayName");
//    WsGetSubjectsResults execute3 = getSubject.execute();
//    WsSubject[] wsSubjects = execute3.getWsSubjects();
//
//    GcGetPermissionAssignments permissions = new GcGetPermissionAssignments();
//    permissions.assignActAsSubject(theActAsSubject);
//    permissions.assignClientVersion(clientVersion);
//    permissions.assignIncludePermissionAssignDetail(true);
//    permissions.addSubjectLookup(theActAsSubject);
//    WsGetPermissionAssignmentsResults execute4 = permissions.execute();
//    WsPermissionAssign[] wsPermissionAssigns = execute4
//        .getWsPermissionAssigns();
//
//    GcGetGrouperPrivilegesLite privileges = new GcGetGrouperPrivilegesLite();
//    privileges.assignActAsSubject(theActAsSubject);
//    privileges.assignClientVersion(clientVersion);
//    privileges.assignGroupName("test:3tu_identity_management");
//    // privileges.
//    // privileges.assignSubjectLookup(theActAsSubject);
//    WsGetGrouperPrivilegesLiteResult execute5 = privileges.execute();
//    WsGrouperPrivilegeResult[] privilegeResults = execute5
//        .getPrivilegeResults();
//
//    GcFindStems stems = new GcFindStems();
//    stems.assignActAsSubject(theActAsSubject);
//    stems.assignClientVersion(clientVersion);
//    WsStemQueryFilter theStemQueryFilter = new WsStemQueryFilter();
//    theStemQueryFilter.setStemQueryFilterType("FIND_BY_STEM_NAME_APPROXIMATE");
//    theStemQueryFilter.setStemName("E");
//    stems.assignStemQueryFilter(theStemQueryFilter);
//    WsFindStemsResults execute6 = stems.execute();
//    WsStem[] stemResults = execute6.getStemResults();
//  }
  
  @Test
  public void testAddTeam() {
    teamService.addTeam("add-team", "Add Team", "Add team description");
    assertEquals("test:add-team", teamService.findTeamById("test:add-team").getId());
  }
  
  @Test (expected=GcWebServiceError.class)
  public void testAddDuplicateTeam() {
    teamService.addTeam("add-team", "Add Team", "Add team description");    
  }

  @Test
  public void testFindAllTeams() {
    List<Team> teams = teamService.findAllTeams();
    assertEquals(true, teams.size() > 0);
  }
  
  @Test
  public void testFindTeamById() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    // Team test-team bestaat al in Grouper
    Team team = teamService.findTeamById(teamId);
    assertEquals(teamId, team.getId());
  }
  
  @Test (expected=RuntimeException.class)
  public void testFindTeamByIdUnexistentTeam() {
    teamService.findTeamById(nonExistentTeam);
  }
  
  @Test
  public void testFindTeams() {
    // Existing team
    List<Team> teams = teamService.findTeams(displayName2);
    assertEquals(1, teams.size());
    
    // Non existing team
    teams = teamService.findTeams(nonExistentTeam);
    assertEquals(0, teams.size());
  }
  
  @Test
  public void testGetTeamsByMember() throws Exception {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    List<Team> teams = teamService.getTeamsByMember(memberId);
    assertEquals(true, teams.size() > 0);
  }
    
  @Test (expected=RuntimeException.class)
  public void testGetTeamsByMemberNonexistentMember() {
    teamService.getTeamsByMember(nonExistentMember);
  }
  
  @Test
  public void testFindMember() {
    Member member = teamService.findMember(teamId, memberId);
    assertEquals(memberId,member.getId());
  }
  
  @Test (expected=RuntimeException.class)
  public void testFindMemberNotInTeam() {
    teamService.findMember(teamId, memberId2);
  }
  
  @Test (expected=RuntimeException.class)
  public void testFindMemberNonexistentMember() {
    Member member = teamService.findMember(teamId, nonExistentMember);
  }
  
  @Test
  public void testAddMember() {
    teamService.addMember(teamId, memberId2);
    assertEquals(memberId2, teamService.findMember(teamId, memberId2).getId());
  }
  
  @Test (expected=GcWebServiceError.class)
  public void testAddMemberNonexistentMember() {
    // add non existent member
    teamService.addMember(teamId, nonExistentMember);
  }
  
  @Test (expected=RuntimeException.class)
  public void testDeleteMember() {
    // Delete the member added in the testAddMember test.
    teamService.deleteMember(teamId, memberId2);
    teamService.findMember(teamId, memberId2);
  }
  
  @Test
  public void testDeleteTeam() {
    // Delete the team that was added in testAddTeam
    teamService.deleteTeam("test:add-team");
    assertEquals(0,teamService.findTeams("Add Team").size());
  }
  
  @Test
  public void testUpdateTeam() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    teamService.addTeam("update-team", "Update Team", "Update team description");
    teamService.addMember("test:update-team", memberId);
    teamService.addMemberRole("test:update-team", memberId, Role.Admin, true);
    teamService.updateTeam("test:update-team", "New Name", "New Description");
    
    Team team = teamService.findTeamById("test:update-team");
    assertEquals("New Name", team.getName());
    assertEquals("New Description", team.getDescription());
    
    // Clean up
    teamService.deleteTeam("test:update-team");
  }
  
  @Test (expected=GcWebServiceError.class)
  public void testUpdateNonExistingTeam() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    teamService.updateTeam("test:update-team2", "New Name", "New Description");
  }
  
  @Test 
  public void testSetVisibilityGroup() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    teamService.setVisibilityGroup(teamId, false);
    Team team = teamService.findTeamById(teamId);
    assertEquals(false, team.isViewable());
    
    teamService.setVisibilityGroup(teamId, true);
    Team team2 = teamService.findTeamById(teamId);
    assertEquals(true, team2.isViewable());
  }
  
  @Test (expected=GcWebServiceError.class)
  public void testAddMemberRoleToNonExistingMember() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    teamService.addMemberRole(teamId, nonExistentMember, Role.Admin, false);
  }
  
  @Test (expected=GcWebServiceError.class)
  public void testAddMemberRoleToMemberWithoutCorrectPermissions() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    teamService.addMember(teamId, memberId2);
    
    LoginInterceptor.setLoggedInUser(memberId2);
   teamService.addMemberRole(teamId, memberId2, Role.Admin, false);
  }
  
  @Test
  public void testAddMemberRole() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    teamService.addMember(teamId, memberId2);
    Boolean result = teamService.addMemberRole(teamId, memberId2, Role.Admin, false);
    Member member = teamService.findMember(teamId, memberId2);
    assertEquals(true, member.getRoles().contains(Role.Admin));
    assertEquals(true, result);
  }
  
  @Test
  public void testRemoveMemberRole() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());

    Boolean result = teamService.removeMemberRole(teamId, memberId2, Role.Admin, false);
    Member member = teamService.findMember(teamId, memberId2);
    assertEquals(false, member.getRoles().contains(Role.Admin));
    assertEquals(true, result);
  }
  
  @Test (expected=GcWebServiceError.class)
  public void testRemoveMemberRoleNonExistingMember() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    teamService.removeMemberRole(teamId, nonExistentMember, Role.Admin, false);
  }
  
  @Test
  public void testRemoveRoleThatMemberDoesNotHave() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    Boolean result = teamService.removeMemberRole(teamId, memberId2, Role.Admin, false);
    assertEquals(true, result);
        
    // Clean up
    teamService.deleteMember(teamId, memberId2);
  }
  
  @Test
  public void testFindMemberByTeam() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    assertEquals(memberId, teamService.findMember(teamService.findTeamById(teamId), memberId).getId());
  }
  
  @Test (expected=RuntimeException.class)
  public void testFindNonExistingMemberByTeam() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    teamService.findMember(teamService.findTeamById(teamId), nonExistentMember).getId();
  }
  
  @Test (expected=RuntimeException.class)
  public void testFindNonMemberByTeam() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    teamService.findMember(teamService.findTeamById(teamId), memberId2).getId();
  }
  
  @Test
  public void testFindAdmins() {
    LoginInterceptor.setLoggedInUser(environment.getMockLogin());
    assertEquals(1, teamService.findAdmins(teamService.findTeamById(teamId)).size()); 
  }
  
  // //https://spaces.internet2.edu/display/GrouperWG/Grouper+Web+Services

}
