/*
 * Copyright 2011 SURFnet bv, The Netherlands
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

import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.domain.TeamResultWrapper;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.TeamService;
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
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

/**
 * @author steinwelberg
 *         <p/>
 *         {@link Controller} that handles the home page of a logged in user.
 */
@Controller
public class HomeController {

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private TeamService teamService;

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private TeamEnvironment environment;

  private static final int PAGESIZE = 10;

  @RequestMapping("/home.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    String display = request.getParameter("teams");
    String query = request.getParameter("teamSearch");

    query = sanitizeQuery(query);

    // Set the display to my if no display is selected
    if (!StringUtils.hasText(display)) {
      display = "my";
    }

    addTeams(query, person.getId(), display, modelMap, request);

    String email = person.getEmail();
    if (StringUtils.hasText(email)) {
      List<Invitation> invitations = teamInviteService.findPendingInvitationsByEmail(email);
      modelMap.addAttribute("myinvitations", !CollectionUtils.isEmpty(invitations));
    }
    modelMap.addAttribute("appversion", environment.getVersion());
    ViewUtil.addViewToModelMap(request, modelMap);

    return "home";
  }

  protected String sanitizeQuery(final String query) {
    if (query == null) {
        return null;
    }
    return query.replaceAll(" ", "_");
  }

    private void addTeams(String query, final String person,
                        final String display, ModelMap modelMap,
                        HttpServletRequest request) {

    Locale locale = localeResolver.resolveLocale(request);

    if (messageSource.getMessage("jsp.home.SearchTeam", null, locale).equals(query)) {
      query = null;
    }
    modelMap.addAttribute("query", query);

    int offset = 0;
    String offsetParam = request.getParameter("offset");
    if (StringUtils.hasText(offsetParam)) {
      try {
        offset = Integer.parseInt(offsetParam);
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
    modelMap.addAttribute("offset", offset);

    TeamResultWrapper resultWrapper;
    // Display all teams when the person is empty or when display equals "all"

    if ("all".equals(display) || !StringUtils.hasText(person)) {
      String personId = LoginInterceptor.getLoggedInUser();
      modelMap.addAttribute("hasMultipleSources", true);
      if (StringUtils.hasText(query)) {
        resultWrapper = teamService.findTeams(
                personId, query, offset, PAGESIZE);
      } else {
        resultWrapper = teamService.findAllTeams(
                personId, offset, PAGESIZE);
      }
      modelMap.addAttribute("display", "all");
      // else always display my teams
    } else {
      modelMap.addAttribute("hasMultipleSources", teamService.findStemsByMember(person).size() > 1);
      if (StringUtils.hasText(query)) {
        resultWrapper = teamService.findTeamsByMember(
                person, query, offset, PAGESIZE);
      } else {
        resultWrapper = teamService.findAllTeamsByMember(
                person, offset, PAGESIZE);
      }
      modelMap.addAttribute("display", "my");
    }

    List<Team> teams = resultWrapper.getTeams();

    modelMap.addAttribute("pagesize", PAGESIZE);
    modelMap.addAttribute("pager", resultWrapper.getPager());
    modelMap.addAttribute("resultset", resultWrapper.getTotalCount());
    modelMap.addAttribute("teams", teams);
  }

  void setTeamEnvironment(TeamEnvironment teamEnvironment) {
    this.environment = teamEnvironment;
  }

}
