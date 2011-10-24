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

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * Test for {@link AddMemberController}
 */
public class AddMemberControllerTest extends AbstractControllerTest {

  private AddMemberController addMemberController = new AddMemberController();
  final MessageSource messageSource = new ResourceBundleMessageSource() {
    {
      setBasename("messages");
    }
  };

  @Test
  public void testStartHappyFlow() throws Exception {

    MockHttpServletRequest request = getRequest();
    // request team
    request.setParameter("team", "team-1");


    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    Team team1 = new Team("team-1", "Team 1", "description", true);
    Set<Role> roles = new HashSet<Role>();
    roles.add(Role.Admin);
    Member member1 = new Member(roles, "Member 1", "member-1", "member@example.com");

    TeamService teamService = createNiceMock(TeamService.class);
    expect(teamService.findTeamById("team-1")).andReturn(team1);
    expect(teamService.findMember("team-1", "member-1")).andReturn(member1);
    replay(teamService);

    autoWireMock(addMemberController, teamService, TeamService.class);

    addMemberController.start(getModelMap(), request);
    verify(teamService);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("description", team.getDescription());
  }

  @Test(expected = RuntimeException.class)
  public void testStart() throws Exception {

    MockHttpServletRequest request = getRequest();
    // request team

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    Team team1 = new Team("team-1", "Team 1", "description");

    autoWireMock(addMemberController, new Returns(team1), TeamService.class);

    addMemberController.start(getModelMap(), request);
  }


  @Test
  public void testAddMemberHappyFlow() throws Exception {
    MockHttpServletRequest request = getRequest();
    // request team
    request.setParameter("team", "team-1");
    request.setParameter("memberEmail", "john@doe.com");
    request.setParameter("message", "Nice description");

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    Team team1 = new Team("team-1", "Team 1", "description", false);
    Set<Role> roles = new HashSet<Role>();
    roles.add(Role.Admin);
    Member member1 = new Member(roles, "Member 1", "member-1", "member@example.com");

    TeamService teamService = createNiceMock(TeamService.class);
    expect(teamService.findTeamById("team-1")).andReturn(team1);
    expect(teamService.findMember("team-1", "member-1")).andReturn(member1);
    replay(teamService);

    autoWireMock(addMemberController, teamService, TeamService.class);
    autoWireRemainingResources(addMemberController);

    addMemberController.start(getModelMap(), request);
    verify(teamService);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("description", team.getDescription());
  }

  @Test(expected = RuntimeException.class)
  public void testAddMemberWrongPrivileges() throws Exception {
    MockHttpServletRequest request = getRequest();
    // request team
    request.setParameter("team", "team-1");
    request.setParameter("memberEmail", "john@doe.com");
    request.setParameter("message", "Nice description");

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    Team team1 = new Team("team-1", "Team 1", "description", false);
    Set<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);
    Member member1 = new Member(roles, "Member 1", "member-1", "member@example.com");

    TeamService teamService = createNiceMock(TeamService.class);
    expect(teamService.findTeamById("team-1")).andReturn(team1);
    expect(teamService.findMember("team-1", "member-1")).andReturn(member1);
    replay(teamService);

    autoWireMock(addMemberController, teamService, TeamService.class);
    autoWireRemainingResources(addMemberController);

    addMemberController.start(getModelMap(), request);
    verify(teamService);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("description", team.getDescription());
  }

  @Test(expected = RuntimeException.class)
  public void testAddMemberRequesterNotMember() throws Exception {
    MockHttpServletRequest request = getRequest();
    // request team
    request.setParameter("team", "team-1");
    request.setParameter("memberEmail", "john@doe.com");
    request.setParameter("message", "Nice description");

    autoWireMock(addMemberController, messageSource, MessageSource.class);
    autoWireMock(addMemberController, new Returns(Locale.ENGLISH), LocaleResolver.class);

    Team team1 = new Team("team-1", "Team 1", "description", false);

    TeamService teamService = createNiceMock(TeamService.class);
    expect(teamService.findTeamById("team-1")).andReturn(team1);
    expect(teamService.findMember("team-1", "member-1")).andReturn(null);
    replay(teamService);

    autoWireMock(addMemberController, teamService, TeamService.class);
    autoWireRemainingResources(addMemberController);

    addMemberController.start(getModelMap(), request);
    verify(teamService);

    Team team = (Team) getModelMap().get("team");

    assertEquals("team-1", team.getId());
    assertEquals("Team 1", team.getName());
    assertEquals("description", team.getDescription());
  }
//  @Test (expected=RuntimeException.class)
//  public void testAddMember() throws Exception {
//    MockHttpServletRequest request = getRequest();
//    // request team
//    request.setParameter("description", "Nice description");
//
//    Team team1 = new Team("team-1", "Team 1", "description");
//
//    autoWireMock(addMemberController, new Returns(team1), TeamService.class);
//    autoWireRemainingResources(addMemberController);
//
//    addMemberController.start(getModelMap(), request);
//    }
//  

}
