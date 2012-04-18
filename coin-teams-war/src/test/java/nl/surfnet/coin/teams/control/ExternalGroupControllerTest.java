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
import org.springframework.ui.ModelMap;

import nl.surfnet.coin.api.client.domain.Group20;
import nl.surfnet.coin.teams.domain.ConversionRule;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.GroupProviderType;
import nl.surfnet.coin.teams.domain.GroupProviderUserOauth;
import nl.surfnet.coin.teams.service.GroupProviderService;
import nl.surfnet.coin.teams.service.GroupService;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
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
  public void testGroupDetail() throws Exception {
    final MockHttpServletRequest request = getRequest();
    final ModelMap modelMap = getModelMap();

    final String groupId = "urn:collab:group:hz.nl:HZG-1042";
    final GroupProvider groupProvider = getGroupProvider();
    final List<GroupProviderUserOauth> oAuths = getOAuths();

    Group20 group20 = new Group20();
    group20.setTitle("HZG-1042 Test Group");
    group20.setId(groupId);

    GroupProviderService providerService = mock(GroupProviderService.class);
    when(providerService.getGroupProviderUserOauths(getMember().getId())).thenReturn(oAuths);
    when(providerService.getGroupProviderByStringIdentifier("hz")).thenReturn(groupProvider);

    GroupService groupService = mock(GroupService.class);
    when(groupService.getGroup20(getOAuths().get(0), groupProvider, groupId)).thenReturn(group20);

    autoWireMock(controller, providerService, GroupProviderService.class);
    autoWireMock(controller, groupService, GroupService.class);

    String view = controller.groupDetail(groupId, request, modelMap);

    assertEquals("external-groupdetail", view);
    assertEquals(groupProvider, modelMap.get("groupProvider"));
    assertTrue(modelMap.containsKey("members"));
    assertEquals(group20, modelMap.get("group20"));

  }

  private GroupProvider getGroupProvider() {
    GroupProvider groupProvider = new GroupProvider(4L, "hz", "HZ", GroupProviderType.OAUTH_THREELEGGED.getStringValue());
    ConversionRule groupDecorator = new ConversionRule();
    groupDecorator.setPropertyName("id");
    groupDecorator.setSearchPattern("urn:collab:group:hz.nl:(.+)");
    groupDecorator.setReplaceWith("$1");
    groupProvider.addGroupDecorator(groupDecorator);
    return groupProvider;
  }

  private List<GroupProviderUserOauth> getOAuths() {
    List<GroupProviderUserOauth> oAuths = new ArrayList<GroupProviderUserOauth>();
    GroupProviderUserOauth oauth = new GroupProviderUserOauth(getMember().getId(), "hz", "token", "secret");
    oAuths.add(oauth);
    return oAuths;
  }
}
