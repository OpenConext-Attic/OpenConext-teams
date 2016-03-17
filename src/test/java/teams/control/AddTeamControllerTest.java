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

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static teams.control.AddMemberController.INVITE_SEND_INVITE_SUBJECT;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;
import static teams.interceptor.LoginInterceptor.USER_STATUS_SESSION_KEY;
import static teams.util.TokenUtil.TOKENCHECK;

import java.util.Locale;

import com.google.common.collect.ImmutableList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import teams.Application;
import teams.domain.Invitation;
import teams.domain.Language;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Stem;
import teams.domain.Team;
import teams.service.GrouperTeamService;
import teams.service.TeamInviteService;
import teams.util.ControllerUtil;
import teams.util.TokenUtil;

@RunWith(MockitoJUnitRunner.class)
public class AddTeamControllerTest {

  @InjectMocks
  private AddTeamController subject;

  @Mock private GrouperTeamService grouperTeamServiceMock;
  @Mock private Environment environment;
  @Mock private ControllerUtil controllerUtil;
  @Mock private TeamInviteService teamInviteServiceMock;
  @Mock private MessageSource messageSourceMock;

  private MockMvc mockMvc;

  private final Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
  private final String dummyToken = TokenUtil.generateSessionToken();

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void addTeamForm() throws Exception {
    mockMvc.perform(get("/addteam.shtml")
        .sessionAttr(USER_STATUS_SESSION_KEY, "member")
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(model().attributeExists("languages"))
      .andExpect(model().attribute("addTeamCommand", hasProperty("admin2Language", is(Language.English))))
      .andExpect(view().name("addteam"));
  }

  @Test
  public void addTeamHappyFlow() throws Exception {
    Team expectedTeam = new Team("teamid");

    when(environment.acceptsProfiles(Application.GROUPZY_PROFILE_NAME)).thenReturn(false);
    when(grouperTeamServiceMock.addTeam("name", "name", "description", null)).thenReturn("teamid");
    when(grouperTeamServiceMock.findTeamById(expectedTeam.getId())).thenReturn(expectedTeam);

    mockMvc.perform(post("/doaddteam.shtml")
        .sessionAttr(PERSON_SESSION_KEY, person)
        .sessionAttr(USER_STATUS_SESSION_KEY, "member")
        .sessionAttr(TOKENCHECK, dummyToken)
        .param("token", dummyToken)
        .param("teamDescription", "description")
        .param("viewable", "true")
        .param("teamName", "name"))
      .andExpect(view().name("redirect:detailteam.shtml?team=teamid"));

    verify(grouperTeamServiceMock).setVisibilityGroup("teamid", true);
    verify(grouperTeamServiceMock).addMember(expectedTeam, person);
    verify(grouperTeamServiceMock).addMemberRole(expectedTeam, person.getId(), Role.Admin, null);
  }

  @Test
  public void failToAddTeamWithEmptyName() throws Exception {
    when(environment.acceptsProfiles(Application.GROUPZY_PROFILE_NAME)).thenReturn(false);
    when(grouperTeamServiceMock.addTeam("name", "name", "description", null)).thenReturn("teamid");

    mockMvc.perform(post("/doaddteam.shtml")
        .sessionAttr(PERSON_SESSION_KEY, person)
        .sessionAttr(USER_STATUS_SESSION_KEY, "member")
        .sessionAttr(TOKENCHECK, dummyToken)
        .param("token", dummyToken)
        .param("teamDescription", "description")
        .param("teamName", ""))
      .andExpect(view().name("addteam"))
      .andExpect(model().attributeHasFieldErrors("addTeamCommand", "teamName"));
  }

  @Test
  public void addTeamWithStem() throws Exception {
    Team team = new Team("teamId", "teamName", "teamDescription");
    Team teamStem = new Team("stemId:members", "", "");
    Stem stem = new Stem("stemId", "stemName", "stemDescription");

    when(grouperTeamServiceMock.findStemsByMember(person.getId())).thenReturn(ImmutableList.of(stem));
    when(grouperTeamServiceMock.findTeamById("teamId")).thenReturn(team);
    when(grouperTeamServiceMock.findTeamById("stemId:members")).thenReturn(teamStem);
    when(controllerUtil.isPersonMemberOfTeam(person, teamStem)).thenReturn(true);

    when(grouperTeamServiceMock.addTeam("name", "name", "description", "stemId")).thenReturn("created");

    mockMvc.perform(post("/doaddteam.shtml")
        .sessionAttr(PERSON_SESSION_KEY, person)
        .sessionAttr(USER_STATUS_SESSION_KEY, "member")
        .sessionAttr(TOKENCHECK, dummyToken)
        .param("token", dummyToken)
        .param("teamDescription", "description")
        .param("teamName", "name")
        .param("stem", "stemId"))
    .andExpect(view().name("redirect:detailteam.shtml?team=created"));
  }

  @Test
  public void ampersandInTeamIdShouldGetUrlEscaped() throws Exception {
    when(environment.acceptsProfiles(Application.GROUPZY_PROFILE_NAME)).thenReturn(false);
    when(grouperTeamServiceMock.addTeam("name", "name", "description", null)).thenReturn("Henk & Truus");

    mockMvc.perform(post("/doaddteam.shtml")
        .sessionAttr(PERSON_SESSION_KEY, person)
        .sessionAttr(USER_STATUS_SESSION_KEY, "member")
        .sessionAttr(TOKENCHECK, dummyToken)
        .param("token", dummyToken)
        .param("teamDescription", "description")
        .param("teamName", "name"))
      .andExpect(view().name("redirect:detailteam.shtml?team=Henk+%26+Truus"));
  }


  @Test
  public void addTeamWithSecondAdminShouldSendAnEmailWithCorrectLocale() throws Exception {
    when(environment.acceptsProfiles(Application.GROUPZY_PROFILE_NAME)).thenReturn(false);
    when(grouperTeamServiceMock.addTeam("name", "name", "description", null)).thenReturn("created");
    when(grouperTeamServiceMock.findTeamById("created")).thenReturn(new Team("created"));

    when(messageSourceMock.getMessage(INVITE_SEND_INVITE_SUBJECT, new Object[] {"name"}, Locale.forLanguageTag("nl"))).thenReturn("subject");

    mockMvc.perform(post("/doaddteam.shtml")
        .sessionAttr(PERSON_SESSION_KEY, person)
        .sessionAttr(USER_STATUS_SESSION_KEY, "member")
        .sessionAttr(TOKENCHECK, dummyToken)
        .param("token", dummyToken)
        .param("teamDescription", "description")
        .param("teamName", "name")
        .param("admin2Email", "henk@example.com")
        .param("admin2Language", "Dutch")
        .param("admin2Message", "message"))
      .andExpect(view().name("redirect:detailteam.shtml?team=created"));

    ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);
    verify(teamInviteServiceMock).saveOrUpdate(invitationCaptor.capture());
    verify(controllerUtil).sendInvitationMail(new Team("created"), invitationCaptor.getValue(), "subject", person);

    assertThat(invitationCaptor.getValue().getEmail(), is("henk@example.com"));
    assertThat(invitationCaptor.getValue().getLanguage(), is(Language.Dutch));
  }
}
