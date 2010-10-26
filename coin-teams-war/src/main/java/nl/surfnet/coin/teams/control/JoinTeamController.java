package nl.surfnet.coin.teams.control;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author steinwelberg
 * 
 *         {@link Controller} that handles the join team page of a logged in
 *         user.
 */
@Controller
public class JoinTeamController {

  @Autowired
  private TeamService teamService;

  @RequestMapping("/jointeam.shtml")
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

    return "jointeam";
  }

  @RequestMapping(value = "/dojointeam.shtml", method = RequestMethod.POST)
  public RedirectView joinTeam(ModelMap modelMap, HttpServletRequest request) {
    
    String teamId = request.getParameter("team");
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }
    
    if (team == null) {
      throw new RuntimeException("Parameter error");
    }
    
    // TODO use SURFteams API

    return new RedirectView("home.shtml?teams=my");
  }

}
