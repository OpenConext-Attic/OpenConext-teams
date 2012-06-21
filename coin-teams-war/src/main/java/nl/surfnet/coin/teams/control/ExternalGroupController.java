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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.api.client.domain.GroupMembersEntry;
import nl.surfnet.coin.teams.domain.ExternalGroupDetailWrapper;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.Pager;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.ExternalGroupProviderProcessor;
import nl.surfnet.coin.teams.util.ViewUtil;

import nl.surfnet.coin.api.client.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for external teams
 */
@Controller
@RequestMapping("/externalgroups/*")
public class ExternalGroupController {

  private static final int PAGESIZE = 10;
  
  @Autowired
  private ExternalGroupProviderProcessor processor;

  @RequestMapping("/groupdetail.shtml")
  public String groupDetail(@RequestParam
  String groupId, @RequestParam
  String externalGroupProviderIdentifier, @RequestParam(defaultValue = "0", required = false)
  int offset, HttpServletRequest request, ModelMap modelMap) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    List<GroupProvider> allGroupProviders = (List<GroupProvider>) request.getSession().getAttribute(
        HomeController.ALL_GROUP_PROVIDERS_SESSION_KEY);
    modelMap.addAttribute("groupId", groupId);

    ExternalGroupDetailWrapper groupDetails = processor.getGroupDetails(person.getId(), groupId, allGroupProviders,
        externalGroupProviderIdentifier, offset, PAGESIZE);
    GroupProvider groupProvider = processor.getGroupProviderByStringIdentifier(externalGroupProviderIdentifier,
        allGroupProviders);
    modelMap.addAttribute("groupProvider", groupProvider);
    modelMap.addAttribute("group20", groupDetails.getGroup20());
    GroupMembersEntry groupMembersEntry = groupDetails.getGroupMembersEntry();
    modelMap.addAttribute("groupMembersEntry", groupMembersEntry);
    if (groupMembersEntry != null && groupMembersEntry.getEntry().size() <= PAGESIZE) {
      Pager pager = new Pager(groupMembersEntry.getTotalResults(), offset, PAGESIZE);
      modelMap.addAttribute("pager", pager);
    }
    ViewUtil.addViewToModelMap(request, modelMap);
    return "external-groupdetail";
  }

}
