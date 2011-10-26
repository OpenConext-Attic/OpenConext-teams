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
import nl.surfnet.coin.teams.util.TokenUtil;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.support.SimpleSessionStatus;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author steinwelberg
 *
 */
public class AddTeamControllerTest extends AbstractControllerTest{
  
  private AddTeamController addTeamController = new AddTeamController();
  
  @Test
  public void testStart() {
    MockHttpServletRequest request = getRequest();

    String result = addTeamController.start(getModelMap(), request);
    
    assertEquals("addteam", result);
  }

  @Test
  public void testAddTeamHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    Team team1 = getTeam1();
    // request team
    request.setParameter("team", team1.getId());
    request.setParameter("teamName", team1.getName());
    request.setParameter("description", team1.getDescription());

    TeamService teamService = mock(TeamService.class);
    when(teamService.addTeam(team1.getName(), team1.getName(), team1.getDescription(),
            null)).thenReturn(team1.getId());
    
    autoWireMock(addTeamController, teamService, TeamService.class);
    autoWireRemainingResources(addTeamController);
    
    String view = addTeamController.addTeam(getModelMap(), team1, request, token, token, new SimpleSessionStatus());

    assertEquals("redirect:detailteam.shtml?team=" + team1.getId() + "&view=app", view);
  }
  
  @Test
  public void testFailToAddTeamWithEmptyName() throws Exception {
    MockHttpServletRequest request = getRequest();
    String token = TokenUtil.generateSessionToken();
    Team team = getTeam1();

    TeamService teamService = mock(TeamService.class);
    when(teamService.addTeam(team.getName(), team.getName(), team.getDescription(),
            null)).thenReturn(team.getId());

    autoWireMock(addTeamController, teamService, TeamService.class);
    autoWireRemainingResources(addTeamController);
    
    String view = addTeamController.addTeam(getModelMap(), new Team(team.getId(), null, team.getDescription()), request, token, token, new SimpleSessionStatus());
    assertEquals("addteam", view);
    }
}