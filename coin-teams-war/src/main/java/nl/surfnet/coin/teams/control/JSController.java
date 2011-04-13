package nl.surfnet.coin.teams.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author steinwelberg
 * 
 * {@link Controller} that handles the javascript of a user.
 */
@Controller
public class JSController {

  @RequestMapping("/js/coin-teams.js")
  public String js(ModelMap modelMap, HttpServletRequest request) {
    
    return "js";
  }

}
