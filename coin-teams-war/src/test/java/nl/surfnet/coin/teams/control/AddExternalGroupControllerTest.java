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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opensocial.models.Person;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;

import nl.surfnet.coin.api.client.OpenConextJsonParser;
import nl.surfnet.coin.api.client.domain.Group20Entry;
import nl.surfnet.coin.teams.domain.ConversionRule;
import nl.surfnet.coin.teams.domain.ExternalGroup;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.GroupProviderType;
import nl.surfnet.coin.teams.domain.GroupProviderUserOauth;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.domain.TeamExternalGroup;
import nl.surfnet.coin.teams.service.GroupProviderService;
import nl.surfnet.coin.teams.service.GroupService;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.TeamExternalGroupDao;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.ExternalGroupUtil;
import nl.surfnet.coin.teams.util.TokenUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link AddExternalGroupController}
 */
public class AddExternalGroupControllerTest extends AbstractControllerTest {

  private AddExternalGroupController controller;
  private OpenConextJsonParser parser = new OpenConextJsonParser();


  @Before
  public void setUp() throws Exception {
    controller = new AddExternalGroupController();
    final Person person1 = getPerson1();
    person1.setField("id", "urn:collab:person:hz.nl:member-1");

    GrouperTeamService teamService = mock(GrouperTeamService.class);
    when(teamService.findTeamById("team-1")).thenReturn(getTeam1());
    autoWireMock(controller, teamService, GrouperTeamService.class);

  }

  @Test
  public void testShowAddExternalGroupsForm() throws Exception {
    final MockHttpServletRequest request = getRequest();
    final Team team1 = getTeam1();
    final ModelMap modelMap = getModelMap();
    final Person person1 = getPerson1();
    final String groupProviderIdentifier = "hz";
    final GroupProvider hzProvider = getHzProvider(groupProviderIdentifier);

    ControllerUtil controllerUtil = mock(ControllerUtil.class);
    when(controllerUtil.hasUserAdministrativePrivileges(person1, team1.getId())).thenReturn(true);
    autoWireMock(controller, controllerUtil, ControllerUtil.class);

    GroupProviderService groupProviderService = mock(GroupProviderService.class);
    when(groupProviderService.getGroupProviderUserOauths(person1.getId())).thenReturn(oauthKeys());
    when(groupProviderService.getGroupProviderByStringIdentifier(groupProviderIdentifier)).thenReturn(hzProvider);
    autoWireMock(controller, groupProviderService, GroupProviderService.class);

    GroupService groupService = mock(GroupService.class);
    when(groupService.getGroup20Entry(oauthKeys().get(0), hzProvider, 250, 0)).thenReturn(getMyHzGroups());
    autoWireMock(controller, groupService, GroupService.class);

    TeamExternalGroupDao teamExternalGroupDao = mock(TeamExternalGroupDao.class);
    when(teamExternalGroupDao.getByTeamIdentifier(team1.getId())).thenReturn(new ArrayList<TeamExternalGroup>());
    autoWireMock(controller, teamExternalGroupDao, TeamExternalGroupDao.class);


    final String viewName = controller.showAddExternalGroupsForm(team1.getId(), modelMap, request);

    assertEquals("addexternalgroup", viewName);
    assertEquals(team1, modelMap.get("team"));
    @SuppressWarnings("unchecked") List<ExternalGroup> externalGroups = (List<ExternalGroup>) request.getSession().getAttribute("externalGroups");
    assertEquals(3, externalGroups.size());
    assertTrue(modelMap.containsKey("view"));
    assertTrue(modelMap.containsKey(TokenUtil.TOKENCHECK));
  }

  @Test
  public void testShowAddExternalGroupsForm_alreadyLinked() throws Exception {
    final MockHttpServletRequest request = getRequest();
    final Team team1 = getTeam1();
    final ModelMap modelMap = getModelMap();
    final Person person1 = getPerson1();
    final String groupProviderIdentifier = "hz";
    final GroupProvider hzProvider = getHzProvider(groupProviderIdentifier);

    ControllerUtil controllerUtil = mock(ControllerUtil.class);
    when(controllerUtil.hasUserAdministrativePrivileges(person1, team1.getId())).thenReturn(true);
    autoWireMock(controller, controllerUtil, ControllerUtil.class);

    GroupProviderService groupProviderService = mock(GroupProviderService.class);
    when(groupProviderService.getGroupProviderUserOauths(person1.getId())).thenReturn(oauthKeys());
    when(groupProviderService.getGroupProviderByStringIdentifier(groupProviderIdentifier)).thenReturn(hzProvider);
    autoWireMock(controller, groupProviderService, GroupProviderService.class);

    GroupService groupService = mock(GroupService.class);
    when(groupService.getGroup20Entry(oauthKeys().get(0), hzProvider, 250, 0)).thenReturn(getMyHzGroups());
    autoWireMock(controller, groupService, GroupService.class);

    TeamExternalGroupDao teamExternalGroupDao = mock(TeamExternalGroupDao.class);
    when(teamExternalGroupDao.getByTeamIdentifier(team1.getId())).thenReturn(getLinkedHzGroups(team1.getId()));
    autoWireMock(controller, teamExternalGroupDao, TeamExternalGroupDao.class);


    final String viewName = controller.showAddExternalGroupsForm(team1.getId(), modelMap, request);

    assertEquals("addexternalgroup", viewName);
    assertEquals(team1, modelMap.get("team"));
    @SuppressWarnings("unchecked") List<ExternalGroup> externalGroups = (List<ExternalGroup>) request.getSession().getAttribute("externalGroups");
    assertEquals(0, externalGroups.size());
    assertTrue(modelMap.containsKey("view"));
    assertTrue(modelMap.containsKey(TokenUtil.TOKENCHECK));
  }

  @Test(expected = RuntimeException.class)
  public void testShowAddExternalGroupsForm_noPrivileges() throws Exception {
    final MockHttpServletRequest request = getRequest();
    final Team team1 = getTeam1();
    final ModelMap modelMap = getModelMap();
    final Person person1 = getPerson1();
    ControllerUtil controllerUtil = mock(ControllerUtil.class);
    when(controllerUtil.hasUserAdministrativePrivileges(person1, team1.getId())).thenReturn(false);
    autoWireMock(controller, controllerUtil, ControllerUtil.class);

    controller.showAddExternalGroupsForm(team1.getId(), modelMap, request);
  }

  @Test
  public void testShowAddExternalGroupsForm_noAuthKeys() throws Exception {
    final MockHttpServletRequest request = getRequest();
    final Team team1 = getTeam1();
    final ModelMap modelMap = getModelMap();
    final Person person1 = getPerson1();

    ControllerUtil controllerUtil = mock(ControllerUtil.class);
    when(controllerUtil.hasUserAdministrativePrivileges(person1, team1.getId())).thenReturn(true);
    autoWireMock(controller, controllerUtil, ControllerUtil.class);

    GroupProviderService groupProviderService = mock(GroupProviderService.class);

    when(groupProviderService.getGroupProviderUserOauths(person1.getId())).
        thenReturn(Collections.<GroupProviderUserOauth>emptyList());
    autoWireMock(controller, groupProviderService, GroupProviderService.class);

    final String viewName = controller.showAddExternalGroupsForm(team1.getId(), modelMap, request);

    assertEquals("addexternalgroup", viewName);
    assertTrue(modelMap.containsKey("view"));
  }

  @Test
  public void testShowAddExternalGroupsForm_noExternalGroups() throws Exception {
    final MockHttpServletRequest request = getRequest();
    final Team team1 = getTeam1();
    final ModelMap modelMap = getModelMap();
    final Person person1 = getPerson1();
    final String groupProviderIdentifier = "hz";
    final GroupProvider hzProvider = getHzProvider(groupProviderIdentifier);

    ControllerUtil controllerUtil = mock(ControllerUtil.class);
    when(controllerUtil.hasUserAdministrativePrivileges(person1, team1.getId())).thenReturn(true);
    autoWireMock(controller, controllerUtil, ControllerUtil.class);

    GroupProviderService groupProviderService = mock(GroupProviderService.class);
    when(groupProviderService.getGroupProviderUserOauths(person1.getId())).thenReturn(oauthKeys());
    when(groupProviderService.getGroupProviderByStringIdentifier(groupProviderIdentifier)).thenReturn(hzProvider);
    autoWireMock(controller, groupProviderService, GroupProviderService.class);

    GroupService groupService = mock(GroupService.class);
    when(groupService.getGroup20Entry(oauthKeys().get(0), hzProvider, 250, 0)).thenReturn(null);
    autoWireMock(controller, groupService, GroupService.class);


    final String viewName = controller.showAddExternalGroupsForm(team1.getId(), modelMap, request);

    assertEquals("addexternalgroup", viewName);
    assertEquals(team1, modelMap.get("team"));
    @SuppressWarnings("unchecked") List<ExternalGroup> externalGroups = (List<ExternalGroup>) request.getSession().getAttribute("externalGroups");
    assertEquals(0, externalGroups.size());
    assertTrue(modelMap.containsKey("view"));
    assertTrue(modelMap.containsKey(TokenUtil.TOKENCHECK));
  }

  @Test
  public void testAddExternalGroups() {
    final ModelMap modelMap = getModelMap();
    final MockHttpServletRequest request = getRequest();
    final String token = TokenUtil.generateSessionToken();
//    controller.addExternalGroups(token, modelMap, request);
  }


  // ======== Mocks =========

  private List<GroupProviderUserOauth> oauthKeys() {
    List<GroupProviderUserOauth> oauthList = new ArrayList<GroupProviderUserOauth>();
    GroupProviderUserOauth oauth = new GroupProviderUserOauth(getPerson1().getId(), "hz", "token", "secret");
    oauthList.add(oauth);
    return oauthList;
  }

  private GroupProvider getHzProvider(String identifier) {
    if (!"hz".equals(identifier)) {
      return null;
    }
    GroupProvider groupProvider = new GroupProvider(4L, "hz", "HZ", GroupProviderType.OAUTH_THREELEGGED.getStringValue());
    ConversionRule groupDecorator = new ConversionRule();
    groupDecorator.setPropertyName("id");
    groupDecorator.setSearchPattern("urn:collab:group:hz.nl:(.+)");
    groupDecorator.setReplaceWith("$1");
    groupProvider.addGroupDecorator(groupDecorator);

    return groupProvider;
  }

  private Group20Entry getMyHzGroups() throws IOException {
    Resource resource = new ClassPathResource("mocks/hz-groups.json");
    return parser.parseGroups20(resource.getInputStream());
  }

  private List<TeamExternalGroup> getLinkedHzGroups(String team1Id) throws IOException {
    List<TeamExternalGroup> tegs = new ArrayList<TeamExternalGroup>();
    final List<ExternalGroup> externalGroups = ExternalGroupUtil.convertToExternalGroups(getHzProvider("hz"), getMyHzGroups());
    for (ExternalGroup eg : externalGroups) {
      TeamExternalGroup teg = new TeamExternalGroup();
      teg.setGrouperTeamId(team1Id);
      teg.setExternalGroup(eg);
      tegs.add(teg);
    }
    return tegs;
  }

}
