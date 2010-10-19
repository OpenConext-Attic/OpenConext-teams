package nl.surfnet.coin.teams.control;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.teams.util.TeamEnvironment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class JSController {

  @Autowired
  private TeamEnvironment environment;

  @RequestMapping("/js/coin-teams.js")
  public String js(ModelMap modelMap, HttpServletRequest request) {
    modelMap.addAttribute("environment", environment);
    
    return "js";
  }

}
