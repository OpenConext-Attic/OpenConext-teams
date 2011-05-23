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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;

/**
 * @author steinwelberg
 *
 */
public class JoinTeamControllerTest extends AbstractControllerTest {
  
  private JoinTeamController joinTeamController = new JoinTeamController();
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
    
    autoWireMock(joinTeamController, new Returns(mockTeam), TeamService.class);
    autoWireRemainingResources(joinTeamController);
    
    joinTeamController.start(getModelMap(), request);
    
    Team team = (Team) getModelMap().get("team");
    
    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("description", team.getDescription());
  }
  
  @Test(expected = RuntimeException.class)
  public void testStart() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Do NOT add the team

    autoWireRemainingResources(joinTeamController);
    
    joinTeamController.start(getModelMap(), request);
  }
  
  @Test
  public void testJoinTeamHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Add the team
    request.addParameter("team", "team-1");
    request.addParameter("message", "message");

    TeamService mockTeamService = createMock("mockTeamService", TeamService.class);
    joinTeamController.setTeamService(mockTeamService);
    expect(mockTeamService.findTeamById("team-1")).andReturn(mockTeam);

    Set<Role> roleSet = new HashSet<Role>();
    roleSet.add(Role.Admin);
    Member admin = new Member(roleSet,"Mr Member", "ID2345", "member@example.com");
    Set<Member> admins = new HashSet<Member>();
    admins.add(admin);
    expect(mockTeamService.findAdmins(mockTeam)).andReturn(admins);

    JoinTeamRequest joinTeamRequest = new JoinTeamRequest("ID2345", "team-1");

    autoWireRemainingResources(joinTeamController);

    replay(mockTeamService);
    RedirectView result = joinTeamController.joinTeam(getModelMap(), joinTeamRequest, request);
    
    assertEquals("home.shtml?teams=my&view=app", result.getUrl());
  }
  
  @Test(expected = RuntimeException.class)
  public void testJoinTeam() throws Exception {
    MockHttpServletRequest request = getRequest();
    // Do NOT add the team
    
    autoWireRemainingResources(joinTeamController);
    
    joinTeamController.joinTeam(getModelMap(), null, request);
  }

}
