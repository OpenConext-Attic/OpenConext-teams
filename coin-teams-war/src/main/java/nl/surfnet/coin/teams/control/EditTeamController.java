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

package nl.surfnet.coin.teams.control;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.TokenUtil;
import nl.surfnet.coin.teams.util.ViewUtil;
import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author steinwelberg
 *         <p/>
 *         {@link Controller} that handles the edit team page of a logged in
 *         user.
 */
@Controller
@SessionAttributes({TokenUtil.TOKENCHECK})
public class EditTeamController {

  @Autowired
  private TeamService teamService;

  @RequestMapping("/editteam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    Team team = getTeam(request);

    // Check if a user has the privileges to edit the team
    if (!hasUserAdminPrivileges(person, team.getId())) {
      throw new RuntimeException("Member (" + person.getId() + ") does not have the correct privileges to edit team " +
              "(" + team.getName() + ")");
    }

    modelMap.addAttribute("team", team);
    modelMap.addAttribute(TokenUtil.TOKENCHECK, TokenUtil.generateSessionToken());
    ViewUtil.addViewToModelMap(request, modelMap);

    return "editteam";
  }

  @RequestMapping("/vo/{voName}/editteam.shtml")
  public String startVO(@PathVariable String voName, ModelMap modelMap, HttpServletRequest request) {
    return start(modelMap, request);
  }

  @RequestMapping(value = "/doeditteam.shtml", method = RequestMethod.POST)
  public RedirectView editTeam(ModelMap modelMap,
                               HttpServletRequest request,
                               @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                               @RequestParam() String token,
                               SessionStatus status)
          throws UnsupportedEncodingException {
    // Check if the token is valid
    TokenUtil.checkTokens(sessionToken, token, status);

    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    String teamId = request.getParameter("team");
    String teamName = request.getParameter("teamName");
    String teamDescription = request.getParameter("description");

    Team team = getTeam(request);

    // Check if a user has the privileges to edit the team
    if (!hasUserAdminPrivileges(person, team.getId())) {
      throw new RuntimeException("Member (" + person.getId() + ") does not have the correct privileges to edit team " +
              "(" + team.getName() + ")");
    }

    // If viewablilityStatus is set this means that the team should be private
    boolean viewable = !StringUtils.hasText(request
            .getParameter("viewabilityStatus"));

    // Form not completely filled in.
    if (!StringUtils.hasText(teamName)) {
      throw new RuntimeException("Parameter error.");
    }

    // Update the team info
    teamService.updateTeam(teamId, teamName, teamDescription);
    teamService.setVisibilityGroup(teamId, viewable);

    status.setComplete();
    return new RedirectView("detailteam.shtml?team="
            + URLEncoder.encode(teamId, "utf-8") + "&view="
            + ViewUtil.getView(request), false, true, false);
  }

  @RequestMapping(value = "/vo/{voName}/doeditteam.shtml", method = RequestMethod.POST)
  public RedirectView editTeamVO(@PathVariable String voName,
                                 ModelMap modelMap,
                                 HttpServletRequest request,
                                 @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                 @RequestParam() String token,
                                 SessionStatus status)
          throws UnsupportedEncodingException {
    return editTeam(modelMap, request, sessionToken, token, status);
  }

  private Team getTeam(HttpServletRequest request) {
    String teamId = request.getParameter("team");
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }
    if (team == null) {
      throw new RuntimeException("Team (" + teamId + ") not found");
    }
    return team;
  }

  private boolean hasUserAdminPrivileges(Person person, String teamId) {
    // Check if the requester is member of the team AND
    // Check if the requester has the role admin or manager, so he is allowed to invite new members.
    Member member = teamService.findMember(teamId, person.getId());
    return member != null && (member.getRoles().contains(Role.Admin));
  }
}
