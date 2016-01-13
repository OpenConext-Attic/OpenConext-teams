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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static teams.interceptor.LoginInterceptor.EXTERNAL_GROUPS_SESSION_KEY;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

  protected static final int PAGESIZE = 10;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private VootClient vootClient;

  @RequestMapping(value = {"/", "/home.shtml"})
  public String start(ModelMap modelMap, HttpServletRequest request,
                      Locale locale,
                      @RequestParam(required = false, defaultValue = "my") String teams,
                      @RequestParam(required = false) String teamSearch,
                      @RequestParam(required = false) String groupProviderId) {
    Person person = (Person) request.getSession().getAttribute(PERSON_SESSION_KEY);

    checkNotNull(person, "No user set. Is shibboleth configured correctly?");

    modelMap.addAttribute("groupProviderId", groupProviderId);
    if ("externalGroups".equals(teams)) {
      modelMap.addAttribute("display", teams);
    } else {
      addTeams(teamSearch, person.getId(), teams, modelMap, locale, request);
    }
    String email = person.getEmail();
    if (StringUtils.hasText(email)) {
      List<Invitation> invitations = teamInviteService.findPendingInvitationsByEmail(email);
      modelMap.addAttribute("myinvitations", !CollectionUtils.isEmpty(invitations));
    }

    List<ExternalGroup> externalGroups = getExternalGroups(person, request);
    Map<String, ExternalGroupProvider> groupProviders = externalGroups.stream().collect(toMap(ExternalGroup::getGroupProviderIdentifier, ExternalGroup::getGroupProvider, (eg1, eg2) -> eg1));

    if (groupProviderId != null) {
      addExternalGroupsToModelMap(modelMap, getOffset(request), groupProviders.get(groupProviderId), externalGroups);
    }

    modelMap.addAttribute("groupProviders", groupProviders.values());

    ViewUtil.addViewToModelMap(request, modelMap);

    return "home";
  }

  private List<ExternalGroup> getExternalGroups(Person person, HttpServletRequest request) {
    @SuppressWarnings("unchecked")
    List<ExternalGroup> groups = (List<ExternalGroup>) request.getSession().getAttribute(EXTERNAL_GROUPS_SESSION_KEY);
    if (groups == null) {
      groups = vootClient.groups(person.getId());
      request.getSession().setAttribute(EXTERNAL_GROUPS_SESSION_KEY, groups);
    }

    return groups;
  }

  private void addExternalGroupsToModelMap(ModelMap modelMap, int offset, ExternalGroupProvider externalGroupProvider, List<ExternalGroup> groups) {
    modelMap.addAttribute("externalGroupProvider", externalGroupProvider);

    List<ExternalGroup> filteredGroups = groups.stream()
        .filter(group -> group.getGroupProviderIdentifier().equals(externalGroupProvider.getIdentifier()))
        .collect(toList());

    if (filteredGroups.size() >= PAGESIZE) {
      Pager pager = new Pager(filteredGroups.size(), offset, PAGESIZE);
      modelMap.addAttribute("pager", pager);
      filteredGroups = filteredGroups.subList(offset, offset + PAGESIZE);
    }

    modelMap.addAttribute("externalGroups", filteredGroups);
  }

  private void addTeams(String query, String person, String display, ModelMap modelMap, Locale locale, HttpServletRequest request) {
    if (messageSource.getMessage("jsp.home.SearchTeam", null, locale).equals(query)) {
      query = null;
    }
    modelMap.addAttribute("query", query);

    int offset = getOffset(request);
    modelMap.addAttribute("offset", offset);

    TeamResultWrapper resultWrapper;
    // Display all teams when the person is empty or when display equals "all"
    if ("all".equals(display) || !StringUtils.hasText(person)) {
      if (StringUtils.hasText(query)) {
        List<Team> matchingTeams = grouperTeamService.findPublicTeams(person, query);
        resultWrapper = new TeamResultWrapper(matchingTeams, matchingTeams.size(), 0, 1000);
      } else {
        resultWrapper = new TeamResultWrapper(new ArrayList<>(), 0, 0, 1);
      }
      modelMap.addAttribute("display", "all");
      // else always display my teams
    } else {
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
  public TeamResultWrapper findTeams(HttpServletRequest request, @RequestParam(required = false) String teamSearch) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    List<Team> matchedTeams = grouperTeamService.findPublicTeams(person.getId(), teamSearch);

    return new TeamResultWrapper(matchedTeams, matchedTeams.size(), 0, PAGESIZE);
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
