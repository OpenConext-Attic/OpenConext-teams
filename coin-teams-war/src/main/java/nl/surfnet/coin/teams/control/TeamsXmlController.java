/**
 * 
 */
package nl.surfnet.coin.teams.control;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.teams.util.TeamEnvironment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author steinwelberg
 *
 */
@Controller
public class TeamsXmlController {
  
  @Autowired
  private TeamEnvironment teamEnvironment;

  @RequestMapping("/teams.xml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    modelMap.addAttribute("teamsURL", teamEnvironment.getTeamsURL());
 
    return "teams";
  }
  
}
