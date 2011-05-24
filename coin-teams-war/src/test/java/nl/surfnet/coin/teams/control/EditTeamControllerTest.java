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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;

/**
 * @author steinwelberg
 * 
 */
public class EditTeamControllerTest extends AbstractControllerTest {

  private EditTeamController editTeamController = new EditTeamController();
  
  private Team mockTeam;
  
  @Before
  public void prepareData() {
    mockTeam = new Team("team-1", "Team 1", "description");
  }

  @Test
  public void testStartHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the team
    request.addParameter("team", "team-1");

    autoWireMock(editTeamController, new Returns(mockTeam), TeamService.class);

    editTeamController.start(getModelMap(), request);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
  }

  @Test(expected = RuntimeException.class)
  public void testStart() throws Exception {
    MockHttpServletRequest request = getRequest();

    autoWireRemainingResources(editTeamController);

    editTeamController.start(getModelMap(), request);
  }

  @Test
  public void testEditTeamHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the teamId, team name & description
    request.addParameter("team", "Team 1");
    request.addParameter("teamId", "team-1");
    request.addParameter("description", "description");

    autoWireMock(editTeamController, new Returns(mockTeam), TeamService.class);

    RedirectView result = editTeamController.editTeam(getModelMap(), request);

    assertEquals("detailteam.shtml?team=team-1&view=app", result.getUrl());
  }

  @Test(expected = RuntimeException.class)
  public void testEditTeamNoName() throws Exception {
    MockHttpServletRequest request = getRequest();
    // do NOT add the team name, but do add the team id and team description
    request.addParameter("teamId", "team-1");
    request.addParameter("description", "description");

    autoWireMock(editTeamController, new Returns(mockTeam), TeamService.class);

    editTeamController.editTeam(getModelMap(), request);
  }
  
  @Test(expected = RuntimeException.class)
  public void testEditTeamNoId() throws Exception {
    MockHttpServletRequest request = getRequest();
    // do NOT add the team id, but do add the team name and team description
    request.addParameter("team", "Team 1");
    request.addParameter("description", "description");

    autoWireMock(editTeamController, new Returns(mockTeam), TeamService.class);

    editTeamController.editTeam(getModelMap(), request);
  }

}
