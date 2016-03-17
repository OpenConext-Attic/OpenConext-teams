package teams.control;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static teams.control.AddMemberController.INVITATION_FORM_PARAM;
import static teams.control.AddMemberController.INVITE_SEND_INVITE_SUBJECT;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;
import static teams.util.TokenUtil.TOKENCHECK;

import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;

import teams.domain.Invitation;
import teams.domain.Language;
import teams.domain.Person;
import teams.domain.Team;
import teams.service.TeamInviteService;
import teams.util.ControllerUtil;
import teams.util.TokenUtil;

@RunWith(MockitoJUnitRunner.class)
public class AddMemberControllerTest {

  @InjectMocks
  private AddMemberController subject;

  @Mock
  private ControllerUtil controllerUtilMock;
  @Mock
  private MessageSource messageSourceMock;
  @Mock
  private TeamInviteService teamInviteServiceMock;

  private MockMvc mockMvc;

  private final Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
  private final String dummyToken = TokenUtil.generateSessionToken();

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void addMemberForm() throws Exception {
    Team team = new Team("teamId", "teamName", "teamDescription");

    when(controllerUtilMock.getTeam(any())).thenReturn(team);
    when(controllerUtilMock.hasUserAdministrativePrivileges(person, team)).thenReturn(true);

    mockMvc.perform(get("/addmember.shtml")
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(model().attributeExists("languages"))
      .andExpect(model().attribute(INVITATION_FORM_PARAM, hasProperty("language", is(Language.English))))
      .andExpect(view().name("addmember"));
  }

  @Test
  public void redirectToFromWhenEmailIsMissing() throws Exception {
    Team team = new Team("teamId", "teamName", "teamDescription");

    when(controllerUtilMock.getTeamById(team.getId())).thenReturn(team);
    when(controllerUtilMock.hasUserAdministrativePrivileges(person, team)).thenReturn(true);

    mockMvc.perform(post("/doaddmember.shtml")
        .sessionAttr(PERSON_SESSION_KEY, person)
        .sessionAttr(TOKENCHECK, dummyToken)
        .param("teamId", team.getId())
        .param("token", dummyToken))
      .andExpect(model().attributeHasFieldErrors(INVITATION_FORM_PARAM, "emails"))
      .andExpect(view().name("addmember"));
  }

  @Test
  public void addAMemberToATeam() throws Exception {
    Team team = new Team("teamId", "teamName", "teamDescription");

    when(controllerUtilMock.hasUserAdministrativePrivileges(person, team)).thenReturn(true);
    when(controllerUtilMock.getTeamById(team.getId())).thenReturn(team);
    when(messageSourceMock.getMessage(eq(INVITE_SEND_INVITE_SUBJECT), any(), eq(Locale.forLanguageTag("nl")))).thenReturn("subject");
    when(teamInviteServiceMock.findOpenInvitation("john@example.com", team)).thenReturn(Optional.empty());

    mockMvc.perform(post("/doaddmember.shtml")
        .sessionAttr(PERSON_SESSION_KEY, person)
        .sessionAttr(TOKENCHECK, dummyToken)
        .param("teamId", team.getId())
        .param("emails", "john@example.com")
        .param("language", "Dutch")
        .param("token", dummyToken))
      .andExpect(view().name("redirect:detailteam.shtml?team=teamId"));

    ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);

    verify(controllerUtilMock).sendInvitationMail(invitationCaptor.capture(), eq("subject"), eq(person));
    assertThat(invitationCaptor.getValue().getEmail(), is("john@example.com"));
  }

  @Test
  public void resendAnInvitationForm() throws Exception {
    Invitation invitation = new Invitation("john@example.com", "teamId");

    when(teamInviteServiceMock.findInvitationByInviteId("invitationId")).thenReturn(Optional.of(invitation));
    when(controllerUtilMock.hasUserAdministrativePrivileges(person, new Team("teamId"))).thenReturn(true);

    mockMvc.perform(get("/resendInvitation.shtml")
        .sessionAttr(PERSON_SESSION_KEY, person)
        .param("id", "invitationId"))
    .andExpect(model().attributeExists(AddMemberController.ROLES_PARAM))
    .andExpect(model().attributeExists(AddMemberController.RESEND_INVITATION_COMMAND_PARAM))
    .andExpect(view().name("resendinvitation"));
  }
}
