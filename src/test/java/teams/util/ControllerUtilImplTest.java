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

package teams.util;

import teams.control.AbstractControllerTest;
import teams.domain.Member;
import teams.domain.Person;
import teams.domain.Team;
import teams.service.GrouperTeamService;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Test for {@link ControllerUtilImpl}
 */
public class ControllerUtilImplTest extends AbstractControllerTest {

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

    controllerUtil.getTeam(request);
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

    boolean result = controllerUtil.isPersonMemberOfTeam(getPerson(member.getId()), team);

    assertTrue(result);
  }

  @Test
  public void isPersonMemberOfTeamIsNotMemberTest() {
    Team team = getTeam1();

    Person person = new Person(null, "name", "email", "org", "vootrole", "displayName");
    team.addMembers(getMember());

    boolean result = controllerUtil.isPersonMemberOfTeam(person, team);
    assertFalse(result);
  }

  private MockHttpServletRequest getRequestWithTeam(String teamId) {
    MockHttpServletRequest request = getRequest();
    request.setParameter("team", teamId);
    return request;
  }
}
