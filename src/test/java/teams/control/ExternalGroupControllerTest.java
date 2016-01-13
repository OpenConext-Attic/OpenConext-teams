package teams.control;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;

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
import teams.interceptor.LoginInterceptor;
import teams.service.VootClient;

@RunWith(MockitoJUnitRunner.class)
public class ExternalGroupControllerTest {

  @InjectMocks
  private ExternalGroupController subject;

  @Mock
  private VootClient vootClientMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void whenExternalGroupsAreFoundInTheSession() throws Exception {
    ExternalGroupProvider externalGroupProvider = new ExternalGroupProvider("externalGroupProviderId", "externalGroupProviderName");
    ExternalGroup externalGroup = new ExternalGroup("groupId", "eg-name-1", "eg-desc-1", externalGroupProvider);

    mockMvc.perform(get("/externalgroups/groupdetail.shtml")
        .param("groupId", "groupId")
        .sessionAttr(LoginInterceptor.EXTERNAL_GROUPS_SESSION_KEY, ImmutableList.of(externalGroup)))
      .andExpect(view().name("external-groupdetail"))
      .andExpect(model().attribute("groupProvider", is(externalGroupProvider)))
      .andExpect(model().attribute("externalGroup", is(externalGroup)));
  }

  @Test
  public void whenNoExternalGroupsAreFoundInTheSession() throws Exception {
    Person person = new Person("id", "name", "email", "organization", "voot_role", "displayName");
    ExternalGroupProvider externalGroupProvider = new ExternalGroupProvider("externalGroupProviderId", "externalGroupProviderName");
    ExternalGroup externalGroup = new ExternalGroup("groupId", "eg-name-1", "eg-desc-1", externalGroupProvider);

    when(vootClientMock.groups("id")).thenReturn(ImmutableList.of(externalGroup));

    mockMvc.perform(get("/externalgroups/groupdetail.shtml")
        .param("groupId", "groupId")
        .sessionAttr(PERSON_SESSION_KEY, person))
      .andExpect(view().name("external-groupdetail"))
      .andExpect(model().attribute("groupProvider", is(externalGroupProvider)))
      .andExpect(model().attribute("externalGroup", is(externalGroup)));
  }

}
