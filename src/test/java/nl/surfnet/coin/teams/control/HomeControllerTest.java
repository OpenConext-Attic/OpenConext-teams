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

import nl.surfnet.coin.teams.domain.ExternalGroup;
import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Person;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.VootClient;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
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

    VootClient vootClient = mock(VootClient.class);
    when(vootClient.groups(getMember().getId())).thenReturn(Collections.<ExternalGroup>emptyList());

    TeamInviteService teamInviteService = mock(TeamInviteService.class);
    when(teamInviteService.findPendingInvitationsByEmail(getPerson1().getEmail())).thenReturn(new ArrayList<Invitation>());

    autoWireMock(homeController, vootClient, VootClient.class);
    autoWireMock(homeController, grouperTeamService, GrouperTeamService.class);
    autoWireMock(homeController, teamInviteService, TeamInviteService.class);
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


  private RequestAttributes getRequestAttributes() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    Person person = getPerson("test");
    session.setAttribute(LoginInterceptor.PERSON_SESSION_KEY, person);
    request.setSession(session);
    return new ServletRequestAttributes(request);
  }


  @Override
  public void setup() throws Exception {
    super.setup();
    TeamEnvironment mockTeamEnvironment = mock(TeamEnvironment.class);
    mockTeamEnvironment.setDefaultStemName("nl:surfnet:diensten");
    homeController.setTeamEnvironment(mockTeamEnvironment);
  }
}
