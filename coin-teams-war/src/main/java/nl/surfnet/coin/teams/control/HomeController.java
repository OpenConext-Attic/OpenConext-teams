/**
 * 
 */
package nl.surfnet.coin.teams.control;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.TeamService;

/**
 * @author steinwelberg
 * 
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

  @RequestMapping("/home.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String display = request.getParameter("teams");
    String query = request.getParameter("teamSearch");
    String view = request.getParameter("view");
    
    if (view == null || !StringUtils.hasText(view)) {
      view = (String) request.getSession().getAttribute("view");
    }
    
    // Determine view
    if (view != null && !view.equals("gadget")) {
      view = "app";
    }
    modelMap.addAttribute("view", view);
    request.getSession().setAttribute("view", view);

    
    // Set the display to my if no display is selected
    if (!StringUtils.hasText(display)) {
      display = "my";
    }

    addTeams(query, person.getId(), display, modelMap, request);

    return "home";
  }

  private void addTeams(String query, String person, String display, ModelMap modelMap,
      HttpServletRequest request) {
    
    Locale locale = localeResolver.resolveLocale(request);
    
    if (query != null && query.equals(messageSource.getMessage("jsp.home.SearchTeam", null, locale))) {
        query = null;
    }
    
    // Display all teams when the person is empty or when display equals "all"
    if (display.equals("all") || !StringUtils.hasText(person)) {
      List<Team> teams = null;
      if (!StringUtils.hasText(query)) {
        teams = teamService.findAllTeams();
      } else {
        teams = teamService.findTeams(query);
      }
      
      modelMap.addAttribute("display", "all");
      modelMap.addAttribute("teams", teams);
      
      for (Team team : teams) {
        team.setViewerRole(person);
      }
        
      // else always display my teams
    } else {
      
      List<Team> teams = null;
      if (!StringUtils.hasText(query)) {
        teams = teamService.getTeamsByMember(person);
      } else {
        teams = teamService.findTeams(query, person);
      }
      
      for (Team team : teams) {
        team.setViewerRole(person);
      }
      
      modelMap.addAttribute("display", "my");
      modelMap.addAttribute("teams", teams);
    }
    
    modelMap.addAttribute("query", query);
  }
  
}
