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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import teams.control.AbstractControllerTest;
import teams.domain.Member;
import teams.domain.Person;
import teams.domain.Team;
import teams.service.GrouperTeamService;

@RunWith(MockitoJUnitRunner.class)
public class ControllerUtilImplTest extends AbstractControllerTest {

  @InjectMocks
  private ControllerUtilImpl controllerUtil;

  @Mock
  private GrouperTeamService grouperTeamServiceMock;

  @Test
  public void getTeamTest() throws Exception {
    MockHttpServletRequest request = getRequestWithTeam(getTeam1().getId());

    when(grouperTeamServiceMock.findTeamById(getTeam1().getId())).thenReturn(getTeam1());

    Team team = controllerUtil.getTeam(request);

    assertEquals(getTeam1().getId(), team.getId());
    assertEquals(getTeam1().getName(), team.getName());
    assertEquals(getTeam1().getDescription(), team.getDescription());
  }

  @Test
  public void hasUserAdministrativePrivilegesTest() throws Exception {
    when(grouperTeamServiceMock.findMember(getTeam1(), getPerson1().getId())).thenReturn(getAdministrativeMember());

    boolean hasPrivileges = controllerUtil.hasUserAdministrativePrivileges(getPerson1(), getTeam1());

    assertTrue(hasPrivileges);
  }

  @Test
  public void hasUserAdministrativePrivilegesWithoutPrivilegesTest() throws Exception {
    when(grouperTeamServiceMock.findMember(getTeam1(), getPerson1().getId())).thenReturn(getMember());

    boolean hasPrivileges = controllerUtil.hasUserAdministrativePrivileges(getPerson1(), getTeam1());

    assertFalse(hasPrivileges);
  }

  @Test
  public void isPersonMemberOfTeamIsMemberTest() {
    Member member = getMember();
    Team team = getTeam1();
    team.addMembers(member);

    boolean result = controllerUtil.isPersonMemberOfTeam(getPerson(member.getId()), team);

    assertTrue(result);
  }

  @Test
  public void isPersonMemberOfTeamIsNotMemberTest() {
    Person person = new Person(null, "name", "email", "org", "vootrole", "displayName");

    Team team = getTeam1();
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
