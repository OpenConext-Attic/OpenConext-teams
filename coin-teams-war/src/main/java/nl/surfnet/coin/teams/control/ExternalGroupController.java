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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import nl.surfnet.coin.teams.service.GroupProviderService;
import nl.surfnet.coin.teams.service.GroupService;
import nl.surfnet.coin.teams.util.TeamEnvironment;

/**
 * Controller for external teams
 */
@Controller
@RequestMapping("/externalgroups/*")
public class ExternalGroupController {

  private static final String UTF_8 = "utf-8";

  @Autowired
  private GroupProviderService groupProviderService;

  @Autowired
  private GroupService groupService;

  @Autowired
  private TeamEnvironment environment;

  // only enable myproviders.shtml if you need to debug. It reveals too much information.
/*  @RequestMapping("/myproviders.shtml")
  public
  @ResponseBody
  List<GroupProvider> getMyExternalGroupProviders(HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    return groupProviderService.getOAuthGroupProviders(person.getId());
  }*/

/*
  @RequestMapping("/mygroups.shtml")
  public String getMyExternalGroups(@RequestParam Long groupProviderId,
                                    ModelMap modelMap,
                                    HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    List<Group20> group20s = new ArrayList<Group20>();

    // get a list of my group providers that I already have an access token for
    final List<GroupProviderUserOauth> oauthList =
        groupProviderService.getGroupProviderUserOauths(person.getId());
    List<GroupProvider> groupProviders = new ArrayList<GroupProvider>();

    for (GroupProviderUserOauth oauth : oauthList) {
      GroupProvider provider =
          groupProviderService.getGroupProviderByStringIdentifier(oauth.getProvider());
      groupProviders.add(provider);
      if (groupProviderId.equals(provider.getId())) {
        group20s.addAll(groupService.getGroup20s(oauth, provider));
      }
    }
    modelMap.addAttribute("group20s", group20s);
    modelMap.addAttribute("appversion", environment.getVersion());
    modelMap.addAttribute("groupProviders", groupProviders);
    modelMap.addAttribute("groupProviderId", groupProviderId);
    ViewUtil.addViewToModelMap(request, modelMap);

    return "home";
  }
*/

/*  @RequestMapping("/mygroupmembers.shtml")
  public
  @ResponseBody
  List<nl.surfnet.coin.api.client.domain.Person> getMyExternalGroupMembers(HttpServletRequest request) throws UnsupportedEncodingException {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String groupId = URLDecoder.decode(request.getParameter("groupId"), UTF_8);

    // get a list of my group providers that I already have an access token for
    final List<GroupProviderUserOauth> oauthList =
        groupProviderService.getGroupProviderUserOauths(person.getId());
    for (GroupProviderUserOauth oauth : oauthList) {
      GroupProvider provider =
          groupProviderService.getGroupProviderByStringIdentifier(oauth.getProvider());
      if (GroupProviderPropertyConverter.isGroupFromGroupProvider(groupId, provider)) {
        return groupService.getGroupMembers(oauth, provider, groupId);
      }
    }
    return new ArrayList<nl.surfnet.coin.api.client.domain.Person>();
  }*/

}
