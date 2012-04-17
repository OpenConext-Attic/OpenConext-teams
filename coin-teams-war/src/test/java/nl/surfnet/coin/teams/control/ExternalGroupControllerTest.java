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

package nl.surfnet.coin.teams.control;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import nl.surfnet.coin.api.client.domain.Group20;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.GroupProviderType;
import nl.surfnet.coin.teams.domain.GroupProviderUserOauth;
import nl.surfnet.coin.teams.service.GroupProviderService;
import nl.surfnet.coin.teams.service.GroupService;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ExternalGroupController}
 */
public class ExternalGroupControllerTest extends AbstractControllerTest {
  private ExternalGroupController controller;

  @Before
  public void setUp() throws Exception {
    super.setup();
    controller = new ExternalGroupController();

  }

  @Test
  public void testGetMyExternalGroups() throws Exception {
    MockHttpServletRequest request = getRequest();
    Long groupProviderId = 4L;

    GroupProviderService groupProviderService = mock(GroupProviderService.class);
    when(groupProviderService.getGroupProviderUserOauths(getMember().getId())).thenReturn(getOAuths());

    GroupProvider provider = new GroupProvider(groupProviderId, "hz", "HZ Groupen",
        GroupProviderType.OAUTH_THREELEGGED.getStringValue());
    when(groupProviderService.getGroupProviderByStringIdentifier("hz")).thenReturn(provider);

    List<Group20> group20s = new ArrayList<Group20>();
    GroupService groupService = mock(GroupService.class);
    when(groupService.getGroup20s(getOAuths().get(0), provider)).thenReturn(group20s);

    autoWireMock(controller, groupProviderService, GroupProviderService.class);
    autoWireMock(controller, provider, GroupProvider.class);
    autoWireMock(controller, groupService, GroupService.class);

    final List<Group20> myExternalGroups = controller.getMyExternalGroups(groupProviderId, request);
    assertEquals(group20s, myExternalGroups);


  }

  @Test
  public void testGetMyExternalGroupMembers() throws Exception {

  }

  private List<GroupProviderUserOauth> getOAuths() {
    List<GroupProviderUserOauth> oAuths = new ArrayList<GroupProviderUserOauth>();
    GroupProviderUserOauth oauth = new GroupProviderUserOauth(getMember().getId(), "hz", "token", "secret");
    oAuths.add(oauth);
    return oAuths;
  }
}
