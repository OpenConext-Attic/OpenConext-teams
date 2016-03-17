package teams.control;

import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static teams.control.AddExternalGroupController.EXTERNAL_GROUPS_SESSION_KEY;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;

import java.util.Collections;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;

import teams.domain.ExternalGroup;
import teams.domain.ExternalGroupProvider;
import teams.domain.Person;
import teams.domain.Team;
import teams.domain.TeamExternalGroup;
import teams.service.GrouperTeamService;
import teams.service.TeamExternalGroupDao;
import teams.service.VootClient;
import teams.util.ControllerUtil;

@RunWith(MockitoJUnitRunner.class)
public class AddExternalGroupControllerTest {

  @InjectMocks
  private AddExternalGroupController subject;

  @Mock
  private ControllerUtil controllerUtilMock;

  @Mock
  private GrouperTeamService teamServiceMock;

  @Mock
  private TeamExternalGroupDao teamExternalGroupDaoMock;

  @Mock
  private VootClient vootClientMock;

  private final Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void addExternalGroupForm() throws Exception {
    Team team = new Team("team::id", "teamName", "teamDescription");

    when(teamServiceMock.findTeamById("team::id")).thenReturn(team);
    when(controllerUtilMock.hasUserAdministrativePrivileges(person, team)).thenReturn(true);
    when(vootClientMock.groups("id")).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/addexternalgroup.shtml")
        .param("teamId", "team::id")
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(model().attribute("teamId", "team::id"))
      .andExpect(view().name("addexternalgroup"));
  }

  @Test
  public void addExternalGroupFormShouldSetExternalGroupsInSession() throws Exception {
    Team team = new Team("team::id", "teamName", "teamDescription");

    ExternalGroupProvider groupProvider = new ExternalGroupProvider("egp::1", "egp-one");
    ExternalGroup externalGroupOne = new ExternalGroup("externalgroup::1", "eg-one", "eg-one", groupProvider);
    ExternalGroup externalGroupTwo = new ExternalGroup("externalgroup::2", "eg-two", "eg-two", groupProvider);
    TeamExternalGroup teamExternalGroup = new TeamExternalGroup();
    teamExternalGroup.setExternalGroup(externalGroupOne);

    when(teamServiceMock.findTeamById("team::id")).thenReturn(team);
    when(controllerUtilMock.hasUserAdministrativePrivileges(person, team)).thenReturn(true);
    when(vootClientMock.groups("id")).thenReturn(ImmutableList.of(externalGroupOne, externalGroupTwo));
    when(teamExternalGroupDaoMock.getByTeamIdentifier("team::id")).thenReturn(ImmutableList.of(teamExternalGroup));

    mockMvc.perform(get("/addexternalgroup.shtml")
        .param("teamId", "team::id")
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(request().sessionAttribute(EXTERNAL_GROUPS_SESSION_KEY, contains(externalGroupTwo)));
  }
}
