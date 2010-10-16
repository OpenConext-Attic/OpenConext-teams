/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import nl.surfnet.coin.teams.util.TempLoginInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGetMemberships;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembership;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

/**
 * {@link TeamService} using Grouper LDAP as persistent store
 * 
 */
@Component("teamService")
public class GrouperTeamService implements TeamService {

  @Autowired
  TeamEnvironment environment;

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#findAllTeams()
   */
  @Override
  public List<Team> findAllTeams() {
    throw new RuntimeException("not implemented yet");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.TeamService#findTeamById(java.lang.String)
   */
  @Override
  public Team findTeamById(String teamId) {
    GcFindGroups findGroups = new GcFindGroups();
    WsSubjectLookup actAsSubject = getActAsSubject();
    findGroups.assignActAsSubject(actAsSubject);
    findGroups.assignIncludeGroupDetail(Boolean.TRUE);
    findGroups.addGroupName(teamId);
    WsFindGroupsResults findResults = findGroups.execute();
    WsGroup[] groupResults = findResults.getGroupResults();
    if (groupResults.length == 0) {
      throw new RuntimeException("No team found with Id('" + teamId + "')");
    }
    WsGroup wsGroup = groupResults[0];
    return new Team(wsGroup.getName(), wsGroup.getDisplayExtension(),
        wsGroup.getDescription(), getMembers(wsGroup.getName()));
  }
  

  /**
   * @return
   */
  private WsSubjectLookup getActAsSubject() {
    WsSubjectLookup actAsSubject = new WsSubjectLookup();
    actAsSubject.setSubjectId(TempLoginInterceptor.getLoggedInUser());
    return actAsSubject;
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#findTeams(java.lang.String)
   */
  @Override
  public List<Team> findTeams(String partOfTeamName) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.TeamService#getTeamsByPerson(java.lang.String
   * )
   */
  @Override
  public List<Team> getTeamsByMember(String memberId) {
    // TODO Auto-generated method stub
    return null;
  }

  private Set<Member> getMembers(String teamId) {
    GcGetMemberships memberships = new GcGetMemberships();
    memberships.assignActAsSubject(getActAsSubject());
    memberships.assignIncludeSubjectDetail(Boolean.TRUE);
    memberships.addGroupName(teamId);
    WsMembership[] wsMembers = memberships.execute().getWsMemberships();

    GcGetMembers getMember = new GcGetMembers();
    // getMember.addSourceId(subjectId);
    getMember.assignActAsSubject(getActAsSubject());
    getMember.assignIncludeSubjectDetail(Boolean.TRUE);
    getMember.addGroupName(teamId);
    WsGetMembersResult[] getMembers = getMember.execute().getResults();
    Set<Member> members = new HashSet<Member>();
    return members;    
  }

  private void addRolesToMembers(Set<Member> members, String teamId) {
    GcGetGrouperPrivilegesLite privileges = new GcGetGrouperPrivilegesLite();
    privileges.assignActAsSubject(getActAsSubject());
    privileges.assignGroupName(teamId);
    WsGrouperPrivilegeResult[] privilegeResults = privileges.execute().getPrivilegeResults();
    for (Member member : members) {
      String id = member.getId();
      List<WsGrouperPrivilegeResult> memberPrivs = getPrivilegeResultsForMember(id,privilegeResults);
      if (!memberPrivs.isEmpty()) {
        for (WsGrouperPrivilegeResult priv : memberPrivs) {
          member.addRole(getRole(priv.getPrivilegeName()));
        }
        
      }
    }
  }

  /**
   * @param privilegeName
   * @return
   */
  private Role getRole(String privilegeName) {
    // TODO Auto-generated method stub
    return null;
  }

  private List<WsGrouperPrivilegeResult> getPrivilegeResultsForMember(
      String id, WsGrouperPrivilegeResult[] privilegeResults) {
    List<WsGrouperPrivilegeResult> result = new ArrayList<WsGrouperPrivilegeResult>();
    for (WsGrouperPrivilegeResult privilege : privilegeResults) {
      if (privilege.getOwnerSubject().getName().equals(id)) {
        result.add(privilege);
      }
    }
    return result;
  }

  /* (non-Javadoc)
   * @see nl.surfnet.coin.teams.service.TeamService#addMember(java.lang.String, java.lang.String)
   */
  @Override
  public void addMember(String teamId, String personId) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see nl.surfnet.coin.teams.service.TeamService#addTeam(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void addTeam(String teamId, String displayName, String teamDescription) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see nl.surfnet.coin.teams.service.TeamService#deleteMember(java.lang.String, java.lang.String)
   */
  @Override
  public void deleteMember(String teamId, String personId) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see nl.surfnet.coin.teams.service.TeamService#deleteTeam(java.lang.String)
   */
  @Override
  public void deleteTeam(String teamId) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see nl.surfnet.coin.teams.service.TeamService#updateMember(java.lang.String, java.lang.String, nl.surfnet.coin.teams.domain.Role)
   */
  @Override
  public void updateMember(String teamId, String personId, Role role) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see nl.surfnet.coin.teams.service.TeamService#updateTeam(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void updateTeam(String teamId, String displayName,
      String teamDescription) {
    // TODO Auto-generated method stub
    
  }

}
