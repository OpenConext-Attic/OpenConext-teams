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

package teams.control;

import teams.domain.Person;
import teams.service.GrouperTeamService;
import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link AddExternalGroupController}
 */
public class AddExternalGroupControllerTest extends AbstractControllerTest {

  private AddExternalGroupController controller;

  @Before
  public void setUp() throws Exception {
    controller = new AddExternalGroupController();
    final Person person1 = getPerson("urn:collab:person:hz.nl:member-1");

    GrouperTeamService teamService = mock(GrouperTeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(getTeam1());
    autoWireMock(controller, teamService, GrouperTeamService.class);

  }


}
