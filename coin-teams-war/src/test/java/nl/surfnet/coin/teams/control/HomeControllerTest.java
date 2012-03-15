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

import java.util.ArrayList;
import java.util.Locale;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.opensocial.models.Person;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author steinwelberg
 */
public class HomeControllerTest extends AbstractControllerTest {

  private HomeController homeController = new HomeController();
  private static final String DEFAULTSTEM = "nl:surfnet:diensten";

  @Test
  public void testStartMyTeams() throws Exception {
    MockHttpServletRequest request = getRequest();
    // This requests my teams
    request.setParameter("teams", "my");
    request.setParameter("teamSearch", "query");

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findAllTeamsByMember(getMember().getId(), 0, 10)).thenReturn(getMyTeams());
    when(grouperTeamService.findStemsByMember(getMember().getId())).thenReturn(getStems());

    autoWireMock(homeController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(homeController, new Returns(DEFAULTSTEM), TeamEnvironment.class);
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    homeController.start(getModelMap(), request);
    @SuppressWarnings("unchecked")
    ArrayList<Team> teams = (ArrayList<Team>) getModelMap().get("teams");
    String display = (String) getModelMap().get("display");

    assertEquals(3, teams.size());
    assertEquals("my", display);
  }

  @Test
  public void testStartAllTeams() throws Exception {
    MockHttpServletRequest request = getRequest();
    // This requests my teams
    request.setParameter("teams", "all");

    autoWireMock(homeController, new Returns(DEFAULTSTEM), TeamEnvironment.class);
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH), LocaleResolver.class);
    autoWireMock(homeController, getAllTeamReturn(), GrouperTeamService.class);

    RequestContextHolder.setRequestAttributes(getRequestAttributes(), true);
    
    homeController.start(getModelMap(), request);
    @SuppressWarnings("unchecked")
    ArrayList<Team> teams = (ArrayList<Team>) getModelMap().get("teams");
    String display = (String) getModelMap().get("display");
    String query = (String) getModelMap().get("query");

    assertEquals(6, teams.size());
    assertEquals("all", display);
    assertNull(query);
  }

  /**
   * @return
   */
  private RequestAttributes getRequestAttributes() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    Person person = new Person();
    person.setField("id","test");
    session.setAttribute(LoginInterceptor.PERSON_SESSION_KEY, person);
    request.setSession(session);
    return new ServletRequestAttributes(request);
  }

  @Test
  public void testStartSearchMyTeams() throws Exception {
    MockHttpServletRequest request = getRequest();
    // This requests my teams
    request.setParameter("teams", "my");
    request.setParameter("teamSearch", "1");

    GrouperTeamService grouperTeamService = mock(GrouperTeamService.class);
    when(grouperTeamService.findStemsByMember(getMember().getId())).thenReturn(getStems());
    when(grouperTeamService.findTeamsByMember(getMember().getId(), "1", 0, 10)).thenReturn(getSearchTeams());

    autoWireMock(homeController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(homeController, new Returns(DEFAULTSTEM), TeamEnvironment.class);
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    homeController.start(getModelMap(), request);
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
