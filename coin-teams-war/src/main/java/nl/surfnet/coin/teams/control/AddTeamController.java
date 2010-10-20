package nl.surfnet.coin.teams.control;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
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
 *         {@link Controller} that handles the add team page of a logged in
 *         user.
 */
@Controller
public class AddTeamController {

  @Autowired
  private TeamService teamService;

  @RequestMapping("/addteam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    return "addteam";
  }

  @RequestMapping(value = "/doaddteam.shtml", method = RequestMethod.POST)
  public RedirectView addTeam(ModelMap modelMap, HttpServletRequest request) {
    
    String personId = (String) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    String teamName = request.getParameter("team");
    String teamDescription = request.getParameter("description");
    
    // If viewablilityStatus is set this means that the team should be private
    boolean viewable = !StringUtils.hasText(request.getParameter("viewabilityStatus"));
    
    // Form not completely filled in.
    if (!StringUtils.hasText(teamName) || !StringUtils.hasText(teamDescription)) {
      throw new RuntimeException("Parameter error.");
    }
    
    // Add the team
    String teamId = teamService.addTeam(teamName, teamName, teamDescription, viewable);
    
    // Add the person who has added the team as admin to the team.
    teamService.addMember(teamId, personId);
    
    // Give him the right permissions
    teamService.updateMember(teamId, personId, Role.Admin);
    teamService.updateMember(teamId, personId, Role.Manager);

    return new RedirectView("detailteam.shtml?team=" + teamId);
  }
}
