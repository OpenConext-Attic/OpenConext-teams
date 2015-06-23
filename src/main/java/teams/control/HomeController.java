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

/**
 *
 */
package teams.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;

import com.google.common.base.Preconditions;

import teams.domain.ExternalGroup;
import teams.domain.ExternalGroupProvider;
import teams.domain.Invitation;
import teams.domain.Pager;
import teams.domain.Person;
import teams.domain.Team;
import teams.domain.TeamResultWrapper;
import teams.interceptor.LoginInterceptor;
import teams.service.GrouperTeamService;
import teams.service.TeamInviteService;
import teams.service.VootClient;
import teams.util.ViewUtil;

@Controller
public class HomeController {

  private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private TeamInviteService teamInviteService;

  @Value("${app.version}")
  private String version;

  @Autowired
  private VootClient vootClient;

  private static final int PAGESIZE = 10;

  @RequestMapping(value = {"/", "/home.shtml"})
  public String start(ModelMap modelMap, HttpServletRequest request,
                      @RequestParam(required = false, defaultValue = "my") String teams,
                      @RequestParam(required = false) String teamSearch,
                      @RequestParam(required = false) String groupProviderId) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    Preconditions.checkNotNull(person, "No user set. Is shibboleth configured correctly?");
    modelMap.addAttribute("groupProviderId", groupProviderId);
    if ("externalGroups".equals(teams)) {
      modelMap.addAttribute("display", teams);
    } else {
      addTeams(teamSearch, person.getId(), teams, modelMap, request);
    }
    String email = person.getEmail();
    if (StringUtils.hasText(email)) {
      List<Invitation> invitations = teamInviteService.findPendingInvitationsByEmail(email);
      modelMap.addAttribute("myinvitations", !CollectionUtils.isEmpty(invitations));
    }
    List<ExternalGroup> groups = (List<ExternalGroup>) request.getSession().getAttribute(LoginInterceptor.EXTERNAL_GROUPS_SESSION_KEY);
    if (groups == null) {
      groups = vootClient.groups(person.getId());
      request.getSession().setAttribute(LoginInterceptor.EXTERNAL_GROUPS_SESSION_KEY, groups);
    }

    Map<String, ExternalGroupProvider> groupProviders = new HashMap<>();
    groups.forEach(group -> groupProviders.put(group.getGroupProviderIdentifier(), group.getGroupProvider()));

    if (groupProviderId != null) {
      addExternalGroupsToModelMap(modelMap, getOffset(request), groupProviderId, groupProviders, groups);
    }

    // Add the external group providers to the ModelMap for the navigation
    modelMap.addAttribute("groupProviders", groupProviders.values());

    modelMap.addAttribute("appversion", version);
    ViewUtil.addViewToModelMap(request, modelMap);
    modelMap.addAttribute("logger", LOG);
    return "home";
  }

  private void addExternalGroupsToModelMap(ModelMap modelMap, int offset, String groupProviderId,
                                           Map<String, ExternalGroupProvider> groupProviders, List<ExternalGroup> groups) {
    modelMap.addAttribute("externalGroupProvider", groupProviders.get(groupProviderId));
    List<ExternalGroup> filteredGroups = new ArrayList<>();
    for (ExternalGroup group : groups) {
      if (group.getGroupProviderIdentifier().equals(groupProviderId)) {
        filteredGroups.add(group);
      }
    }
    if (filteredGroups.size() >= PAGESIZE) {
      Pager pager = new Pager(filteredGroups.size(), offset, PAGESIZE);
      modelMap.addAttribute("pager", pager);
      filteredGroups = filteredGroups.subList(offset, offset + PAGESIZE);
    }
    modelMap.addAttribute("externalGroups", filteredGroups);
  }

  private void addTeams(String query, final String person, final String display, ModelMap modelMap,
                        HttpServletRequest request) {

    Locale locale = localeResolver.resolveLocale(request);

    if (messageSource.getMessage("jsp.home.SearchTeam", null, locale).equals(query)) {
      query = null;
    }
    modelMap.addAttribute("query", query);

    int offset = getOffset(request);
    modelMap.addAttribute("offset", offset);

    TeamResultWrapper resultWrapper;
    // Display all teams when the person is empty or when display equals "all"
    if ("all".equals(display) || !StringUtils.hasText(person)) {
      modelMap.addAttribute("hasMultipleSources", true);
      if (StringUtils.hasText(query)) {
        resultWrapper = grouperTeamService.findPublicTeams(person, query);
      } else {
        resultWrapper = new TeamResultWrapper(new ArrayList<Team>(), 0, 0, 1);
      }
      modelMap.addAttribute("display", "all");
      // else always display my teams
    } else {
      modelMap.addAttribute("hasMultipleSources", grouperTeamService.findStemsByMember(person).size() > 1);
      if (StringUtils.hasText(query)) {
        resultWrapper = grouperTeamService.findTeamsByMember(person, query, offset, PAGESIZE);
      } else {
        resultWrapper = grouperTeamService.findAllTeamsByMember(person, offset, PAGESIZE);
      }
      modelMap.addAttribute("display", "my");
    }

    List<Team> teams = resultWrapper.getTeams();

    modelMap.addAttribute("pagesize", PAGESIZE);
    modelMap.addAttribute("pager", resultWrapper.getPager());
    modelMap.addAttribute("resultset", resultWrapper.getTotalCount());
    modelMap.addAttribute("teams", teams);
  }

  @RequestMapping("/findPublicTeams.json")
  @ResponseBody
  public TeamResultWrapper findTeams(HttpServletRequest request,
                                     @RequestParam(required = false) String teamSearch) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    return grouperTeamService.findPublicTeams(person.getId(), teamSearch);
  }

  private int getOffset(HttpServletRequest request) {
    int offset = 0;
    String offsetParam = request.getParameter("offset");
    if (StringUtils.hasText(offsetParam)) {
      try {
        offset = Integer.parseInt(offsetParam);
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
    return offset;
  }

}
