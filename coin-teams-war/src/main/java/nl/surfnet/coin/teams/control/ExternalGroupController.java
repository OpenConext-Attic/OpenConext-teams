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

import nl.surfnet.coin.api.client.domain.Group20;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.GroupProviderUserOauth;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GroupProviderService;
import nl.surfnet.coin.teams.service.GroupService;
import nl.surfnet.coin.teams.util.GroupProviderPropertyConverter;
import nl.surfnet.coin.teams.util.ViewUtil;

/**
 * Controller for external teams
 */
@Controller
@RequestMapping("/externalgroups/*")
public class ExternalGroupController {

  @Autowired
  private GroupProviderService groupProviderService;

  @Autowired
  private GroupService groupService;

  private static final Logger log = LoggerFactory.getLogger(ExternalGroupController.class);

  @RequestMapping("/groupdetail.shtml")
  public String groupDetail(@RequestParam String groupId,
                            HttpServletRequest request,
                            ModelMap modelMap) {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    List<nl.surfnet.coin.api.client.domain.Person> members = new ArrayList<nl.surfnet.coin.api.client.domain.Person>();
    // get a list of my group providers that I already have an access token for
    final List<GroupProviderUserOauth> oauthList =
        groupProviderService.getGroupProviderUserOauths(person.getId());
    for (GroupProviderUserOauth oauth : oauthList) {
      GroupProvider provider =
          groupProviderService.getGroupProviderByStringIdentifier(oauth.getProvider());

      if (GroupProviderPropertyConverter.isGroupFromGroupProvider(groupId, provider)) {
        modelMap.addAttribute("groupProvider", provider);

        Group20 group20 = getGroup20FromGroupProvider(groupId, oauth, provider);
        modelMap.addAttribute("group20", group20);

        // TODO replace with paging
        final List<nl.surfnet.coin.api.client.domain.Person> groupMembers =
            groupService.getGroupMembers(oauth, provider, groupId);
        members.addAll(groupMembers);
      }
    }
    modelMap.addAttribute("members", members);
    ViewUtil.addViewToModelMap(request, modelMap);
    return "external-groupdetail";
  }

  /**
   * TODO replace this method with {@link GroupService#getGroup20(nl.surfnet.coin.teams.domain.GroupProviderUserOauth, nl.surfnet.coin.teams.domain.GroupProvider, String)}
   * when all institutions comply to the OS spec
   */
  private Group20 getGroup20FromGroupProvider(String groupId, GroupProviderUserOauth oauth, GroupProvider provider) {
    Group20 group20 = null;
    try {
      group20 = groupService.getGroup20(oauth, provider, groupId);
    } catch (RuntimeException e) {
      log.debug("Group provider does not support retrieving single group, will iterate over all groups",
          e.getMessage());
      final List<Group20> group20List = groupService.getGroup20List(oauth, provider);
      for (Group20 g : group20List) {
        if (groupId.equals(group20.getId())) {
          group20 = g;
          break;
        }
      }
    }
    return group20;
  }
}
