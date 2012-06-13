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
package nl.surfnet.coin.teams.control;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.api.client.domain.Group20Entry;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Pager;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.domain.TeamResultWrapper;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.ExternalGroupProviderProcessor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import nl.surfnet.coin.teams.util.ViewUtil;

import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

/**
 * @author steinwelberg
 *         <p/>
 *         {@link Controller} that handles the home page of a logged in user.
 */
@Controller
public class HomeController {

  public static final String ALL_GROUP_PROVIDERS_SESSION_KEY = "ALL_GROUP_PROVIDERS_SESSION_KEY";

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private TeamEnvironment environment;

  @Autowired
  private ExternalGroupProviderProcessor processor;

  private static final int PAGESIZE = 10;

  @RequestMapping("/home.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request,
      @RequestParam(required = false, defaultValue = "my")
      String teams, @RequestParam(required = false)
      String teamSearch, @RequestParam(required = false)
      Long groupProviderId) {

    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    String display = teams;
    String query = teamSearch;
    modelMap.addAttribute("groupProviderId", groupProviderId);

    if ("externalGroups".equals(display)) {
      modelMap.addAttribute("display", display);
    } else {
      addTeams(query, person.getId(), display, modelMap, request);
    }

    String email = person.getEmail();
    if (StringUtils.hasText(email)) {
      List<Invitation> invitations = teamInviteService.findPendingInvitationsByEmail(email);
      modelMap.addAttribute("myinvitations", !CollectionUtils.isEmpty(invitations));
    }

    List<GroupProvider> allGroupProviders = getAllGroupProviders(request);

    List<GroupProvider> groupProviders = processor.getGroupProvidersForUser(person.getId(), allGroupProviders);

    if (groupProviderId != null) {
      addExternalGroupsToModelMap(modelMap, getOffset(request), groupProviderId, person, allGroupProviders);
    }

    // Add the external group providers to the ModelMap for the navigation
    modelMap.addAttribute("groupProviders", groupProviders);

    modelMap.addAttribute("appversion", environment.getVersion());
    ViewUtil.addViewToModelMap(request, modelMap);

    return "home";
  }

  private void addExternalGroupsToModelMap(ModelMap modelMap, int offset, Long groupProviderId, Person person,
      List<GroupProvider> allGroupProviders) {
    GroupProvider groupProvider = processor.getGroupProviderByLongIdentifier(groupProviderId, allGroupProviders);
    modelMap.addAttribute("externalGroupProvider", groupProvider);

    Group20Entry group20Entry = processor.getExternalGroupsForGroupProviderId(groupProvider, person.getId(), offset,
        PAGESIZE);
    modelMap.addAttribute("group20Entry", group20Entry);
    if (group20Entry != null && group20Entry.getEntry().size() <= PAGESIZE) {
      Pager pager = new Pager(group20Entry.getTotalResults(), offset, PAGESIZE);
      modelMap.addAttribute("pager", pager);
    }
  }

  /*
   * We store all groupProviders in the session
   */
  @SuppressWarnings("unchecked")
  private List<GroupProvider> getAllGroupProviders(HttpServletRequest request) {
    List<GroupProvider> allGroupProviders = (List<GroupProvider>) request.getSession().getAttribute(
        ALL_GROUP_PROVIDERS_SESSION_KEY);
    if (allGroupProviders == null) {
      allGroupProviders = processor.getAllGroupProviders();
      request.getSession().setAttribute(ALL_GROUP_PROVIDERS_SESSION_KEY, allGroupProviders);
    }
    return allGroupProviders;
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
        resultWrapper = grouperTeamService.findTeams(person, query, offset, PAGESIZE);
      } else {
        resultWrapper = grouperTeamService.findAllTeams(person, offset, PAGESIZE);
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

  void setTeamEnvironment(TeamEnvironment teamEnvironment) {
    this.environment = teamEnvironment;
  }

}
