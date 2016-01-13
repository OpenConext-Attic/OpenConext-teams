package teams.control;

import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;

import java.util.Locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;

import teams.domain.ExternalGroup;
import teams.domain.ExternalGroupProvider;
import teams.domain.Member;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Team;
import teams.domain.TeamResultWrapper;
import teams.service.GrouperTeamService;
import teams.service.TeamInviteService;
import teams.service.VootClient;

@RunWith(MockitoJUnitRunner.class)
public class HomeControllerTest {

  @InjectMocks
  private HomeController subject;

  @Mock
  private MessageSource messageSourceMock;

  @Mock
  private GrouperTeamService grouperTeamServiceMock;

  @Mock
  private TeamInviteService teamInviteServiceMock;

  @Mock
  private VootClient vootClientMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void homeAddsExternalGroupProvidersToModel() throws Exception {
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Member member = new Member(ImmutableSet.of(Role.Admin), person);
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(member));
    TeamResultWrapper teamResultWrapper = new TeamResultWrapper(ImmutableList.of(team), 1, 0, 10);
    ExternalGroupProvider externalGroupProvider = new ExternalGroupProvider("externalGroupProviderId", "externalGroupProviderName");
    ExternalGroup externalGroup1 = new ExternalGroup("eg1", "eg-name-1", "eg-desc-1", externalGroupProvider);
    ExternalGroup externalGroup2 = new ExternalGroup("eg2", "eg-name-2", "eg-desc-2", externalGroupProvider);

    when(messageSourceMock.getMessage("jsp.home.SearchTeam", null, Locale.ENGLISH)).thenReturn("searchTeam");
    when(grouperTeamServiceMock.findAllTeamsByMember("id", 0, HomeController.PAGESIZE)).thenReturn(teamResultWrapper);
    when(vootClientMock.groups("id")).thenReturn(ImmutableList.of(externalGroup1, externalGroup2));

    mockMvc.perform(get("/")
        .param("team", "teamId")
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(view().name("home"))
      .andExpect(model().attribute("groupProviders", contains(externalGroupProvider)));
  }
}
