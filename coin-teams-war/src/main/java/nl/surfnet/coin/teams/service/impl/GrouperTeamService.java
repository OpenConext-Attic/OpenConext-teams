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

package nl.surfnet.coin.teams.service.impl;

import edu.internet2.middleware.grouperClient.api.*;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.*;
import nl.surfnet.coin.teams.domain.*;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperDao;
import nl.surfnet.coin.teams.service.MemberAttributeService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.DuplicateTeamException;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import org.opensocial.models.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * {@link TeamService} using Grouper LDAP as persistent store
 * 
 */
public class GrouperTeamService implements TeamService {

  @Autowired
  private TeamEnvironment environment;

  @Autowired
  private GrouperDao grouperDao;

  @Autowired
  private MemberAttributeService memberAttributeService;

  private static final String[] FORBIDDEN_CHARS = new String[] { "<", ">", "/",
      "\\", "*", ":", ",", "%" };

  private static final Logger LOGGER = LoggerFactory
      .getLogger(GrouperTeamService.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public Team findTeamById(String teamId) {
    GcFindGroups findGroups = new GcFindGroups();
    WsSubjectLookup actAsSubject = getActAsSubject(true);
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

  public boolean doesStemExists(String stemName) {
    GcFindStems findStems = new GcFindStems();
    findStems.assignActAsSubject(getActAsSubject(true));
    findStems.addStemName(stemName);
    try {
      WsFindStemsResults results = findStems.execute();
      WsStem[] stemResults = results.getStemResults();
      return stemResults != null && stemResults.length > 0;
    } catch (GcWebServiceError e) {
      // The Grouper implementation throws an Error if there is no Stem
      return false;
    }
  }

  /**
   * Defines which user is performing the action in the Grouper service.
   * 
   * @return {@link WsSubjectLookup} for the current logged in user
   */
  private WsSubjectLookup getActAsSubject() {
    return getActAsSubject(false);
  }

  /**
   * Defines which user is performing the action in the Grouper service.
   * 
   * @param powerUser
   *          if {@literal true} a configured power user will perform the
   *          action, bypassing the current user's privileges. Otherwise the
   *          current user's privileges are used.
   * @return {@link WsSubjectLookup} for either the power user or the logged in
   *         user
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

  /**
   * Gets an array of all group privileges for a team
   * 
   * @param teamId
   *          unique identifier for the team
   * @return array of {@link WsGrouperPrivilegeResult}'s
   */
  private WsGrouperPrivilegeResult[] getGroupPrivileges(String teamId) {
    GcGetGrouperPrivilegesLite privileges = new GcGetGrouperPrivilegesLite();
    privileges.assignActAsSubject(getActAsSubject(true));
    privileges.assignGroupName(teamId);
    WsGrouperPrivilegeResult[] privilegeResults = privileges.execute()
        .getPrivilegeResults();
    return privilegeResults;
  }

  /**
   * Builds a List of {@link Member}'s based on a Grouper query result.<br />
   * Enriches the Member's with their {@link Role}'s and custom attributes.
   * 
   * @param teamId
   *          unique identifier of the Team
   * @param privilegeResults
   *          query result from Grouper as array
   * @return List of Member's, can be empty
   */
  private List<Member> getMembers(final String teamId,
      final WsGrouperPrivilegeResult[] privilegeResults) {
    List<Member> members = new ArrayList<Member>();
    if (privilegeResults == null) {
      return members;
    }

    WsGetMembersResult[] getMembers = getMemberDataFromWs(teamId);
    if (getMembers == null || getMembers.length == 0
        || getMembers[0].getWsSubjects() == null) {
      return members;
    }

    final WsSubject[] subjects = getMembers[0].getWsSubjects();
    Map<String, Member> memberMap = new HashMap<String, Member>();
    for (WsSubject wsSubject : subjects) {
      final String id = wsSubject.getId();
      final String mail = wsSubject.getAttributeValue(0);
      final String displayName = wsSubject.getName();
      final Set<Role> roles = getRolesForMember(id, privilegeResults);
      memberMap.put(id, new Member(roles, displayName, id, mail));
    }
    assignAttributesToMembers(memberMap);
    members.addAll(memberMap.values());
    return members;
  }

  /**
   * Retrieves member data from the webservice
   * 
   * @param teamId
   *          unique identifier for a Team
   * @return array that represents member data
   */
  private WsGetMembersResult[] getMemberDataFromWs(String teamId) {
    GcGetMembers getMember = new GcGetMembers();
    getMember.assignActAsSubject(getActAsSubject(true));
    getMember.assignIncludeSubjectDetail(Boolean.TRUE);
    getMember.addGroupName(teamId);
    getMember.addSubjectAttributeName("mail");
    getMember.addSubjectAttributeName("displayName");
    return getMember.execute().getResults();
  }

  /**
   * Gets the SURFnet attributes for a member (from the db) and adds them to the
   * Member object
   * 
   * @param members
   *          Map of {@link nl.surfnet.coin.teams.domain.Member}'s that need to
   *          be enriched with attributes
   */
  private void assignAttributesToMembers(Map<String, Member> members) {
    final List<MemberAttribute> attributesForMembers = memberAttributeService
        .findAttributesForMembers(members.values());
    for (MemberAttribute memberAttribute : attributesForMembers) {
      Member member = members.get(memberAttribute.getMemberId());
      if (member != null) { // if db is not cleaned up
        member.addMemberAttribute(memberAttribute);
      }
    }

  }

  /**
   * Filters the roles for a single member based on an array of privileges
   * 
   * @param memberId
   *          unique identifier of the {@link Member}
   * @param privilegeResults
   *          array of {@link WsGrouperPrivilegeResult}'s
   * @return Set of {@link Role}'s for this Member
   */
  private Set<Role> getRolesForMember(final String memberId,
      final WsGrouperPrivilegeResult[] privilegeResults) {
    Set<Role> roles = new HashSet<Role>();
    final List<WsGrouperPrivilegeResult> memberPrivs = getPrivilegeResultsForMember(
        memberId, privilegeResults);
    for (WsGrouperPrivilegeResult priv : memberPrivs) {
      roles.add(getRole(priv.getPrivilegeName()));
    }
    return roles;
  }

  /**
   * @param privilegeName
   *          De grouper rechten heten "admin" voor de group administrator, en
   *          "update" voor de group manager.
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

  /**
   * @param memberId
   *          unique identifier for a member
   * @param privilegeResults
   *          an array of {@link WsGrouperPrivilegeResult}
   * @return List of {@link WsGrouperPrivilegeResult} for a specific member, can
   *         be empty
   */
  private List<WsGrouperPrivilegeResult> getPrivilegeResultsForMember(
      String memberId, WsGrouperPrivilegeResult[] privilegeResults) {
    List<WsGrouperPrivilegeResult> result = new ArrayList<WsGrouperPrivilegeResult>();
    for (WsGrouperPrivilegeResult privilege : privilegeResults) {
      if (privilege.getOwnerSubject().getId().equals(memberId)) {
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
      String teamDescription, String stemName) throws DuplicateTeamException {
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
      WsGroupSaveResults results = (WsGroupSaveResults) e
          .getContainerResponseObject();
      String resultCode = results.getResults()[0].getResultMetadata()
          .getResultCode();
      if (resultCode.equals("GROUP_ALREADY_EXISTS")) {
        throw new DuplicateTeamException("Team already exists: " + teamId);
      }
    }
    return teamId;
  }

  /**
   * {@inheritDoc}
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteTeam(String teamId) {
    GcGroupDelete groupDelete = new GcGroupDelete();
    groupDelete.assignActAsSubject(getActAsSubject(true));
    WsGroupLookup wsGroupLookup = new WsGroupLookup(teamId, null);
    groupDelete.addGroupLookup(wsGroupLookup);
    groupDelete.execute();

  }

  /**
   * {@inheritDoc}
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void setVisibilityGroup(String teamId, boolean viewable) {
    GcAssignGrouperPrivilegesLite assignPrivilige = new GcAssignGrouperPrivilegesLite();
    assignPrivilige.assignActAsSubject(getActAsSubject(true));
    assignPrivilige.assignGroupName(teamId);
    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
    wsSubjectLookup.setSubjectId("GrouperAll");
    assignPrivilige.assignSubjectLookup(wsSubjectLookup);
    assignPrivilige.assignPrivilegeType("access");
    assignPrivilige.assignPrivilegeName("view");
    assignPrivilige.addSubjectAttributeName("GrouperAll");

    assignPrivilige.assignAllowed(viewable);
    assignPrivilige.execute();
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void addMember(String teamId, Person person) {
    GcAddMember addMember = new GcAddMember();
    addMember.assignActAsSubject(getActAsSubject(true));
    addMember.assignGroupName(teamId);
    addMember.addSubjectId(person.getId());
    addMember.execute();
    Member member = findMember(teamId, person.getId());
    if (member.isGuest() != person.isGuest()) {
      member.setGuest(person.isGuest());
      memberAttributeService.saveOrUpdate(member.getMemberAttributes());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Member findMember(String teamId, String memberId) {
    Team team = findTeamById(teamId);
    List<Member> members = team.getMembers();

    for (Member member : members) {
      if (member.getId().equals(memberId)) {
        return member;
      }
    }
    throw new RuntimeException("Member(memberId='" + memberId
        + "') is not a member of the given team");
  }

  /**
   * {@inheritDoc}
   */
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
   * @param environment
   *          the environment to set
   */
  public void setEnvironment(TeamEnvironment environment) {
    this.environment = environment;
  }

  @Override
  public TeamResultWrapper findAllTeams(String stemName, String personId,
      int offset, int pageSize) {
    return grouperDao.findAllTeams(stemName, personId, offset, pageSize);
  }

  @Override
  public TeamResultWrapper findTeams(String stemName, String personId,
      String partOfGroupname, int offset, int pageSize) {
    return grouperDao.findTeams(stemName, personId, partOfGroupname, offset,
        pageSize);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.GrouperDao#findAllTeamsByMember(java.lang
   * .String, java.lang.String, int, int)
   */
  @Override
  public TeamResultWrapper findAllTeamsByMember(String stemName,
      String personId, int offset, int pageSize) {
    return grouperDao
        .findAllTeamsByMember(stemName, personId, offset, pageSize);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.GrouperDao#findTeamsByMember(java.lang.String
   * , java.lang.String, java.lang.String, int, int)
   */
  @Override
  public TeamResultWrapper findTeamsByMember(String stemName, String personId,
      String partOfGroupname, int offset, int pageSize) {
    return grouperDao.findTeamsByMember(stemName, personId, partOfGroupname,
        offset, pageSize);
  }

}
