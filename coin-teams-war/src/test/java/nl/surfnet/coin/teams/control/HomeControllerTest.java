/*
 * Copyright 2011 SURFnet bv, The Netherlands
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

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

import java.util.ArrayList;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

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

    autoWireMock(homeController, new Returns(DEFAULTSTEM), TeamEnvironment.class);
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH), LocaleResolver.class);
    autoWireMock(homeController, getMyTeamReturn(), TeamService.class);

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
    autoWireMock(homeController, getAllTeamReturn(), TeamService.class);

    homeController.start(getModelMap(), request);
    @SuppressWarnings("unchecked")
    ArrayList<Team> teams = (ArrayList<Team>) getModelMap().get("teams");
    String display = (String) getModelMap().get("display");
    String query = (String) getModelMap().get("query");

    assertEquals(6, teams.size());
    assertEquals("all", display);
    assertNull(query);
  }

  @Test
  public void testStartSearchMyTeams() throws Exception {
    MockHttpServletRequest request = getRequest();
    // This requests my teams
    request.setParameter("teams", "my");
    request.setParameter("teamSearch", "1");

    autoWireMock(homeController, new Returns(DEFAULTSTEM), TeamEnvironment.class);
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH), LocaleResolver.class);
    autoWireMock(homeController, getSearchTeamReturn(), TeamService.class);

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
