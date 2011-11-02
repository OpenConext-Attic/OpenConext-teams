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

package nl.surfnet.coin.teams.util;

import nl.surfnet.coin.teams.control.AbstractControllerTest;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Test for {@link ControllerUtil}
 */
public class ControllerUtilTest extends AbstractControllerTest {

  private ControllerUtil controllerUtil = new ControllerUtilImpl();

  @Test
  public void getTeamTest() throws Exception {

    MockHttpServletRequest request = getRequestWithTeam(getTeam1().getId());

    TeamService teamService = createNiceMock(TeamService.class);
    expect(teamService.findTeamById(getTeam1().getId())).andReturn(getTeam1());
    replay(teamService);

    autoWireMock(controllerUtil, teamService, TeamService.class);
    autoWireRemainingResources(controllerUtil);

    Team team = controllerUtil.getTeam(request);
    verify(teamService);

    assertEquals(getTeam1().getId(), team.getId());
    assertEquals(getTeam1().getName(), team.getName());
    assertEquals(getTeam1().getDescription(), team.getDescription());
  }

  @Test(expected = RuntimeException.class)
  public void getTeamNonExistingTest() throws Exception {
    MockHttpServletRequest request = getRequestWithTeam(getTeam1().getId());

    TeamService teamService = createNiceMock(TeamService.class);
    expect(teamService.findTeamById(getTeam1().getId())).andReturn(null);
    replay(teamService);

    Team team = controllerUtil.getTeam(request);
    verify(teamService);
  }

  @Test
  public void hasUserAdministrativePrivilegesTest() throws Exception {
    TeamService teamService = createNiceMock(TeamService.class);
    expect(teamService.findMember(getTeam1().getId(), getPerson1().getId())).andReturn(getAdministrativeMember());
    replay(teamService);

    autoWireMock(controllerUtil, teamService, TeamService.class);
    autoWireRemainingResources(controllerUtil);

    boolean hasPrivileges = controllerUtil.hasUserAdministrativePrivileges(getPerson1(), getTeam1().getId());
    verify(teamService);
    assertTrue(hasPrivileges);
  }

  @Test
  public void hasUserAdministrativePrivilegesWithoutPrivilegesTest() throws Exception {
    TeamService teamService = createNiceMock(TeamService.class);
    expect(teamService.findMember(getTeam1().getId(), getPerson1().getId())).andReturn(getMember());
    replay(teamService);

    autoWireMock(controllerUtil, teamService, TeamService.class);
    autoWireRemainingResources(controllerUtil);

    boolean hasPrivileges = controllerUtil.hasUserAdministrativePrivileges(getPerson1(), getTeam1().getId());
    verify(teamService);
    assertFalse(hasPrivileges);
  }

  @Test
  public void isPersonMemberOfTeamIsMemberTest() {
    Team team = getTeam1();
    Member member = getMember();
    team.addMembers(member);

    boolean result = controllerUtil.isPersonMemberOfTeam(member.getId(), team);
    assertTrue(result);
  }

  @Test
  public void isPersonMemberOfTeamIsNotMemberTest() {
    Team team = getTeam1();
    Member member = new Member(null, "member-2", "member-2", "member-2@example.com");
    team.addMembers(getMember());

    boolean result = controllerUtil.isPersonMemberOfTeam(member.getId(), team);
    assertFalse(result);
  }

  private MockHttpServletRequest getRequestWithTeam(String teamId) {
    MockHttpServletRequest request = getRequest();
    request.setParameter("team", teamId);
    return request;
  }
}
