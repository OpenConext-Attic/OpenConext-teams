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

import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.GroupProviderType;
import nl.surfnet.coin.teams.service.GroupProviderService;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ExternalTeamsController}
 */
public class ExternalTeamsControllerTest extends AbstractControllerTest{
  private GroupProviderService groupProviderService;
  private ExternalTeamsController externalTeamsController;

  @Before
  public void setUp() throws Exception {
    super.setup();
    groupProviderService = mock(GroupProviderService.class);
    externalTeamsController = new ExternalTeamsController();
  }

  @Test
  public void testGetMyExternalGroupProviders_empty() throws Exception {
    MockHttpServletRequest request = getRequest();
    List<GroupProvider> groupProviders = new ArrayList<GroupProvider>();

    when(groupProviderService.getGroupProviders(getMember().getId())).thenReturn(groupProviders);
    autoWireMock(externalTeamsController, groupProviderService, GroupProviderService.class);

    final List<GroupProvider> list = externalTeamsController.getMyExternalGroupProviders(request);
    assertEquals(groupProviders, list);
  }

  @Test
  public void testGetMyExternalGroupProviders_populated() throws Exception {
    MockHttpServletRequest request = getRequest();
    List<GroupProvider> groupProviders = new ArrayList<GroupProvider>();
    GroupProvider external1 = new GroupProvider(1L, "myGroupProvider", "My group provider",
        GroupProviderType.OAUTH_THREELEGGED.getStringValue());
    GroupProvider external2 = new GroupProvider(2L, "myGroupProvider2", "My 2nd group provider",
        GroupProviderType.OAUTH_THREELEGGED.getStringValue());
    groupProviders.add(external1);
    groupProviders.add(external2);

    when(groupProviderService.getGroupProviders(getMember().getId())).thenReturn(groupProviders);
    autoWireMock(externalTeamsController, groupProviderService, GroupProviderService.class);

    final List<GroupProvider> list = externalTeamsController.getMyExternalGroupProviders(request);
    assertEquals(groupProviders, list);
  }
}
