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

import javax.servlet.http.HttpServletRequest;

import org.opensocial.models.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import nl.surfnet.coin.api.client.domain.Group20;
import nl.surfnet.coin.api.client.domain.Group20Entry;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.GroupProviderUserOauth;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GroupProviderService;
import nl.surfnet.coin.teams.service.GroupService;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.TokenUtil;
import nl.surfnet.coin.teams.util.ViewUtil;

/**
 * Controller to add an external group to a SURFteam
 */
@Controller
@SessionAttributes({TokenUtil.TOKENCHECK})
public class AddExternalGroupController {

  @Autowired
  private GroupProviderService groupProviderService;

  @Autowired
  private GroupService groupService;

  @Autowired
  private ControllerUtil controllerUtil;

  private static final Logger log = LoggerFactory.getLogger(AddExternalGroupController.class);

  @RequestMapping("addexternalgroup.shtml")
  public String showAddExternalGroupsForm(@RequestParam String teamId, ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();
    if (!controllerUtil.hasUserAdministrativePrivileges(person, teamId)) {
      throw new RuntimeException("Requester (" + person.getId() + ") is not member or does not have the correct " +
          "privileges to add external groups");
    }

    final List<GroupProviderUserOauth> oauthList = groupProviderService.getGroupProviderUserOauths(personId);
    final List<Group20> group20List = new ArrayList<Group20>();
    final List<GroupProvider> groupProviders = new ArrayList<GroupProvider>();
    for (GroupProviderUserOauth oauth : oauthList) {
      final GroupProvider groupProvider = groupProviderService.getGroupProviderByStringIdentifier(oauth.getProvider());
      groupProviders.add(groupProvider);
      try {
        final Group20Entry entry = groupService.getGroup20Entry(oauth, groupProvider, 250, 0);
        if (entry == null) {
          continue;
        }
        group20List.addAll(entry.getEntry());
      } catch (RuntimeException e) {
        log.info("Failed to retrieve external groups for user " + personId + " and provider " + groupProvider.getIdentifier(), e);
      }
    }
    modelMap.addAttribute("groupProviders", groupProviders);
    modelMap.addAttribute("group20List", group20List);
    ViewUtil.addViewToModelMap(request, modelMap);
    return "addexternalgroup";
  }

}
