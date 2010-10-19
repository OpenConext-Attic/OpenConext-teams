/**
 * 
 */
package nl.surfnet.coin.teams.control;

import java.util.List;
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

/**
 * @author steinwelberg
 * 
 *         {@link Controller} that handles the home page of a logged in user.
 */
@Controller
public class HomeController {

  @Autowired
  private TeamService teamService;

  @RequestMapping("/home.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    String person = (String) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String display = request.getParameter("teams");
    
    // Set the display to my if no display is selected
    if (!StringUtils.hasText(display)) {
      display = "my";
    }

    addTeams(person, display, modelMap, request);

    return "home";
  }

  private void addTeams(String person, String display, ModelMap modelMap,
      HttpServletRequest request) {
    
    // Display all teams when the person is empty or when display equals "all"
    if (display.equals("all") || !StringUtils.hasText(person)) {
      List<Team> teams = teamService.findAllTeams();
      modelMap.addAttribute("display", "all");
      modelMap.addAttribute("teams", teams);
      
      for (Team team : teams) {
        team.setViewerRole(getViewerRole(team, person));
      }
        
      // else always display my teams
    } else {
      List<Team> teams = teamService.getTeamsByMember(person);
      
      for (Team team : teams) {
        team.setViewerRole(getViewerRole(team, person));
      }
      
      modelMap.addAttribute("display", "my");
      modelMap.addAttribute("teams", teams);
    }
  }

  private String getViewerRole(Team team, String person) {
    
    String result = "-";
    Set<Member> members = team.getMembers();
    
    for (Member member : members) {
      if (member.getId().equals(person)) {
        // Always display the role with the most privileges
        if (member.getRoles().contains(Role.Admin)) {
          result = Role.Admin.name();
        } else if (member.getRoles().contains(Role.Manager)) {
          result = Role.Manager.name();
        } else if (member.getRoles().contains(Role.Member)) {
          result = Role.Member.name();
        }
      }
    }
    return result;
  }
}
