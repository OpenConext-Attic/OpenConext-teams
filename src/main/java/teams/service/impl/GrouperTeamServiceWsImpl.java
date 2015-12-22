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
package teams.service.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivileges;
import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcFindStems;
import edu.internet2.middleware.grouperClient.api.GcGetGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGetMemberships;
import edu.internet2.middleware.grouperClient.api.GcGroupDelete;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindStemsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsStem;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import teams.domain.Member;
import teams.domain.MemberAttribute;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Stem;
import teams.domain.Team;
import teams.domain.TeamResultWrapper;
import teams.service.GrouperTeamService;
import teams.service.MemberAttributeService;
import teams.util.DuplicateTeamException;

public class GrouperTeamServiceWsImpl implements GrouperTeamService {

  private static final Logger LOG = LoggerFactory.getLogger(GrouperTeamServiceWsImpl.class);

  private static final String[] FORBIDDEN_CHARS = new String[]{"<", ">", "/", "\\", "*", ":", ",", "%"};

  private final MemberAttributeService memberAttributeService;

  private final String defaultStemName;
  private final String powerUser;

  public GrouperTeamServiceWsImpl(MemberAttributeService memberAttributeService, String defaultStemName, String powerUser) {
    this.memberAttributeService = memberAttributeService;
    this.defaultStemName = defaultStemName;
    this.powerUser = powerUser;
  }

  @Override
  public Team findTeamById(String teamId) {
    WsSubjectLookup actAsSubject = getActAsSubject(powerUser);

    GcFindGroups findGroups = new GcFindGroups();
    findGroups.assignActAsSubject(actAsSubject);
    findGroups.assignIncludeGroupDetail(Boolean.TRUE);
    findGroups.addGroupName(teamId);

    WsGroup[] groupResults = findGroups.execute().getGroupResults();

    if (groupResults == null || groupResults.length == 0) {
      throw new RuntimeException("No team found with Id('" + teamId + "')");
    }

    WsGroup wsGroup = groupResults[0];
    WsGrouperPrivilegeResult[] privilegeResults = getGroupPrivileges(wsGroup.getName());

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
    findStems.assignActAsSubject(getActAsSubject(powerUser));
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
   * @param actAsUser the identifier of the user that performs the action in the Grouper Webservice
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
   * @param teamId unique identifier for the team
   * @return array of {@link WsGrouperPrivilegeResult}'s
   */
  private WsGrouperPrivilegeResult[] getGroupPrivileges(String teamId) {
    GcGetGrouperPrivilegesLite privileges = new GcGetGrouperPrivilegesLite();
    privileges.assignActAsSubject(getActAsSubject(powerUser));
    privileges.assignGroupName(teamId);
    WsGrouperPrivilegeResult[] privilegeResults = privileges.execute().getPrivilegeResults();
    return privilegeResults;
  }

  /**
   * Builds a List of {@link Member}'s based on a Grouper query result.<br />
   * Enriches the Member's with their {@link Role}'s and custom attributes.
   *
   * @param teamId           unique identifier of the Team
   * @param privilegeResults query result from Grouper as array
   * @return List of Member's, can be empty
   */
  private List<Member> getMembers(final String teamId, final WsGrouperPrivilegeResult[] privilegeResults) {
    List<Member> members = new ArrayList<Member>();
    if (privilegeResults == null) {
      return members;
    }

    WsGetMembersResult[] getMembers = getMemberDataFromWs(teamId);
    if (getMembers == null || getMembers.length == 0 || getMembers[0].getWsSubjects() == null) {
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
   * @param teamId unique identifier for a Team
   * @return array that represents member data
   */
  private WsGetMembersResult[] getMemberDataFromWs(String teamId) {
    GcGetMembers getMember = new GcGetMembers();
    getMember.assignActAsSubject(getActAsSubject(powerUser));
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
   * @param members Map of {@link nl.surfnet.coin.teams.domain.Member}'s that need to
   *                be enriched with attributes
   */
  private void assignAttributesToMembers(Map<String, Member> members) {
    List<MemberAttribute> attributesForMembers = memberAttributeService.findAttributesForMembers(members.values());
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
   * @param memberId         unique identifier of the {@link Member}
   * @param privilegeResults array of {@link WsGrouperPrivilegeResult}'s
   * @return Set of {@link Role}'s for this Member
   */
  private Set<Role> getRolesForMember(String memberId, WsGrouperPrivilegeResult[] privilegeResults) {
    return getPrivilegeResultsForMember(memberId, privilegeResults).stream()
        .map(priv -> getRole(priv.getPrivilegeName()))
        .collect(toSet());
  }

  /**
   * @param privilegeName The grouper privileges are "admin" for the group adminstrator and
   *                      "update" for the group manager.
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
   * @param memberId         unique identifier for a member
   * @param privilegeResults an array of {@link WsGrouperPrivilegeResult}
   * @return List of {@link WsGrouperPrivilegeResult} for a specific member, can
   * be empty
   */
  private List<WsGrouperPrivilegeResult> getPrivilegeResultsForMember(
    String memberId, WsGrouperPrivilegeResult[] privilegeResults) {
    List<WsGrouperPrivilegeResult> result = new ArrayList<>();
    for (WsGrouperPrivilegeResult privilege : privilegeResults) {
      if (privilege.getOwnerSubject().getId().equals(memberId)) {
        result.add(privilege);
      }
    }
    return result;
  }

  @Override
  public String addTeam(String teamId, String displayName, String teamDescription, String stemName) throws DuplicateTeamException {
    if (!StringUtils.hasText(teamId)) {
      throw new IllegalArgumentException("teamId is not optional");
    }
    if (!StringUtils.hasText(stemName)) {
      stemName = defaultStemName;
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
    groupSave.assignActAsSubject(getActAsSubject(powerUser));
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

  @Override
  public void deleteMember(String teamId, String personId) {
    Member member = findMember(teamId, personId);

    if (member.getRoles() != null) {
      for (Role role : member.getRoles()) {
        removeMemberRole(teamId, personId, role, powerUser);
      }
    }

    GcDeleteMember deleteMember = new GcDeleteMember();
    deleteMember.addSubjectId(personId);
    deleteMember.assignActAsSubject(getActAsSubject(powerUser));
    deleteMember.assignGroupName(teamId);
    deleteMember.execute();
  }

  @Override
  public void deleteTeam(String teamId) {
    GcGroupDelete groupDelete = new GcGroupDelete();
    groupDelete.assignActAsSubject(getActAsSubject(powerUser));
    WsGroupLookup wsGroupLookup = new WsGroupLookup(teamId, null);
    groupDelete.addGroupLookup(wsGroupLookup);
    groupDelete.execute();
  }

  @Override
  public void updateTeam(String teamId, String displayName, String teamDescription, String actAsSubject) {
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

  @Override
  public void setVisibilityGroup(String teamId, boolean viewable) {
    GcAssignGrouperPrivilegesLite assignPrivilege = new GcAssignGrouperPrivilegesLite();
    assignPrivilege.assignActAsSubject(getActAsSubject(powerUser));
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
      LOG.info("Could not add member role", e);
      // Grouper converts every exception to RuntimeException
      return false;
    }
    return result.getResultMetadata().getResultCode().equals("SUCCESS");
  }

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
      LOG.info("Could not remove role", e);
      // Grouper converts every exception to RuntimeException
      return false;
    }
    return result.getResultMetadata().getResultCode().equals("SUCCESS") ? true
      : false;
  }

  @Override
  public void addMember(String teamId, Person person) {
    GcAddMember addMember = new GcAddMember();
    addMember.assignActAsSubject(getActAsSubject(powerUser));
    addMember.assignGroupName(teamId);
    addMember.addSubjectId(person.getId());
    addMember.execute();
    Member member = findMember(teamId, person.getId());
    if (member.isGuest() != person.isGuest()) {
      member.setGuest(person.isGuest());
      memberAttributeService.saveOrUpdate(member.getMemberAttributes());
    }
  }

  private boolean getVisibilityGroup(String teamId, WsGrouperPrivilegeResult[] privilegeResults) {
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

  @Override
  public List<Team> findPublicTeams(String personId, String partOfGroupname) {
    // FIXME: exclude teams from etc stem
    try {
      WsQueryFilter filter = new WsQueryFilter();
      filter.setGroupName(defaultStemName + ":%" + partOfGroupname + "%");
      filter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
      filter.setStemName(defaultStemName);
      WsFindGroupsResults results = new GcFindGroups()
        .assignQueryFilter(filter)
        .assignActAsSubject(getActAsSubject(personId))
        .assignIncludeGroupDetail(false)
        .execute();

      return Optional.ofNullable(results.getGroupResults())
          .map(Arrays::stream)
          .orElse(Stream.empty())
          .map(g -> buildTeam(g, personId, false))
          .collect(toList());
    } catch (GcWebServiceError e) {
      LOG.warn("Could not get teams by member {}. Perhaps no groups for this user? Will return empty list. Exception msg: {}", personId, e.getMessage());
      return Collections.emptyList();
    }
  }

  @Override
  public TeamResultWrapper findAllTeamsByMember(String personId, int offset, int pageSize) {
    // FIXME: exclude teams from etc stem
    try {
      long start = System.currentTimeMillis();
      WsGetGroupsResults results = new GcGetGroups()
        .assignActAsSubject(getActAsSubject(powerUser))
        .addSubjectId(personId)
        .assignPageSize(pageSize)
        .assignPageNumber(pagenumber(offset, pageSize))
        .assignIncludeGroupDetail(false)
        .assignIncludeSubjectDetail(false)
        .assignWsStemLookup(new WsStemLookup(defaultStemName, null))
        .assignStemScope(StemScope.ALL_IN_SUBTREE)
        .execute();
      long end = System.currentTimeMillis();
      LOG.trace("findAllTeamsByMember: {} ms", end - start);

      // Get total count
      long start2 = System.currentTimeMillis();
      WsGetGroupsResults totalCountResults = new GcGetGroups()
        .assignActAsSubject(getActAsSubject(powerUser))
        .addSubjectId(personId)
        .assignIncludeGroupDetail(false)
        .assignIncludeSubjectDetail(false)
        .assignWsStemLookup(new WsStemLookup(defaultStemName, null))
        .assignStemScope(StemScope.ALL_IN_SUBTREE)
        .execute();
      long end2 = System.currentTimeMillis();
      LOG.trace("findAllTeamsByMember, count: {} ms", end2 - start2);

      int totalCount = (totalCountResults.getResults() != null
        && totalCountResults.getResults().length > 0
        && totalCountResults.getResults()[0].getWsGroups() != null)
        ? totalCountResults.getResults()[0].getWsGroups().length
        : 0;

      return buildTeamResultWrapper(results, offset, pageSize, personId, totalCount, true);
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
      .assignActAsSubject(getActAsSubject(powerUser))
      .assignIncludeGroupDetail(false)
      .assignIncludeSubjectDetail(false)
      .assignWsStemLookup(new WsStemLookup(defaultStemName, null))
      .assignStemScope(StemScope.ALL_IN_SUBTREE)
      .assignScope(defaultStemName + ":%" + partOfGroupname + "%")
      .assignPageNumber(pagenumber(offset, pageSize))
      .assignPageSize(pageSize)
      .execute();

    // Get total count
    WsGetGroupsResults totalCountResults = new GcGetGroups()
      .addSubjectId(personId)
      .assignActAsSubject(getActAsSubject(powerUser))
      .assignIncludeGroupDetail(false)
      .assignIncludeSubjectDetail(false)
      .assignWsStemLookup(new WsStemLookup(defaultStemName, null))
      .assignStemScope(StemScope.ALL_IN_SUBTREE)
      .assignScope("%" + partOfGroupname + "%")
      .execute();

    int totalCount = (totalCountResults.getResults() != null
      && totalCountResults.getResults().length > 0
      && totalCountResults.getResults()[0].getWsGroups() != null)
      ? totalCountResults.getResults()[0].getWsGroups().length
      : 0;

    return buildTeamResultWrapper(results, offset, pageSize, personId, totalCount, true);
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

  private Team buildTeam(WsGroup group, String userId, boolean addMemberCountAndRoles) {
    Team team = new Team(group.getName(), group.getDisplayExtension(), group.getDescription());

    if (addMemberCountAndRoles) {
      long start = System.currentTimeMillis();
      // Query and add all member numbers
      WsGetMembershipsResults results = new GcGetMemberships()
        .addGroupName(group.getName())
        .addGroupUuid(group.getUuid())
        .assignActAsSubject(getActAsSubject(powerUser))
        .assignIncludeSubjectDetail(false)
        .assignIncludeGroupDetail(false)
        .execute();
      if (results.getWsMemberships() != null) {
        team.setNumberOfMembers(results.getWsMemberships().length);
      }
      long end = System.currentTimeMillis();

      long start2 = System.currentTimeMillis();
      // Query and add roles for current user
      WsGetGrouperPrivilegesLiteResult privilegesResults = new GcGetGrouperPrivilegesLite()
        .assignSubjectLookup(new WsSubjectLookup(userId, null, null))
        .assignActAsSubject(getActAsSubject(userId))
        .assignGroupName(group.getName())
        .assignIncludeGroupDetail(false)
        .assignIncludeSubjectDetail(false)
        .execute();
      long end2 = System.currentTimeMillis();
      LOG.trace("buildTeam : {} ms, {} ms", end - start, end2 - start2);
      team.setViewerRole(Role.fromGrouperPrivileges(privilegesResults.getPrivilegeResults()));
    }
    return team;
  }

  private TeamResultWrapper buildTeamResultWrapper(WsGetGroupsResults results, int offset, int pageSize, String userId, int totalCount, boolean addMemberCountAndRoles) {
    List<Team> teams = new ArrayList<>();

    if (results.getResults() != null && results.getResults().length > 0) {
      for (WsGetGroupsResult wsGetGroupsResult : results.getResults()) {
        if (wsGetGroupsResult.getWsGroups() != null && wsGetGroupsResult.getWsGroups().length > 0) {
          for (WsGroup group : wsGetGroupsResult.getWsGroups()) {
            teams.add(buildTeam(group, userId, addMemberCountAndRoles));
          }
        }
      }
    }
    return new TeamResultWrapper(teams, totalCount, offset, pageSize);
  }

}
