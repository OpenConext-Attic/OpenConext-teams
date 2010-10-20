package nl.surfnet.coin.teams.control;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.teams.domain.Member;
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
 *         {@link Controller} that handles the detail team page of a logged in
 *         user.
 */
@Controller
public class DetailTeamController {

  @Autowired
  private TeamService teamService;

  @RequestMapping("/detailteam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    String person = (String) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String teamId = request.getParameter("team");
    Set<Role> roles = new HashSet<Role>();
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(request.getParameter("team"));
    }

    if (team == null || !StringUtils.hasText(person)) {
      throw new RuntimeException("Wrong parameters.");
    }

    Set<Member> members = team.getMembers();

    // Iterate over the members to see if the logged in user is a member of the
    // team.
    for (Member member : members) {
      if (member.getId().equals(person)) {
        roles = member.getRoles();
      }
    }

    modelMap.addAttribute("team", team);
    modelMap.addAttribute("admin", Role.Admin);
    modelMap.addAttribute("manager", Role.Manager);

    if (roles.contains(Role.Admin)) {
      return "detailteam-admin";
    } else if (roles.contains(Role.Manager)) {
      return "detailteam-manager";
    } else if (roles.contains(Role.Member)) {
      return "detailteam-member";
    } else {
      return "detailteam-not-member";
    }
  }

  @RequestMapping(value = "/doleaveteam.shtml", method = RequestMethod.GET)
  public RedirectView leaveTeam(ModelMap modelMap, HttpServletRequest request) {
    String teamId = request.getParameter("team");
    String personId = (String) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);

    if (!StringUtils.hasText(teamId)) {
      throw new RuntimeException("Parameter error.");
    }

    // Leave the team
    teamService.deleteMember(teamId, personId);

    return new RedirectView("home.shtml?teams=my");
  }
  
  @RequestMapping(value = "/dodeleteteam.shtml", method = RequestMethod.GET)
  public RedirectView deleteTeam(ModelMap modelMap, HttpServletRequest request) {
    String teamId = request.getParameter("team");

    if (!StringUtils.hasText(teamId)) {
      throw new RuntimeException("Parameter error.");
    }

    // Delete the team
    teamService.deleteTeam(teamId);

    return new RedirectView("home.shtml?teams=my");
  }
}
