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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.surfnet.coin.api.client.domain.Person;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.MemberAttribute;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Stem;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.domain.TeamResultWrapper;
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

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivileges;
import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcFindStems;
import edu.internet2.middleware.grouperClient.api.GcGetGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGroupDelete;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindStemsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsStem;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import static nl.surfnet.coin.teams.util.PersonUtil.isGuest;

/**
 * {@link nl.surfnet.coin.teams.service.GrouperTeamService} using Grouper LDAP as persistent store
 * 
 */
public class GrouperTeamServiceWsImpl extends GrouperDaoImpl implements GrouperTeamService {

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

  public boolean doesStemExists(String stemName) {
    GcFindStems findStems = new GcFindStems();
    findStems.assignActAsSubject(getActAsSubject(getGrouperPowerUser()));
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
      member.setGuest(person.getTags().contains("guest"));
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
  public TeamResultWrapper findTeams(String personId,
                                     String partOfGroupname, int offset, int pageSize) {
    final String sanitizedGroupname = partOfGroupname.replaceAll(" ", "_");
    return super.findTeams(personId, sanitizedGroupname, offset, pageSize);
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
