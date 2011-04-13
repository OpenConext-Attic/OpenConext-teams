/**
 * 
 */
package nl.surfnet.coin.teams.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author steinwelberg
 *
 */
@Controller
public class HelpController {

  @RequestMapping("/help.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {
    return "help";
  }
}
