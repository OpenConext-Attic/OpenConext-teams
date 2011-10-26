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
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;
import org.junit.Test;
import org.opensocial.models.Person;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashSet;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Test for {@link ControllerUtil}
 */
public class ControllerUtilTest extends AbstractControllerTest {

  @Test
  public void getTeamTest() throws Exception {

    MockHttpServletRequest request = getRequestWithTeam(getTeam1().getId());

    ControllerUtil controllerUtil = new ControllerUtilImpl();
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

    ControllerUtil controllerUtil = new ControllerUtilImpl();
    TeamService teamService = createNiceMock(TeamService.class);
    expect(teamService.findTeamById(getTeam1().getId())).andReturn(null);
    replay(teamService);

    Team team = controllerUtil.getTeam(request);
    verify(teamService);
  }

  @Test
  public void hasUserAdministrativePrivilegesTest() throws Exception {
    ControllerUtil controllerUtil = new ControllerUtilImpl();
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
    ControllerUtil controllerUtil = new ControllerUtilImpl();
    TeamService teamService = createNiceMock(TeamService.class);
    expect(teamService.findMember(getTeam1().getId(), getPerson1().getId())).andReturn(getMember());
    replay(teamService);

    autoWireMock(controllerUtil, teamService, TeamService.class);
    autoWireRemainingResources(controllerUtil);

    boolean hasPrivileges = controllerUtil.hasUserAdministrativePrivileges(getPerson1(), getTeam1().getId());
    verify(teamService);
    assertFalse(hasPrivileges);
  }

  private Team getTeam1() {
    return new Team("team-1", "Team 1", "Nice description", true);
  }

  private Person getPerson1() {
    Person person = new Person();
    person.setField("id", "member-1");
    return person;
  }

  private Member getAdministrativeMember() {
    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Manager);
    roles.add(Role.Member);
    roles.add(Role.Admin);
    return new Member(roles, "Member 1", "member-1", "member@example.com");
  }

  private Member getMember() {
    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);
    return new Member(roles, "Member 1", "member-1", "member@example.com");
  }

  private MockHttpServletRequest getRequestWithTeam(String teamId) {
    MockHttpServletRequest request = getRequest();
    request.setParameter("team", teamId);
    return request;
  }
}
