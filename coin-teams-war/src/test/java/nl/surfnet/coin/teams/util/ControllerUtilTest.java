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

package nl.surfnet.coin.teams.util;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import nl.surfnet.coin.teams.control.AbstractControllerTest;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.GrouperTeamService;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link ControllerUtil}
 */
public class ControllerUtilTest extends AbstractControllerTest {

  private ControllerUtil controllerUtil = new ControllerUtilImpl();

  @Test
  public void getTeamTest() throws Exception {

    MockHttpServletRequest request = getRequestWithTeam(getTeam1().getId());

    GrouperTeamService grouperTeamService = createNiceMock(GrouperTeamService.class);
    expect(grouperTeamService.findTeamById(getTeam1().getId())).andReturn(getTeam1());
    replay(grouperTeamService);

    autoWireMock(controllerUtil, grouperTeamService, GrouperTeamService.class);
    autoWireRemainingResources(controllerUtil);

    Team team = controllerUtil.getTeam(request);
    verify(grouperTeamService);

    assertEquals(getTeam1().getId(), team.getId());
    assertEquals(getTeam1().getName(), team.getName());
    assertEquals(getTeam1().getDescription(), team.getDescription());
  }

  @Test(expected = RuntimeException.class)
  public void getTeamNonExistingTest() throws Exception {
    MockHttpServletRequest request = getRequestWithTeam(getTeam1().getId());

    GrouperTeamService grouperTeamService = createNiceMock(GrouperTeamService.class);
    expect(grouperTeamService.findTeamById(getTeam1().getId())).andReturn(null);
    replay(grouperTeamService);

    Team team = controllerUtil.getTeam(request);
    verify(grouperTeamService);
  }

  @Test
  public void hasUserAdministrativePrivilegesTest() throws Exception {
    GrouperTeamService grouperTeamService = createNiceMock(GrouperTeamService.class);
    expect(grouperTeamService.findMember(getTeam1().getId(), getPerson1().getId())).andReturn(getAdministrativeMember());
    replay(grouperTeamService);

    autoWireMock(controllerUtil, grouperTeamService, GrouperTeamService.class);
    autoWireRemainingResources(controllerUtil);

    boolean hasPrivileges = controllerUtil.hasUserAdministrativePrivileges(getPerson1(), getTeam1().getId());
    verify(grouperTeamService);
    assertTrue(hasPrivileges);
  }

  @Test
  public void hasUserAdministrativePrivilegesWithoutPrivilegesTest() throws Exception {
    GrouperTeamService grouperTeamService = createNiceMock(GrouperTeamService.class);
    expect(grouperTeamService.findMember(getTeam1().getId(), getPerson1().getId())).andReturn(getMember());
    replay(grouperTeamService);

    autoWireMock(controllerUtil, grouperTeamService, GrouperTeamService.class);
    autoWireRemainingResources(controllerUtil);

    boolean hasPrivileges = controllerUtil.hasUserAdministrativePrivileges(getPerson1(), getTeam1().getId());
    verify(grouperTeamService);
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
