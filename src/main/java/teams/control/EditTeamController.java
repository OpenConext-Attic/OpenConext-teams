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

package teams.control;

import static teams.util.ViewUtil.escapeViewParameters;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.view.RedirectView;

import teams.domain.Person;
import teams.domain.Team;
import teams.interceptor.LoginInterceptor;
import teams.service.GrouperTeamService;
import teams.util.AuditLog;
import teams.util.ControllerUtil;
import teams.util.TokenUtil;
import teams.util.ViewUtil;

/**
 * {@link Controller} that handles the edit team page of a logged in user.
 */
@Controller
@SessionAttributes({TokenUtil.TOKENCHECK})
public class EditTeamController {

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private ControllerUtil controllerUtil;

  @RequestMapping("/editteam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    Team team = getTeam(request);

    // Check if a user has the privileges to edit the team
    if (!controllerUtil.hasUserAdminPrivileges(person, team.getId())) {
      throw new RuntimeException("Member (" + person.getId() + ") does not have the correct privileges to edit team " +
        "(" + team.getName() + ")");
    }

    modelMap.addAttribute("team", team);
    modelMap.addAttribute(TokenUtil.TOKENCHECK, TokenUtil.generateSessionToken());
    ViewUtil.addViewToModelMap(request, modelMap);

    return "editteam";
  }

  @RequestMapping(value = "/doeditteam.shtml", method = RequestMethod.POST)
  public RedirectView editTeam(ModelMap modelMap,
                               HttpServletRequest request,
                               @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                               @RequestParam() String token,
                               SessionStatus status) {
    TokenUtil.checkTokens(sessionToken, token, status);

    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    String teamId = request.getParameter("team");
    String teamDescription = request.getParameter("description");

    Team team = getTeam(request);
    String teamName = team.getName(); // the name does not change on edit

    // Check if a user has the privileges to edit the team
    if (!controllerUtil.hasUserAdminPrivileges(person, team.getId())) {
      throw new RuntimeException("Member (" + person.getId() + ") does not have the correct privileges to edit team " +
        "(" + team.getName() + ")");
    }

    // If viewablilityStatus is set this means that the team should be public
    boolean viewable = StringUtils.hasText(request.getParameter("viewabilityStatus"));

    // Update the team info
    grouperTeamService.updateTeam(teamId, teamName, teamDescription, person.getId());
    grouperTeamService.setVisibilityGroup(teamId, viewable);
    AuditLog.log("User {} edited team details of team {}. Original name, description, viewability: '{}', '{}', {}. New: '{}', '{}', {}.",
      person.getId(), teamId,
      team.getName(), team.getDescription(), team.isViewable(),
      teamName, teamDescription, viewable);

    status.setComplete();
    modelMap.clear();

    return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s&view=%s", teamId, ViewUtil.getView(request)));
  }

  private Team getTeam(HttpServletRequest request) {
    String teamId = request.getParameter("team");
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = grouperTeamService.findTeamById(teamId);
    }
    if (team == null) {
      throw new RuntimeException("Team (" + teamId + ") not found");
    }
    return team;
  }
}
