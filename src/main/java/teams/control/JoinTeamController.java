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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;
import teams.domain.JoinTeamRequest;
import teams.domain.Person;
import teams.domain.Team;
import teams.interceptor.LoginInterceptor;
import teams.service.GrouperTeamService;
import teams.service.JoinTeamRequestService;
import teams.util.AuditLog;
import teams.util.ControllerUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;
import static teams.util.ViewUtil.escapeViewParameters;

/**
 * {@link Controller} that handles the join team page of a logged in
 * user.
 */
@Controller
@SessionAttributes(JoinTeamController.JOIN_TEAM_REQUEST)
public class JoinTeamController {

  public static final String JOIN_TEAM_REQUEST = "joinTeamRequest";

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private JoinTeamRequestService joinTeamRequestService;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private ControllerUtil controllerUtil;

  @RequestMapping("/jointeam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {
    String teamId = request.getParameter("team");

    checkArgument(StringUtils.hasText(teamId));

    Team team = grouperTeamService.findTeamById(teamId);

    if (team == null) {
      throw new RuntimeException("Cannot find team for parameter 'team'");
    }
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    JoinTeamRequest joinTeamRequest = joinTeamRequestService.findPendingRequest(person.getId(), team.getId());
    if (joinTeamRequest == null) {
      joinTeamRequest = new JoinTeamRequest(person.getId(), team.getId(), person.getEmail(), person.getDisplayName());
    }

    modelMap.addAttribute("team", team);
    modelMap.addAttribute(JOIN_TEAM_REQUEST, joinTeamRequest);

    return "jointeam";
  }

  @RequestMapping(value = "/dojointeam.shtml", method = RequestMethod.POST)
  public RedirectView joinTeam(ModelMap modelMap,
                               @ModelAttribute(JOIN_TEAM_REQUEST) JoinTeamRequest joinTeamRequest,
                               HttpServletRequest request) throws IOException {
    Team team = controllerUtil.getTeamById(joinTeamRequest.getGroupId());

    if (!team.isViewable()) {
      throw new IllegalStateException("The team you requested to join is private.");
    }

    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    String message = joinTeamRequest.getMessage();

    controllerUtil.sendJoinTeamMail(team, person, message, localeResolver.resolveLocale(request));

    joinTeamRequest.setTimestamp(new Date().getTime());
    joinTeamRequest.setDisplayName(person.getDisplayName());
    joinTeamRequest.setEmail(person.getEmail());
    joinTeamRequestService.saveOrUpdate(joinTeamRequest);

    AuditLog.log("User {} requested to join team {}", joinTeamRequest.getPersonId(), team.getId());

    return new RedirectView(escapeViewParameters("home.shtml?teams=my"));
  }

}
