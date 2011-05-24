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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.ViewUtil;

/**
 * @author steinwelberg
 * 
 *         {@link Controller} that handles the edit team page of a logged in
 *         user.
 */
@Controller
public class EditTeamController {

  @Autowired
  private TeamService teamService;

  @RequestMapping("/editteam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    String teamId = request.getParameter("team");
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }

    if (team != null) {
      modelMap.addAttribute("team", team);
    } else {
      // Team does not exist
      throw new RuntimeException("Parameter error.");
    }

    ViewUtil.addViewToModelMap(request, modelMap);

    return "editteam";
  }

  @RequestMapping(value = "/doeditteam.shtml", method = RequestMethod.POST)
  public RedirectView editTeam(ModelMap modelMap, HttpServletRequest request)
      throws UnsupportedEncodingException {
    String teamId = request.getParameter("teamId");
    String teamName = request.getParameter("team");
    String teamDescription = request.getParameter("description");

    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }

    // If viewablilityStatus is set this means that the team should be private
    boolean viewable = !StringUtils.hasText(request
        .getParameter("viewabilityStatus"));

    // Form not completely filled in.
    if (!StringUtils.hasText(teamName) || team == null) {
      throw new RuntimeException("Parameter error.");
    }

    // Update the team info
    teamService.updateTeam(teamId, teamName, teamDescription);
    teamService.setVisibilityGroup(teamId, viewable);

    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8") + "&view="
        + ViewUtil.getView(request));
  }

}
