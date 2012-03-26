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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.surfnet.coin.api.client.domain.Group20;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.GroupProviderUserOauth;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GroupProviderService;
import nl.surfnet.coin.teams.service.GroupService;

/**
 * Controller for external teams
 */
@Controller
@RequestMapping("/externalgroups/*")
public class ExternalTeamsController {

  @Autowired
  private GroupProviderService groupProviderService;

  @Autowired
  private GroupService groupService;

  @RequestMapping("/myproviders.shtml")
  public
  @ResponseBody
  List<GroupProvider> getMyExternalGroupProviders(HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    return groupProviderService.getOAuthGroupProviders(person.getId());
  }

  @RequestMapping("/mygroups.shtml")
  public
  @ResponseBody
  List<Group20> getMyExternalGroups(HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);

    List<Group20> group20s = new ArrayList<Group20>();

    // get a list of my group providers that I already have an access token for
    final List<GroupProviderUserOauth> oauthList =
        groupProviderService.getGroupProviderUserOauths(person.getId());
    for (GroupProviderUserOauth oauth : oauthList) {
      group20s.addAll(groupService.getGroup20s(oauth));
      // if 1 provider fails, error is returned, but this is not the final UI anyway
    }
    return group20s;
  }


}
