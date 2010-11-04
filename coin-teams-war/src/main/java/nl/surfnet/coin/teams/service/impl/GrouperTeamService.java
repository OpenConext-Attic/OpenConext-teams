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
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivileges;
import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGroupDelete;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

/**
 * {@link TeamService} using Grouper LDAP as persistent store
 * 
 */
public class GrouperTeamService implements TeamService {

  @Autowired
  private TeamEnvironment environment;

  private static String[] FORBIDDEN_CHARS = new String[] { "<", ">", "/", "\\",
      "*", ":" };

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#findAllTeams()
   */
  @Override
  public List<Team> findAllTeams() {
    GcFindGroups findGroups = new GcFindGroups();
    findGroups.assignActAsSubject(getActAsSubject());
    findGroups.assignIncludeGroupDetail(Boolean.TRUE);

    WsQueryFilter queryFilter = new WsQueryFilter();
    queryFilter.setQueryFilterType("FIND_BY_STEM_NAME");
    queryFilter.setStemName(environment.getDefaultStemName());
    findGroups.assignQueryFilter(queryFilter);
    WsFindGroupsResults findResults = findGroups.execute();
    WsGroup[] groupResults = findResults.getGroupResults();
    return convertWsGroupToTeam(groupResults);

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
    WsGrouperPrivilegeResult[] privilegeResults = getGroupPrivileges(wsGroup
        .getName());

    return new Team(wsGroup.getName(), wsGroup.getDisplayExtension(),
        wsGroup.getDescription(), getMembers(wsGroup.getName(),
            privilegeResults), getVisibilityGroup(wsGroup.getName(),
            privilegeResults));
  }

  /*
   * 
   */
  private WsSubjectLookup getActAsSubject() {
    return getActAsSubject(false);
  }

  /*
   * 
   */
  private WsSubjectLookup getActAsSubject(boolean powerUser) {
    WsSubjectLookup actAsSubject = new WsSubjectLookup();
    if (powerUser) {
      actAsSubject.setSubjectId(environment.getGrouperPowerUser());
    } else {
      actAsSubject.setSubjectId(LoginInterceptor.getLoggedInUser());
    }
    return actAsSubject;
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#findTeams(java.lang.String)
   */
  @Override
  public List<Team> findTeams(String partOfTeamName) {
    GcFindGroups findGroups = new GcFindGroups();
    findGroups.assignActAsSubject(getActAsSubject());
    findGroups.assignIncludeGroupDetail(Boolean.TRUE);

    WsQueryFilter queryFilter = new WsQueryFilter();
    queryFilter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
    queryFilter.setGroupName(partOfTeamName);
    findGroups.assignQueryFilter(queryFilter);
    WsFindGroupsResults findResults = findGroups.execute();
    WsGroup[] groupResults = findResults.getGroupResults();
    return convertWsGroupToTeam(groupResults);
  }

  private List<Team> convertWsGroupToTeam(WsGroup[] groupResults) {
    List<Team> result = new ArrayList<Team>();
    if (groupResults != null && groupResults.length > 0) {
      for (WsGroup wsGroup : groupResults) {
        WsGrouperPrivilegeResult[] privilegeResults = getGroupPrivileges(wsGroup
            .getName());
        Team team = new Team(wsGroup.getName(), wsGroup.getDisplayExtension(),
            wsGroup.getDescription(), getMembers(wsGroup.getName(),
                privilegeResults), getVisibilityGroup(wsGroup.getName(),
                privilegeResults));
        result.add(team);
      }
    }
    return result;
  }

  private WsGrouperPrivilegeResult[] getGroupPrivileges(String teamId) {
    GcGetGrouperPrivilegesLite privileges = new GcGetGrouperPrivilegesLite();
    privileges.assignActAsSubject(getActAsSubject(true));
    privileges.assignGroupName(teamId);
    WsGrouperPrivilegeResult[] privilegeResults = privileges.execute()
        .getPrivilegeResults();
    return privilegeResults;
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
    GcGetGroups getGroups = new GcGetGroups();
    getGroups.addSubjectId(memberId);
    getGroups.assignActAsSubject(getActAsSubject());
    WsGetGroupsResult[] groups = getGroups.execute().getResults();
    if (groups.length > 0) {
      WsGroup[] wsGroups = groups[0].getWsGroups();
      return convertWsGroupToTeam(wsGroups);
    }
    return new ArrayList<Team>();

  }

  private Set<Member> getMembers(String teamId,
      WsGrouperPrivilegeResult[] privilegeResults) {
    GcGetMembers getMember = new GcGetMembers();
    getMember.assignActAsSubject(getActAsSubject(true));
    getMember.assignIncludeSubjectDetail(Boolean.TRUE);
    getMember.addGroupName(teamId);
    getMember.addSubjectAttributeName("mail");
    getMember.addSubjectAttributeName("displayName");
    WsGetMembersResult[] getMembers = getMember.execute().getResults();
    Set<Member> members = new HashSet<Member>();
    if (getMembers[0].getWsSubjects() != null && getMembers[0].getWsSubjects().length > 0) {
      WsSubject[] subjects = getMembers[0].getWsSubjects();
      for (WsSubject wsSubject : subjects) {
        String id = wsSubject.getId();
        String mail = wsSubject.getAttributeValue(0);
        String displayName = wsSubject.getName();
        Member member = new Member(null, displayName, id, mail);
        members.add(member);
      }
    }
    addRolesToMembers(members, teamId, privilegeResults);
    return members;
  }

  private void addRolesToMembers(Set<Member> members, String teamId,
      WsGrouperPrivilegeResult[] privilegeResults) {
    if (privilegeResults != null) {
      for (Member member : members) {
        String id = member.getId();
        List<WsGrouperPrivilegeResult> memberPrivs = getPrivilegeResultsForMember(
            id, privilegeResults);
        if (!memberPrivs.isEmpty()) {
          for (WsGrouperPrivilegeResult priv : memberPrivs) {
            member.addRole(getRole(priv.getPrivilegeName()));
          }

        }
      }
    } else {
      throw new RuntimeException("group (id='" + teamId
          + "') has no privileges");
    }
  }

  /**
   * @param privilegeName
   * @return
   */
  private Role getRole(String privilegeName) {
    /*
     * De grouper rechten heten "admin" voor de group administrator, en "update"
     * voor de group manager.
     */
    if (privilegeName.equalsIgnoreCase("admin")) {
      return Role.Admin;
    } else if (privilegeName.equalsIgnoreCase("update")) {
      return Role.Manager;
    }
    return Role.Member;
  }

  private List<WsGrouperPrivilegeResult> getPrivilegeResultsForMember(
      String id, WsGrouperPrivilegeResult[] privilegeResults) {
    List<WsGrouperPrivilegeResult> result = new ArrayList<WsGrouperPrivilegeResult>();
    for (WsGrouperPrivilegeResult privilege : privilegeResults) {
      if (privilege.getOwnerSubject().getId().equals(id)) {
        result.add(privilege);
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#addTeam(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public String addTeam(String teamId, String displayName,
      String teamDescription) {
    if (!StringUtils.hasText(teamId)) {
      throw new IllegalArgumentException("teamId is not optional");
    }
    for (String ch : FORBIDDEN_CHARS) {
      teamId = teamId.replace(ch, "");
    }
    teamId = teamId.replace(" ", "_").toLowerCase();
    teamId = environment.getDefaultStemName() + ":" + teamId;
    GcGroupSave groupSave = new GcGroupSave();
    groupSave.assignActAsSubject(getActAsSubject(true));
    WsGroupToSave group = new WsGroupToSave();
    group.setSaveMode("INSERT");
    WsGroup wsGroup = new WsGroup();
    wsGroup.setDescription(teamDescription);
    wsGroup.setDisplayExtension(displayName);
    wsGroup.setName(teamId);
    group.setWsGroup(wsGroup);
    groupSave.addGroupToSave(group);
    groupSave.execute();
    return teamId;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.TeamService#deleteMember(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void deleteMember(String teamId, String personId) {

    Member member = findMember(teamId, personId);
    for (Role role : member.getRoles()) {
      removeMemberRole(teamId, personId, role, true);
    }

    GcDeleteMember deleteMember = new GcDeleteMember();
    deleteMember.addSubjectId(personId);
    deleteMember.assignActAsSubject(getActAsSubject(true));
    deleteMember.assignGroupName(teamId);
    deleteMember.execute();

  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#deleteTeam(java.lang.String)
   */
  @Override
  public void deleteTeam(String teamId) {
    GcGroupDelete groupDelete = new GcGroupDelete();
    groupDelete.assignActAsSubject(getActAsSubject(true));
    WsGroupLookup wsGroupLookup = new WsGroupLookup(teamId, null);
    groupDelete.addGroupLookup(wsGroupLookup);
    groupDelete.execute();

  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#updateTeam(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public void updateTeam(String teamId, String displayName,
      String teamDescription) {
    GcGroupSave groupSave = new GcGroupSave();
    groupSave.assignActAsSubject(getActAsSubject());
    WsGroupToSave group = new WsGroupToSave();
    group.setSaveMode("UPDATE");
    WsGroup wsGroup = new WsGroup();
    wsGroup.setDescription(teamDescription);
    wsGroup.setDisplayExtension(displayName);
    wsGroup.setName(teamId);
    group.setWsGroup(wsGroup);
    WsGroupLookup wsGroupLookup = new WsGroupLookup();
    wsGroupLookup.setGroupName(teamId);
    group.setWsGroupLookup(wsGroupLookup);
    groupSave.addGroupToSave(group);
    groupSave.execute();

  }

  @Override
  public void setVisibilityGroup(String teamId, boolean viewable) {
    GcAssignGrouperPrivilegesLite assignPrivilige = new GcAssignGrouperPrivilegesLite();
    assignPrivilige.assignActAsSubject(getActAsSubject(true));
    assignPrivilige.assignGroupName(teamId);
    //assignPrivilige.assignSubjectLookup(getActAsSubject(true));
    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
    wsSubjectLookup.setSubjectId("GrouperAll");
    assignPrivilige.assignSubjectLookup(wsSubjectLookup);
    assignPrivilige.assignPrivilegeType("access");
    assignPrivilige.assignPrivilegeName("view");
    assignPrivilige.addSubjectAttributeName("GrouperAll");

    assignPrivilige.assignAllowed(viewable);
    WsAssignGrouperPrivilegesLiteResult result = assignPrivilige.execute();

  }

  @Override
  public boolean addMemberRole(String teamId, String memberId, Role role,
      boolean addAsSuperUser) {
    GcAssignGrouperPrivileges assignPrivilige = new GcAssignGrouperPrivileges();
    assignPrivilige.assignActAsSubject(getActAsSubject(addAsSuperUser));
    assignPrivilige.assignGroupLookup(new WsGroupLookup(teamId, null));
    WsSubjectLookup subject = new WsSubjectLookup();
    subject.setSubjectId(memberId);
    assignPrivilige.addSubjectLookup(subject);
    assignPrivilige.assignPrivilegeType("access");
    switch (role) {
    case Admin: {
      assignPrivilige.addPrivilegeName("admin");
      assignPrivilige.addPrivilegeName("read");
      assignPrivilige.addPrivilegeName("optout");
      assignPrivilige.addPrivilegeName("update");
      break;
    }
    case Manager: {
      assignPrivilige.addPrivilegeName("update");
      assignPrivilige.addPrivilegeName("read");
      assignPrivilige.addPrivilegeName("optout");
      break;
    }
    case Member: {
      assignPrivilige.addPrivilegeName("read");
      assignPrivilige.addPrivilegeName("optout");
      break;
    }

    }
    assignPrivilige.assignAllowed(true);
    WsAssignGrouperPrivilegesResults result = assignPrivilige.execute();

    return result.getResultMetadata().getResultCode().equals("SUCCESS") ? true
        : false;
  }

  @Override
  public boolean removeMemberRole(String teamId, String memberId, Role role, boolean removeAsPowerUser) {
    GcAssignGrouperPrivileges assignPrivilige = new GcAssignGrouperPrivileges();
    assignPrivilige.assignActAsSubject(getActAsSubject(removeAsPowerUser));
    assignPrivilige.assignGroupLookup(new WsGroupLookup(teamId, null));
    WsSubjectLookup subject = new WsSubjectLookup();
    subject.setSubjectId(memberId);
    assignPrivilige.addSubjectLookup(subject);
    assignPrivilige.assignPrivilegeType("access");
    switch (role) {
    case Admin: {
      assignPrivilige.addPrivilegeName("admin");
      break;
    }
    case Manager: {
      assignPrivilige.addPrivilegeName("update");
      break;
    }
    case Member: {
      assignPrivilige.addPrivilegeName("read");
      assignPrivilige.addPrivilegeName("optout");
      break;
    }

    }
    assignPrivilige.assignAllowed(false);
    WsAssignGrouperPrivilegesResults result = assignPrivilige.execute();

    return result.getResultMetadata().getResultCode().equals("SUCCESS") ? true
        : false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#addMember(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void addMember(String teamId, String personId) {
    GcAddMember addMember = new GcAddMember();
    addMember.assignActAsSubject(getActAsSubject(true));
    addMember.assignGroupName(teamId);
    addMember.addSubjectId(personId);
    WsAddMemberResults execute = addMember.execute();
  }

  @Override
  public List<Team> findTeams(String partOfTeamName, String memberId) {
    List<Team> teamsByMember = getTeamsByMember(memberId);
    List<Team> result = new ArrayList<Team>();
    for (Team team : teamsByMember) {
      if (team.getName().toLowerCase().contains(partOfTeamName.toLowerCase())) {
        result.add(team);
      }
    }
    return result;
  }

  @Override
  public Member findMember(String teamId, String memberId) {
    Team team = findTeamById(teamId);
    Set<Member> members = team.getMembers();

    for (Member member : members) {
      if (member.getId().equals(memberId)) {
        return member;
      }
    }
    throw new RuntimeException("Member(id='" + memberId
        + "') is not a member of the given team");
  }

  @Override
  public Set<Member> findAdmins(Team team) {
    Set<Member> result = new HashSet<Member>();
    Set<Member> members = team.getMembers();

    for (Member member : members) {
      if (member.getRoles().contains(Role.Admin)) {
        result.add(member);
      }
    }

    return result;
  }

  private boolean getVisibilityGroup(String teamId,
      WsGrouperPrivilegeResult[] privilegeResults) {
    for (WsGrouperPrivilegeResult privilege : privilegeResults) {
      if (privilege.getWsGroup().getName().equals(teamId)) {
        if (privilege.getPrivilegeName().equals("view")
            && privilege.getPrivilegeType().equals("access")
            && privilege.getAllowed().equals("T")
            && privilege.getOwnerSubject().getId().equals("GrouperAll")) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Member findMember(Team team, String memberId) {
    Set<Member> members = team.getMembers();

    for (Member member : members) {
      if (member.getId().equals(memberId)) {
        return member;
      }
    }
    throw new RuntimeException("Member(id='" + memberId
        + "') is not a member of the given team");
  }

}
