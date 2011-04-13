/**
 * 
 */
package nl.surfnet.coin.teams.control;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.teams.util.TeamEnvironment;
import nl.surfnet.coin.teams.util.ViewUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author steinwelberg
 * 
 */
@Controller
public class LandingPageController {

  @Autowired
  private TeamEnvironment teamEnvironment;

  @RequestMapping("/landingpage.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    ViewUtil.defineView(request, modelMap);

    modelMap.addAttribute("environment", teamEnvironment);

    return "landingpage";
  }
}
