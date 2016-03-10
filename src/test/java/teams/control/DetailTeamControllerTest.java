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
package teams.control;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static teams.control.DetailTeamController.INVITATIONS_PARAM;
import static teams.control.DetailTeamController.MEMBER_PARAM;
import static teams.control.DetailTeamController.PENDING_REQUESTS_PARAM;
import static teams.control.DetailTeamController.ROLE_PARAM;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;
import static teams.util.TokenUtil.TOKENCHECK;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.LocaleResolver;

import teams.Application;
import teams.domain.Invitation;
import teams.domain.JoinTeamRequest;
import teams.domain.Member;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Team;
import teams.domain.TeamExternalGroup;
import teams.service.GrouperTeamService;
import teams.service.JoinTeamRequestService;
import teams.service.TeamExternalGroupDao;
import teams.service.TeamInviteService;
import teams.util.ControllerUtil;
import teams.util.TokenUtil;

@RunWith(MockitoJUnitRunner.class)
public class DetailTeamControllerTest {

  @InjectMocks
  private DetailTeamController subject;

  @Mock
  private GrouperTeamService grouperTeamServiceMock;
  @Mock
  private TeamInviteService teamInviteServiceMock;
  @Mock
  private Environment environmentMock;
  @Mock
  private JoinTeamRequestService joinTeamRequestServiceMock;
  @Mock
  private TeamExternalGroupDao teamExternalGroupDaoMock;
  @Mock
  private MessageSource messageSourceMock;
  @Mock
  private ControllerUtil controllerUtilMock;
  @Mock
  private LocaleResolver localeResolverMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void testDetailTeamNotMember() throws Exception {
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Admin), person);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));
    Invitation invitation = new Invitation("john@example.com", "teamId");
    JoinTeamRequest joinRequest = new JoinTeamRequest("id", "groupId", "email", "displayName");

    when(environmentMock.acceptsProfiles(Application.GROUPZY_PROFILE_NAME)).thenReturn(true);
    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(grouperTeamServiceMock.findAdmins(team)).thenReturn(ImmutableSet.of(member));
    when(teamInviteServiceMock.findInvitationsForTeamExcludeAccepted(team)).thenReturn(ImmutableList.of(invitation));
    when(joinTeamRequestServiceMock.findPendingRequests("teamId")).thenReturn(ImmutableList.of(joinRequest));
    when(teamExternalGroupDaoMock.getByTeamIdentifier("teamId")).thenReturn(ImmutableList.of());

    mockMvc.perform(get("/detailteam.shtml")
        .param("team", "teamId")
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(view().name("detailteam"))
      .andExpect(model().attribute("team", is(team)))
      .andExpect(model().attribute("onlyAdmin", is(true)))
      .andExpect(model().attribute(ROLE_PARAM, is(Role.Admin)))
      .andExpect(model().attribute(PENDING_REQUESTS_PARAM, contains(person)))
      .andExpect(model().attribute(INVITATIONS_PARAM, contains(invitation)))
      .andExpect(model().attribute("groupzyEnabled", is(true)));
    }

  @Test
  public void testDetailTeamManager() throws Exception {
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Manager), person);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));
    Invitation invitation = new Invitation("john@example.com", "teamId");
    JoinTeamRequest joinRequest = new JoinTeamRequest("id", "groupId", "email", "displayName");

    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(grouperTeamServiceMock.findAdmins(team)).thenReturn(ImmutableSet.of(member));
    when(teamInviteServiceMock.findInvitationsForTeamExcludeAccepted(team)).thenReturn(ImmutableList.of(invitation));
    when(joinTeamRequestServiceMock.findPendingRequests("teamId")).thenReturn(ImmutableList.of(joinRequest));
    when(teamExternalGroupDaoMock.getByTeamIdentifier("teamId")).thenReturn(ImmutableList.of());

    mockMvc.perform(get("/detailteam.shtml")
        .param("team", "teamId")
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(view().name("detailteam"))
      .andExpect(model().attribute("team", is(team)))
      .andExpect(model().attribute("onlyAdmin", is(true)))
      .andExpect(model().attribute(ROLE_PARAM, is(Role.Manager)))
      .andExpect(model().attribute(PENDING_REQUESTS_PARAM, contains(person)))
      .andExpect(model().attribute(INVITATIONS_PARAM, contains(invitation)));
    }

  @Test
  public void teamDetailWithAMessage() throws Exception {
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Member), person);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));

    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(grouperTeamServiceMock.findAdmins(team)).thenReturn(ImmutableSet.of(member));
    when(teamInviteServiceMock.findInvitationsForTeamExcludeAccepted(team)).thenReturn(ImmutableList.of());
    when(messageSourceMock.getMessage(eq("IAmAMessage"), eq(new Object[] {}), any(Locale.class))).thenReturn("message");

    mockMvc.perform(get("/detailteam.shtml")
        .param("team", "teamId")
        .param("mes", "IAmAMessage")
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(view().name("detailteam"))
      .andExpect(model().attribute("message", is("IAmAMessage")));
    }

  @Test
  public void testLeaveTeamHappyFlow() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Person otherAdminPerson = new Person("i", "n", "e", "o", "v", "d");
    Member member = new Member(ImmutableSet.of(Role.Admin), person);
    Member otherAdminMember = new Member(ImmutableSet.of(Role.Member), otherAdminPerson);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));

    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(grouperTeamServiceMock.findAdmins(team)).thenReturn(ImmutableSet.of(member, otherAdminMember));

    mockMvc.perform(post("/doleaveteam.shtml")
        .param("token", dummyToken)
        .param("team", "teamId")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("home.shtml?teams=my"));

    verify(grouperTeamServiceMock).deleteMember("teamId", "id");
  }

  @Test
  public void testLeaveTeam() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Admin), person);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));

    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(grouperTeamServiceMock.findAdmins(team)).thenReturn(ImmutableSet.of(member));

    mockMvc.perform(post("/doleaveteam.shtml")
        .param("token", dummyToken)
        .param("team", "teamId")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("detailteam.shtml?team=teamId&mes=error.AdminCannotLeaveTeam"));

    verify(grouperTeamServiceMock, never()).deleteMember("teamId", "id");
  }

  @Test
  public void testDeleteTeamHappyFlow() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Admin), person);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));
    Invitation invitation = new Invitation("john@example.com", "teamId");
    TeamExternalGroup externalGroup= new TeamExternalGroup();

    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(grouperTeamServiceMock.findMember("teamId", "id")).thenReturn(member);
    when(teamInviteServiceMock.findAllInvitationsForTeam(team)).thenReturn(ImmutableList.of(invitation));
    when(teamExternalGroupDaoMock.getByTeamIdentifier("teamId")).thenReturn(ImmutableList.of(externalGroup));

    mockMvc.perform(post("/dodeleteteam.shtml")
        .param("token", dummyToken)
        .param("team", "teamId")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("home.shtml?teams=my"));

    verify(teamInviteServiceMock).delete(invitation);
    verify(teamExternalGroupDaoMock).delete(externalGroup);
    verify(grouperTeamServiceMock).deleteTeam("teamId");
  }

  @Test
  public void testDeleteTeam() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Member), person);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));

    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(grouperTeamServiceMock.findMember("teamId", "id")).thenReturn(member);

    mockMvc.perform(post("/dodeleteteam.shtml")
        .param("token", dummyToken)
        .param("team", "teamId")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("detailteam.shtml?team=teamId"));

    verify(grouperTeamServiceMock, never()).deleteTeam("teamId");
  }

  @Test
  public void testDeleteMemberHappyFlow() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Admin), person);
    Person person2 = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member memberToRemove = new Member(ImmutableSet.of(Role.Member), person2);

    when(grouperTeamServiceMock.findMember("teamId", "id")).thenReturn(member);
    when(grouperTeamServiceMock.findMember("teamId", "memberId")).thenReturn(memberToRemove);

    mockMvc.perform(get("/dodeletemember.shtml")
        .param("token", dummyToken)
        .param("team", "teamId")
        .param(MEMBER_PARAM, "memberId")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("detailteam.shtml?team=teamId"));

    verify(grouperTeamServiceMock).deleteMember("teamId", "memberId");
  }

  @Test
  public void aMemberShouldNotBeAbleToDeleteAMember() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Member), person);
    Person person2 = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member memberToRemove = new Member(ImmutableSet.of(Role.Member), person2);

    when(grouperTeamServiceMock.findMember("teamId", "id")).thenReturn(member);
    when(grouperTeamServiceMock.findMember("teamId", "memberId")).thenReturn(memberToRemove);

    mockMvc.perform(get("/dodeletemember.shtml")
        .param("token", dummyToken)
        .param("team", "teamId")
        .param(MEMBER_PARAM, "memberId")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("detailteam.shtml?team=teamId&mes=error.NotAuthorizedToDeleteMember"));

    verify(grouperTeamServiceMock, never()).deleteMember("teamId", "memberId");
  }

  @Test
  public void aManagerCanNotDeleteAnAdmin() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Manager), person);
    Person person2 = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member memberToRemove = new Member(ImmutableSet.of(Role.Admin), person2);

    when(grouperTeamServiceMock.findMember("teamId", "id")).thenReturn(member);
    when(grouperTeamServiceMock.findMember("teamId", "memberId")).thenReturn(memberToRemove);

    mockMvc.perform(get("/dodeletemember.shtml")
        .param("token", dummyToken)
        .param("team", "teamId")
        .param(MEMBER_PARAM, "memberId")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("detailteam.shtml?team=teamId&mes=error.NotAuthorizedToDeleteMember"));

    verify(grouperTeamServiceMock, never()).deleteMember("teamId", "memberId");
  }

  @Test
  public void testAddRoleHappyFlow() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Person personToAddRole = new Person("personId", "name", "email", "organization", "voot_role", "displayName");
    Member memberToAddRole = new Member(ImmutableSet.of(Role.Member), personToAddRole);

    when(grouperTeamServiceMock.findMember("teamId", "personId")).thenReturn(memberToAddRole);
    when(grouperTeamServiceMock.addMemberRole("teamId", "personId", Role.Manager, "id")).thenReturn(true);

    mockMvc.perform(post("/doaddremoverole.shtml")
        .param("token", dummyToken)
        .param("teamId", "teamId")
        .param("memberId", "personId")
        .param("roleId", Role.Manager.name())
        .param("doAction", "add")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("detailteam.shtml?team=teamId&mes=role.added&offset=0"));
  }

  @Test
  public void testAddRoleNotAuthorized() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Person personToAddRole = new Person("personId", "name", "email", "organization", "voot_role", "displayName");
    Member memberToAddRole = new Member(ImmutableSet.of(Role.Member), personToAddRole);

    when(grouperTeamServiceMock.findMember("teamId", "personId")).thenReturn(memberToAddRole);
    when(grouperTeamServiceMock.addMemberRole("teamId", "personId", Role.Manager, "id")).thenReturn(false);

    mockMvc.perform(post("/doaddremoverole.shtml")
        .param("token", dummyToken)
        .param("teamId", "teamId")
        .param("memberId", "personId")
        .param("roleId", Role.Manager.name())
        .param("doAction", "add")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("detailteam.shtml?team=teamId&mes=no.role.added&offset=0"));
  }

  @Test
  public void testRemoveRoleHappyFlow() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Manager), person);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));

    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(grouperTeamServiceMock.removeMemberRole("teamId", "personId", Role.Manager, "id")).thenReturn(true);

    mockMvc.perform(post("/doaddremoverole.shtml")
        .param("token", dummyToken)
        .param("teamId", "teamId")
        .param("memberId", "personId")
        .param("roleId", "1")
        .param("doAction", "remove")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("detailteam.shtml?team=teamId&mes=role.removed&offset=0"));
  }

  @Test
  public void testRemoveRoleOneAdmin() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Admin), person);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));

    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(grouperTeamServiceMock.removeMemberRole("teamId", "personId", Role.Manager, "id")).thenReturn(true);
    when(grouperTeamServiceMock.findAdmins(team)).thenReturn(ImmutableSet.of(member));

    mockMvc.perform(post("/doaddremoverole.shtml")
        .param("token", dummyToken)
        .param("teamId", "teamId")
        .param("memberId", "personId")
        .param("roleId", "0")
        .param("doAction", "remove")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("detailteam.shtml?team=teamId&mes=no.role.added.admin.status&offset=0"));
  }

  @Test
  public void testRemoveRoleException() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");

    mockMvc.perform(post("/doaddremoverole.shtml")
        .param("token", dummyToken)
        .param("doAction", "remove")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("home.shtml?teams=my"));
  }

  @Test
  public void removeJoinRequestWithEmail() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Admin), person);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));
    JoinTeamRequest joinRequest = new JoinTeamRequest("member", "teamId", "email", "displayName");

    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(joinTeamRequestServiceMock.findPendingRequest("member", "teamId")).thenReturn(joinRequest);
    when(controllerUtilMock.hasUserAdministrativePrivileges(person, "teamId")).thenReturn(true);
    when(localeResolverMock.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);

    mockMvc.perform(post("/dodeleterequest.shtml")
        .param("token", dummyToken)
        .param("team", "teamId")
        .param(MEMBER_PARAM, "member")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("detailteam.shtml?team=teamId"));

    verify(controllerUtilMock).sendDeclineMail(new Person("member", "", "email", "", "", ""), team, Locale.ENGLISH);
  }

  @Test
  public void removeJoinRequestWithNoEmail() throws Exception {
    String dummyToken = TokenUtil.generateSessionToken();
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Admin), person);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));
    JoinTeamRequest joinRequest = new JoinTeamRequest("member", "teamId", null, "displayName");

    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(joinTeamRequestServiceMock.findPendingRequest("member", "teamId")).thenReturn(joinRequest);
    when(controllerUtilMock.hasUserAdministrativePrivileges(person, "teamId")).thenReturn(true);
    when(localeResolverMock.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);

    mockMvc.perform(post("/dodeleterequest.shtml")
        .param("token", dummyToken)
        .param("team", "teamId")
        .param(MEMBER_PARAM, "member")
        .sessionAttr(TOKENCHECK, dummyToken)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(redirectedUrl("detailteam.shtml?team=teamId"));

    verify(controllerUtilMock, never()).sendDeclineMail(person, team, Locale.ENGLISH);
  }
}
