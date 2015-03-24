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

/**
 *
 */
package nl.surfnet.coin.teams.control;

import nl.surfnet.coin.api.client.domain.Group20;
import nl.surfnet.coin.api.client.domain.Group20Entry;
import nl.surfnet.coin.teams.domain.Person;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.GroupProviderType;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.ExternalGroupProviderProcessor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HomeControllerTest extends AbstractControllerTest {

  private HomeController homeController = new HomeController();
  private static final String DEFAULTSTEM = "nl:surfnet:diensten";

  @Test
  public void testStartMyTeams() throws Exception {
    MockHttpServletRequest request = getRequest();

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findAllTeamsByMember(getMember().getId(), 0, 10)).thenReturn(getMyTeams());
    when(grouperTeamService.findStemsByMember(getMember().getId())).thenReturn(getStems());

    ExternalGroupProviderProcessor processor = mock(ExternalGroupProviderProcessor.class);
    when(processor.getAllGroupProviders()).thenReturn(Collections.<GroupProvider>emptyList());

    autoWireMock(homeController, processor, ExternalGroupProviderProcessor.class);
    autoWireMock(homeController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(homeController, new Returns(DEFAULTSTEM), TeamEnvironment.class);
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    homeController.start(getModelMap(), request, "my", "query", null);
    @SuppressWarnings("unchecked")
    ArrayList<Team> teams = (ArrayList<Team>) getModelMap().get("teams");
    String display = (String) getModelMap().get("display");

    assertEquals(3, teams.size());
    assertEquals("my", display);
  }

  @Test
  public void testStartMyTeams_withExternalTeams() throws Exception {
    MockHttpServletRequest request = getRequest();
    // This requests my teams

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
//    when(grouperTeamService.findAllTeamsByMember(getMember().getId(), 0, 10)).thenReturn(getMyTeams());
    when(grouperTeamService.findStemsByMember(getMember().getId())).thenReturn(getStems());

    ExternalGroupProviderProcessor processor = mock(ExternalGroupProviderProcessor.class);
    
    GroupProvider groupProvider = new GroupProvider(1L, "uvh", "Universiteit van Harderwijk",
        GroupProviderType.OAUTH_THREELEGGED.getStringValue());
    List<GroupProvider> groupProviders = Collections.<GroupProvider>singletonList(groupProvider);
    Group20Entry entry = new Group20Entry();
    Group20 group20 = new Group20();
    group20.setId("externalGroupId");
    group20.setTitle("External Group");
    List<Group20> group20s = new ArrayList<Group20>();
    group20s.add(group20);
    entry.setEntry(group20s);

    when(processor.getAllGroupProviders()).thenReturn(groupProviders);
    when(processor.getGroupProvidersForUser("member-1", groupProviders)).thenReturn(groupProviders);
    when(processor.getGroupProviderByLongIdentifier(1L,groupProviders)).thenReturn(groupProvider);
    when(processor.getExternalGroupsForGroupProviderId(groupProvider,"member-1",0,10)).thenReturn(entry);

    autoWireMock(homeController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(homeController, processor, ExternalGroupProviderProcessor.class);
    autoWireMock(homeController, new Returns(DEFAULTSTEM), TeamEnvironment.class);
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    homeController.start(getModelMap(), request, "externalGroups", "query", 1L);
    @SuppressWarnings("unchecked")
    List<Team> teams = (List<Team>) getModelMap().get("teams");
    @SuppressWarnings("unchecked")
    final Group20Entry group20Entry = (Group20Entry) getModelMap().get("group20Entry");
    String display = (String) getModelMap().get("display");

    assertNull(teams);
    assertEquals(group20Entry.getEntry(), group20s);
    assertEquals("externalGroups", display);
    //noinspection unchecked
    assertEquals(groupProvider, ((List<GroupProvider>) getModelMap().get("groupProviders")).get(0));
  }

  @Test
  public void testStartAllTeams() throws Exception {
    MockHttpServletRequest request = getRequest();

    ExternalGroupProviderProcessor processor = mock(ExternalGroupProviderProcessor.class);

    autoWireMock(homeController, processor, ExternalGroupProviderProcessor.class);
    autoWireMock(homeController, new Returns(DEFAULTSTEM), TeamEnvironment.class);
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH), LocaleResolver.class);
    autoWireMock(homeController, getAllTeamReturn(), GrouperTeamService.class);

    RequestContextHolder.setRequestAttributes(getRequestAttributes(), true);

    homeController.start(getModelMap(), request, "all", null, null);
    @SuppressWarnings("unchecked")
    ArrayList<Team> teams = (ArrayList<Team>) getModelMap().get("teams");
    String display = (String) getModelMap().get("display");
    String query = (String) getModelMap().get("query");

    assertEquals(0, teams.size());
    assertEquals("all", display);
    assertNull(query);
  }

  private RequestAttributes getRequestAttributes() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    Person person = new Person();
    person.setId("test");
    session.setAttribute(LoginInterceptor.PERSON_SESSION_KEY, person);
    request.setSession(session);
    return new ServletRequestAttributes(request);
  }

  @Test
  public void testStartSearchMyTeams() throws Exception {
    MockHttpServletRequest request = getRequest();

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findStemsByMember(getMember().getId())).thenReturn(getStems());
    when(grouperTeamService.findTeamsByMember(getMember().getId(), "1", 0, 10)).thenReturn(getSearchTeams());

    ExternalGroupProviderProcessor processor = mock(ExternalGroupProviderProcessor.class);
    autoWireMock(homeController, processor, ExternalGroupProviderProcessor.class);

    autoWireMock(homeController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(homeController, new Returns(DEFAULTSTEM), TeamEnvironment.class);
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    homeController.start(getModelMap(), request, "my", "1", null);
    @SuppressWarnings("unchecked")
    ArrayList<Team> teams = (ArrayList<Team>) getModelMap().get("teams");
    String display = (String) getModelMap().get("display");
    String query = (String) getModelMap().get("query");

    assertEquals(1, teams.size());
    assertEquals("my", display);
    assertEquals("1", query);
  }

  @Override
  public void setup() throws Exception {
    super.setup();
    TeamEnvironment mockTeamEnvironment = mock(TeamEnvironment.class);
    mockTeamEnvironment.setDefaultStemName("nl:surfnet:diensten");
    homeController.setTeamEnvironment(mockTeamEnvironment);
  }
}
