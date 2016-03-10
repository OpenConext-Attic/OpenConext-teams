package teams.control;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import teams.domain.Pager;
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

  @Test
  public void paginationWhenOnTheFirstPage() throws Exception {
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(new Member(ImmutableSet.of(Role.Admin), person)));
    ExternalGroupProvider externalGroupProvider = new ExternalGroupProvider("externalGroupProviderId", "externalGroupProviderName");

    int offset = 0;

    List<ExternalGroup> externalGroups = IntStream.range(0, 11).boxed()
        .map(i -> new ExternalGroup("eg" + i, "eg-name-" + i, "eg-desc-" + i, externalGroupProvider))
        .collect(toList());

    when(messageSourceMock.getMessage("jsp.home.SearchTeam", null, Locale.ENGLISH)).thenReturn("searchTeam");
    when(grouperTeamServiceMock.findAllTeamsByMember("id", offset, HomeController.PAGESIZE)).thenReturn(new TeamResultWrapper(ImmutableList.of(team), 1, 0, HomeController.PAGESIZE));
    when(vootClientMock.groups("id")).thenReturn(externalGroups);

    mockMvc.perform(get("/")
        .param("team", "teamId")
        .param("groupProviderId", "externalGroupProviderId")
        .param("offset", "" + offset)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(view().name("home"))
      .andExpect(model().attribute("pager", is(new Pager(11, offset, 10))))
      .andExpect(model().attribute("externalGroups", hasSize(10)));
  }

  @Test
  public void paginationWhenOnTheSecondPage() throws Exception {
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    Team team = new Team("teamId", "Team 1", "team description", ImmutableList.of(new Member(ImmutableSet.of(Role.Admin), person)));
    ExternalGroupProvider externalGroupProvider = new ExternalGroupProvider("externalGroupProviderId", "externalGroupProviderName");

    int offset = HomeController.PAGESIZE;

    List<ExternalGroup> externalGroups = IntStream.range(0, HomeController.PAGESIZE + 5).boxed()
        .map(i -> new ExternalGroup("eg" + i, "eg-name-" + i, "eg-desc-" + i, externalGroupProvider))
        .collect(toList());

    when(messageSourceMock.getMessage("jsp.home.SearchTeam", null, Locale.ENGLISH)).thenReturn("searchTeam");
    when(grouperTeamServiceMock.findAllTeamsByMember("id", offset, HomeController.PAGESIZE)).thenReturn(new TeamResultWrapper(ImmutableList.of(team), 1, 0, HomeController.PAGESIZE));
    when(vootClientMock.groups("id")).thenReturn(externalGroups);

    mockMvc.perform(get("/")
        .param("team", "teamId")
        .param("groupProviderId", "externalGroupProviderId")
        .param("offset", "" + offset)
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(view().name("home"))
      .andExpect(model().attribute("pager", is(new Pager(15, offset, 10))))
      .andExpect(model().attribute("externalGroups", hasSize(5)));
  }
}
