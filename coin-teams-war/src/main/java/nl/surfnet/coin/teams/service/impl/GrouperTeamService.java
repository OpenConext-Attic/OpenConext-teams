/*
 * Copyright 2011 SURFnet bv
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
package nl.surfnet.coin.teams.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
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
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.domain.TeamResultWrapper;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperDao;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.DuplicateTeamException;
import nl.surfnet.coin.teams.util.TeamEnvironment;

/**
 * {@link TeamService} using Grouper LDAP as persistent store
 * 
 */
public class GrouperTeamService implements TeamService {

  @Autowired
  private TeamEnvironment environment;

  @Autowired
  private GrouperDao grouperDao;

  private static final String[] FORBIDDEN_CHARS = new String[] { "<", ">", "/", "\\",
      "*", ":", "," };

  private static final Logger LOGGER = LoggerFactory.getLogger(GrouperTeamService.class);

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
    if (groupResults == null || groupResults.length == 0) {
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


  private List<Team> convertWsGroupToTeam(WsGroup[] groupResults,
      boolean retrieveAll) {
    List<Team> result = new ArrayList<Team>();
    if (groupResults != null && groupResults.length > 0) {
      for (WsGroup wsGroup : groupResults) {
        WsGrouperPrivilegeResult[] privilegeResults = new WsGrouperPrivilegeResult[] {};
        String name = wsGroup.getName();
        if (retrieveAll) {
          privilegeResults = getGroupPrivileges(name);
        }
        String displayExtension = wsGroup.getDisplayExtension();
        String description = wsGroup.getDescription();
        List<Member> members = new ArrayList<Member>();
        if (retrieveAll) {
          members = getMembers(name, privilegeResults);
        }
        boolean visibilityGroup = getVisibilityGroup(name, privilegeResults);
        Team team = new Team(name, displayExtension, description, members,
            visibilityGroup);
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

  private List<Member> getMembers(String teamId,
                                  WsGrouperPrivilegeResult[] privilegeResults) {
    GcGetMembers getMember = new GcGetMembers();
    getMember.assignActAsSubject(getActAsSubject(true));
    getMember.assignIncludeSubjectDetail(Boolean.TRUE);
    getMember.addGroupName(teamId);
    getMember.addSubjectAttributeName("mail");
    getMember.addSubjectAttributeName("displayName");
    WsGetMembersResult[] getMembers = getMember.execute().getResults();
    List<Member> members = new ArrayList<Member>();
    if (getMembers[0].getWsSubjects() != null
        && getMembers[0].getWsSubjects().length > 0) {
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

  private void addRolesToMembers(List<Member> members, String teamId,
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
   * @param privilegeName De grouper rechten heten "admin" voor de group administrator,
   * en "update" voor de group manager.
   * @return {@link Role}
   */
  private Role getRole(String privilegeName) {
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

  /**
   * {@inheritDoc}
   */
  @Override
  public String addTeam(String teamId, String displayName,
                        String teamDescription, String stemName)
          throws DuplicateTeamException {
    if (!StringUtils.hasText(teamId)) {
      throw new IllegalArgumentException("teamId is not optional");
    }
    if (!StringUtils.hasText(stemName)) {
      stemName = environment.getDefaultStemName();
    }
    for (String ch : FORBIDDEN_CHARS) {
      teamId = teamId.replace(ch, "");
    }
    teamId = teamId.replace(" ", "_").toLowerCase();
    teamId = stemName + ":" + teamId;

    WsGroup wsGroup = new WsGroup();
    wsGroup.setDescription(teamDescription);
    wsGroup.setDisplayExtension(displayName);
    wsGroup.setName(teamId);

    WsGroupToSave group = new WsGroupToSave();
    group.setSaveMode("INSERT");
    group.setWsGroup(wsGroup);

    GcGroupSave groupSave = new GcGroupSave();
    groupSave.assignActAsSubject(getActAsSubject(true));
    groupSave.addGroupToSave(group);
    try {
      groupSave.execute();
    } catch (GcWebServiceError e) {
      WsGroupSaveResults results = (WsGroupSaveResults) e.getContainerResponseObject();
      String resultCode = results.getResults()[0].getResultMetadata().getResultCode();
      if (resultCode.equals("GROUP_ALREADY_EXISTS")) {
        throw new DuplicateTeamException("Team already exists: " + teamId);
      }
    }
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

    if (member.getRoles() != null) {
      for (Role role : member.getRoles()) {
        removeMemberRole(teamId, personId, role, true);
      }
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
    // assignPrivilige.assignSubjectLookup(getActAsSubject(true));
    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
    wsSubjectLookup.setSubjectId("GrouperAll");
    assignPrivilige.assignSubjectLookup(wsSubjectLookup);
    assignPrivilige.assignPrivilegeType("access");
    assignPrivilige.assignPrivilegeName("view");
    assignPrivilige.addSubjectAttributeName("GrouperAll");

    assignPrivilige.assignAllowed(viewable);
    assignPrivilige.execute();
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
    WsAssignGrouperPrivilegesResults result;
    try {
      result = assignPrivilige.execute();
    } catch (RuntimeException e) {
      LOGGER.info("Could not add member role", e);
      // Grouper converts every exception to RuntimeException
      return false;
    }
    return result.getResultMetadata().getResultCode().equals("SUCCESS") ? true
        : false;
  }

  @Override
  public boolean removeMemberRole(String teamId, String memberId, Role role,
      boolean removeAsPowerUser) {
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
    WsAssignGrouperPrivilegesResults result;
    try {
      result = assignPrivilige.execute();
    } catch (RuntimeException e) {
      LOGGER.info("Could not remove role", e);
      // Grouper converts every exception to RuntimeException
      return false;
    }
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
    addMember.execute();
  }

  @Override
  public Member findMember(String teamId, String memberId) {
    Team team = findTeamById(teamId);
    List<Member> members = team.getMembers();

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
    List<Member> members = team.getMembers();

    for (Member member : members) {
      Set<Role> roles = member.getRoles();
      if (!CollectionUtils.isEmpty(roles) && roles.contains(Role.Admin)) {
        result.add(member);
      }
    }

    return result;
  }

  private boolean getVisibilityGroup(String teamId,
      WsGrouperPrivilegeResult[] privilegeResults) {
    for (WsGrouperPrivilegeResult privilege : privilegeResults) {
      if (privilege.getWsGroup().getName().equals(teamId)
            && privilege.getPrivilegeName().equals("view")
            && privilege.getPrivilegeType().equals("access")
            && privilege.getAllowed().equals("T")
            && privilege.getOwnerSubject().getId().equals("GrouperAll")) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param environment the environment to set
   */
  public void setEnvironment(TeamEnvironment environment) {
    this.environment = environment;
  }

  @Override
  public TeamResultWrapper findAllTeams(String stemName, String personId, int offset, int pageSize) {
   return grouperDao.findAllTeams(stemName,personId, offset, pageSize);
  }

  @Override
  public TeamResultWrapper findTeams(String stemName, String personId, String partOfGroupname,
                                     int offset, int pageSize) {
    return grouperDao.findTeams(stemName, personId, partOfGroupname, offset, pageSize);
  }

  @Override
  public TeamResultWrapper findAllTeamsByMember(String stemName, String personId,
                                                int offset, int pageSize) {
    GcGetGroups getGroups = new GcGetGroups();
    getGroups.addSubjectId(personId);
    getGroups.assignActAsSubject(getActAsSubject());
    WsStemLookup wsStemLookup = new WsStemLookup();
    wsStemLookup.setStemName(stemName);
    getGroups.assignWsStemLookup(wsStemLookup);
    getGroups.assignStemScope(StemScope.ALL_IN_SUBTREE);
    WsGetGroupsResult[] groups = getGroups.execute().getResults();
    List<Team> teams = new ArrayList<Team>();
    if (groups.length > 0) {
      WsGroup[] wsGroups = groups[0].getWsGroups();
      teams = convertWsGroupToTeam(wsGroups, true);
    }
    List<Team> limited = new ArrayList<Team>();
    int totalCount = teams.size();
    int max = totalCount < offset + pageSize ? totalCount : offset + pageSize;
    for (int i = offset; i < max; i++) {
      limited.add(teams.get(i));
    }
    return new TeamResultWrapper(limited, totalCount);
  }

  @Override
  public TeamResultWrapper findTeamsByMember(String stemName, String personId,
                                             String partOfGroupname, int offset, int pageSize) {
    TeamResultWrapper teamResultWrapper =
            findAllTeamsByMember(stemName, personId, 0, Integer.MAX_VALUE);
    List<Team> teamsByMember = teamResultWrapper.getTeams();
    List<Team> result = new ArrayList<Team>();
    for (Team team : teamsByMember) {
      if (team.getName().toLowerCase().contains(partOfGroupname.toLowerCase())) {
        result.add(team);
      }
    }
    List<Team> limited = new ArrayList<Team>();
    int totalCount = result.size();
    int max = totalCount < offset + pageSize ? totalCount : offset + pageSize;
    for (int i = offset; i < max; i++) {
      limited.add(result.get(i));
    }
    return new TeamResultWrapper(limited, totalCount);
  }
  
  /**
   * {@inheritDoc}
   */
  
  public List<Team> findAllTeamsOld(String stemName) {
    if (!StringUtils.hasText(stemName)) {
      stemName = environment.getDefaultStemName();
    }
    GcFindGroups findGroups = new GcFindGroups();
    findGroups.assignActAsSubject(getActAsSubject());
    findGroups.assignIncludeGroupDetail(Boolean.TRUE);

    WsQueryFilter queryFilter = new WsQueryFilter();
    queryFilter.setQueryFilterType("FIND_BY_STEM_NAME");
    queryFilter.setStemName(stemName);
    findGroups.assignQueryFilter(queryFilter);
    WsFindGroupsResults findResults = findGroups.execute();
    WsGroup[] groupResults = findResults.getGroupResults();
    return convertWsGroupToTeam(groupResults, false);
  }


}
