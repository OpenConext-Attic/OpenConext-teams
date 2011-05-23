/*
 * Copyright 2011 SURFnet bv
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;

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
    // request team
    request.setParameter("team", "Team 1");
    request.setParameter("description", "description team 1");
    Team team = new Team("Team 1", "Team 1", "description team 1", null);
    TeamService teamService = mock(TeamService.class);
    when(teamService.addTeam("Team 1", "Team 1", "description team 1",
            null)).thenReturn("team-1");
    
    autoWireMock(addTeamController, teamService, TeamService.class);
    autoWireRemainingResources(addTeamController);
    
    String view = addTeamController.addTeam(getModelMap(), team, request);
        
    assertEquals("redirect:detailteam.shtml?team=team-1&view=app", view);
  }
  
  @Test
  public void testFailToAddTeamWithEmptyName() throws Exception {
    MockHttpServletRequest request = getRequest();
    // request team
    request.setParameter("description", "description team 1");

    TeamService teamService = mock(TeamService.class);
    when(teamService.addTeam("Team 1", "Team 1", "description team 1",
            null)).thenReturn("team-1");
    Team team = new Team("Team 1", "", "description team 1", null);

    autoWireMock(addTeamController, teamService, TeamService.class);
    autoWireRemainingResources(addTeamController);
    
    String view = addTeamController.addTeam(getModelMap(), team, request);
    assertEquals("addteam", view);
    }
}