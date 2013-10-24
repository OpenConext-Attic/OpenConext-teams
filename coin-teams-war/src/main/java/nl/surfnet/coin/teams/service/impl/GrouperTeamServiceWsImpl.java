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

package nl.surfnet.coin.teams.service.impl;

import edu.internet2.middleware.grouperClient.api.*;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.*;
import nl.surfnet.coin.api.client.domain.Person;
import nl.surfnet.coin.teams.domain.*;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.MemberAttributeService;
import nl.surfnet.coin.teams.service.ProvisioningManager;
import nl.surfnet.coin.teams.util.DuplicateTeamException;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static nl.surfnet.coin.teams.util.PersonUtil.isGuest;

/**
 * {@link nl.surfnet.coin.teams.service.GrouperTeamService} using Grouper LDAP as persistent store
 * 
 */
public class GrouperTeamServiceWsImpl implements GrouperTeamService {

  private static final Logger LOG = LoggerFactory.getLogger(GrouperTeamServiceWsImpl.class);

  @Autowired
  private TeamEnvironment environment;

  @Autowired
  private MemberAttributeService memberAttributeService;
  
  @Autowired
  private ProvisioningManager provisioningManager;

  private static final String[] FORBIDDEN_CHARS = new String[] { "<", ">", "/",
      "\\", "*", ":", ",", "%" };

  private static final Logger LOGGER = LoggerFactory
      .getLogger(GrouperTeamServiceWsImpl.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public Team findTeamById(String teamId) {
    GcFindGroups findGroups = new GcFindGroups();
    WsSubjectLookup actAsSubject = getActAsSubject(getGrouperPowerUser());
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

    // Add the stem to the group
    int lastColonIndex = wsGroup.getName().lastIndexOf(":");
    String stemId = wsGroup.getName().substring(0, lastColonIndex);
    Stem stem = findStem(stemId);

    return new Team(wsGroup.getName(), wsGroup.getDisplayExtension(),
        wsGroup.getDescription(), getMembers(wsGroup.getName(),
            privilegeResults), stem, getVisibilityGroup(wsGroup.getName(),
            privilegeResults));
  }


  @Override
  public Stem findStem(String stemId) {
    GcFindStems findStems = new GcFindStems();
    findStems.assignActAsSubject(getActAsSubject(getGrouperPowerUser()));
    findStems.addStemName(stemId);
    try {
      WsFindStemsResults results = findStems.execute();
      WsStem[] stemResults = results.getStemResults();
      WsStem wsStem = stemResults[0];
      return new Stem(wsStem.getName(), wsStem.getDisplayName(), wsStem.getDescription());
    } catch (GcWebServiceError e) {
      // The Grouper implementation throws an Error if there is no Stem
      return null;
    }
  }

  /**
   * Defines which user is performing the action in the Grouper service.
   *
   * @param actAsUser
   *          the identifier of the user that performs the action in the Grouper Webservice
   * @return {@link WsSubjectLookup} for the user
   */
  private WsSubjectLookup getActAsSubject(String actAsUser) {
    WsSubjectLookup actAsSubject = new WsSubjectLookup();
    actAsSubject.setSubjectId(actAsUser);
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
    privileges.assignActAsSubject(getActAsSubject(getGrouperPowerUser()));
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
    getMember.assignActAsSubject(getActAsSubject(getGrouperPowerUser()));
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
   *          The grouper privileges are "admin" for the group adminstrator and
   *          "update" for the group manager.
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
    groupSave.assignActAsSubject(getActAsSubject(getGrouperPowerUser()));
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
    provisioningManager.groupEvent(teamIdWithContext(teamId), displayName, ProvisioningManager.Operation.CREATE);
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
        removeMemberRole(teamId, personId, role, getGrouperPowerUser());
      }
    }

    GcDeleteMember deleteMember = new GcDeleteMember();
    deleteMember.addSubjectId(personId);
    deleteMember.assignActAsSubject(getActAsSubject(getGrouperPowerUser()));
    deleteMember.assignGroupName(teamId);
    deleteMember.execute();
    provisioningManager.teamMemberEvent(teamIdWithContext(teamId), personId, null, ProvisioningManager.Operation.DELETE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteTeam(String teamId) {
    GcGroupDelete groupDelete = new GcGroupDelete();
    groupDelete.assignActAsSubject(getActAsSubject(getGrouperPowerUser()));
    WsGroupLookup wsGroupLookup = new WsGroupLookup(teamId, null);
    groupDelete.addGroupLookup(wsGroupLookup);
    groupDelete.execute();
    provisioningManager.groupEvent(teamIdWithContext(teamId), null, ProvisioningManager.Operation.DELETE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateTeam(String teamId, String displayName,
                         String teamDescription, String actAsSubject) {
    GcGroupSave groupSave = new GcGroupSave();
    groupSave.assignActAsSubject(getActAsSubject(actAsSubject));
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
    GcAssignGrouperPrivilegesLite assignPrivilege = new GcAssignGrouperPrivilegesLite();
    assignPrivilege.assignActAsSubject(getActAsSubject(getGrouperPowerUser()));
    assignPrivilege.assignGroupName(teamId);
    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
    wsSubjectLookup.setSubjectId("GrouperAll");
    assignPrivilege.assignSubjectLookup(wsSubjectLookup);
    assignPrivilege.assignPrivilegeType("access");
    assignPrivilege.assignPrivilegeName("view");
    assignPrivilege.addSubjectAttributeName("GrouperAll");

    assignPrivilege.assignAllowed(viewable);
    assignPrivilege.execute();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean addMemberRole(String teamId, String memberId, Role role,
                               String actAsUserId) {
    GcAssignGrouperPrivileges assignPrivilege = new GcAssignGrouperPrivileges();
    assignPrivilege.assignActAsSubject(getActAsSubject(actAsUserId));
    assignPrivilege.assignGroupLookup(new WsGroupLookup(teamId, null));
    WsSubjectLookup subject = new WsSubjectLookup();
    subject.setSubjectId(memberId);
    assignPrivilege.addSubjectLookup(subject);
    assignPrivilege.assignPrivilegeType("access");
    switch (role) {
      case Admin: {
      assignPrivilege.addPrivilegeName("admin");
        assignPrivilege.addPrivilegeName("read");
      assignPrivilege.addPrivilegeName("optout");
      assignPrivilege.addPrivilegeName("update");
        break;
    }
    case Manager: {
      assignPrivilege.addPrivilegeName("update");
      assignPrivilege.addPrivilegeName("read");
      assignPrivilege.addPrivilegeName("optout");
      break;
    }
    case Member: {
      assignPrivilege.addPrivilegeName("read");
      assignPrivilege.addPrivilegeName("optout");
      break;
    }
    }
    assignPrivilege.assignAllowed(true);
    WsAssignGrouperPrivilegesResults result;
    try {
      result = assignPrivilege.execute();
    } catch (RuntimeException e) {
      LOGGER.info("Could not add member role", e);
      // Grouper converts every exception to RuntimeException
      return false;
    }
    boolean success = result.getResultMetadata().getResultCode().equals("SUCCESS") ? true
        : false;
    if (success) {
      provisioningManager.roleEvent(teamIdWithContext(teamId), memberId, role.name().toLowerCase(), ProvisioningManager.Operation.CREATE);
    }
    return success;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeMemberRole(String teamId, String memberId, Role role,
                                  String actAsUserId) {
    GcAssignGrouperPrivileges assignPrivilege = new GcAssignGrouperPrivileges();
    assignPrivilege.assignActAsSubject(getActAsSubject(actAsUserId));
    assignPrivilege.assignGroupLookup(new WsGroupLookup(teamId, null));
    WsSubjectLookup subject = new WsSubjectLookup();
    subject.setSubjectId(memberId);
    assignPrivilege.addSubjectLookup(subject);
    assignPrivilege.assignPrivilegeType("access");
    switch (role) {
    case Admin: {
      assignPrivilege.addPrivilegeName("admin");
      break;
    }
    case Manager: {
      assignPrivilege.addPrivilegeName("update");
      break;
    }
    case Member: {
      assignPrivilege.addPrivilegeName("read");
      assignPrivilege.addPrivilegeName("optout");
      break;
    }
    }
    assignPrivilege.assignAllowed(false);
    WsAssignGrouperPrivilegesResults result;
    try {
      result = assignPrivilege.execute();
    } catch (RuntimeException e) {
      LOGGER.info("Could not remove role", e);
      // Grouper converts every exception to RuntimeException
      return false;
    }
    boolean success = result.getResultMetadata().getResultCode().equals("SUCCESS") ? true
        : false;
    if (success) {
      provisioningManager.roleEvent(teamIdWithContext(teamId), memberId, role.name().toLowerCase(), ProvisioningManager.Operation.DELETE);
    }
    return success;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addMember(String teamId, Person person) {
    GcAddMember addMember = new GcAddMember();
    addMember.assignActAsSubject(getActAsSubject(getGrouperPowerUser()));
    addMember.assignGroupName(teamId);
    addMember.addSubjectId(person.getId());
    addMember.execute();
    Member member = findMember(teamId, person.getId());
    if (member.isGuest() != isGuest(person)) {
      member.setGuest(isGuest(person));
      memberAttributeService.saveOrUpdate(member.getMemberAttributes());
    }
    provisioningManager.teamMemberEvent(teamIdWithContext(teamId), person.getId(), "member", ProvisioningManager.Operation.CREATE);
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
    throw new RuntimeException("Member(memberId='" + memberId + "') is not a member of the given team");
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
  public TeamResultWrapper findTeams(String personId,
                                     String partOfGroupname, int offset, int pageSize) {

    // FIXME: exclude teams from etc stem
    try {
      WsQueryFilter filter = new WsQueryFilter();
      filter.setPageSize(String.valueOf(pageSize));
      filter.setPageNumber(String.valueOf(pagenumber(offset, pageSize)));
      filter.setGroupName("%" + partOfGroupname + "%");
      filter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
      WsFindGroupsResults results = new GcFindGroups()
              .assignQueryFilter(filter)
              .assignActAsSubject(getActAsSubject(getGrouperPowerUser()))
              .assignIncludeGroupDetail(false)
              .execute();

      return buildTeamResultWrapper(results, offset, pageSize, null);
    } catch (GcWebServiceError e) {
      LOG.debug("Could not get teams by member {}. Perhaps no groups for this user? Will return empty list. Exception msg: {}", personId, e.getMessage());
      return new TeamResultWrapper(new ArrayList<Team>(), 0, offset, pageSize);
    }
  }

  @Override
  public TeamResultWrapper findAllTeamsByMember(String personId, int offset, int pageSize) {
    // FIXME: exclude teams from etc stem
    try {
      WsGetGroupsResults results = new GcGetGroups()
      .assignActAsSubject(getActAsSubject(getGrouperPowerUser()))
      .addSubjectId(personId)
      .assignPageSize(pageSize)
      .assignPageNumber(pagenumber(offset, pageSize))
      .execute();
      return buildTeamResultWrapper(results, offset, pageSize, personId);
    } catch (GcWebServiceError e) {
      LOG.debug("Could not get all teams by member {}. Perhaps no groups for this user? Will return empty list. Exception msg: {}", personId, e.getMessage());
      return new TeamResultWrapper(new ArrayList<Team>(), 0, offset, pageSize);
    }
  }

  /**
   * Get a page number by the given offset and pageSize.
   */
  public int pagenumber(int offset, int pageSize) {
    return (int) Math.floor(offset / pageSize) + 1;
  }


  @Override
  public TeamResultWrapper findTeamsByMember(String personId, String partOfGroupname, int offset, int pageSize) {
    WsGetGroupsResults results = new GcGetGroups()
            .addSubjectId(personId)
            .assignActAsSubject(getActAsSubject(getGrouperPowerUser()))
            .assignPageNumber(pagenumber(offset, pageSize))
            .assignPageSize(pageSize)
            .assignScope("%" + partOfGroupname + "%")
            .execute();
    return buildTeamResultWrapper(results, offset, pageSize, personId);
  }

  @Override
  public List<Stem> findStemsByMember(String personId) {
    WsGetMembershipsResults results = new GcGetMemberships()
            .addWsSubjectLookup(new WsSubjectLookup(personId, null, null))
            .execute();
    return getListOfStems(results.getWsStems());
  }

  private List<Stem> getListOfStems(WsStem[] wsStems) {
    List<Stem> stems = new ArrayList<>();
    if (wsStems != null) {
      for (WsStem wsStem : wsStems) {
        Stem stem = new Stem(wsStem.getName(), wsStem.getDisplayName(), wsStem.getDescription());
        stems.add(stem);
      }
    }
    return stems;
  }

  @Override
  public TeamResultWrapper findAllTeams(String personId, int offset, int pageSize) {
    // FIXME: exclude teams from etc stem
    WsQueryFilter filter = new WsQueryFilter();
    filter.setPageSize(String.valueOf(pageSize));
    filter.setPageNumber(String.valueOf(pagenumber(offset, pageSize)));

    filter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
    filter.setGroupName("%");

    WsFindGroupsResults results = new GcFindGroups()
            .assignQueryFilter(filter)
            .assignIncludeGroupDetail(true)
            .assignActAsSubject(new WsSubjectLookup(personId, null, null))
            .execute();
    return buildTeamResultWrapper(results, offset, pageSize, personId);
  }

  private TeamResultWrapper buildTeamResultWrapper(WsFindGroupsResults results, int offset, int pageSize, String userId) {
    List<Team> teams = new ArrayList<>();
    if (results.getGroupResults() != null && results.getGroupResults().length > 0) {
      for (WsGroup group : results.getGroupResults()) {
        teams.add(buildTeam(group, userId));
      }
    }
    // FIXME: get total from textual metadata or otherwise
    return new TeamResultWrapper(teams, 9999, offset, pageSize);
  }

  private Team buildTeam(WsGroup group, String userId) {
    Team team = new Team(group.getName(), group.getDisplayExtension(), group.getDescription());

    // Query and add all member numbers
    WsGetMembershipsResults results = new GcGetMemberships()
            .addGroupName(group.getName())
            .addGroupUuid(group.getUuid())
            .assignActAsSubject(getActAsSubject(getGrouperPowerUser()))
            .assignIncludeSubjectDetail(true)
            .execute();
    if (results.getWsMemberships() != null) {
      team.setNumberOfMembers(results.getWsMemberships().length);
    }

    // Query and add roles for current user
    WsGetGrouperPrivilegesLiteResult privilegesResults = new GcGetGrouperPrivilegesLite()
            .assignSubjectLookup(new WsSubjectLookup(userId, null, null))
            .assignActAsSubject(getActAsSubject(userId))
            .assignGroupName(group.getName())
            .execute();
    team.setViewerRole(Role.fromGrouperPrivileges(privilegesResults.getPrivilegeResults()));
    return team;
  }

  private TeamResultWrapper buildTeamResultWrapper(WsGetGroupsResults results, int offset, int pageSize, String userId) {
    List<Team> teams = new ArrayList<>();
    if (results.getResults() != null && results.getResults().length > 0) {
      for (WsGetGroupsResult wsGetGroupsResult : results.getResults()) {
        if (wsGetGroupsResult.getWsGroups() != null && wsGetGroupsResult.getWsGroups().length > 0) {
          for (WsGroup group : wsGetGroupsResult.getWsGroups()) {
            teams.add(buildTeam(group, userId));
          }
        }
      }
    }
    // FIXME: get total from textual metadata
    return new TeamResultWrapper(teams, 9999, offset, pageSize);
  }

  public String getGrouperPowerUser() {
    return environment.getGrouperPowerUser();
  }

  /**
   * Prepend the teamId with the (environment specific) context. E.g.:
   * urn:collab:group:surfteams.nl:
   * @param teamId the teamId without context (e.g.: nl:surfnet:diensten:mygroup)
   * @return the concatenation:  urn:collab:group:surfteams.nl:nl:surfnet:diensten:mygroup
   */
  protected String teamIdWithContext(String teamId) {
    return environment.getGroupNameContext() + teamId;
  }
}
