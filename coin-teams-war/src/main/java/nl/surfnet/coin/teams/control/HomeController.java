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
 * {@link Controller} that handles the home page of a logged in user.
 */
@Controller
public class HomeController {
	
	  @RequestMapping("/home.shtml")
	  public String start(ModelMap modelMap, HttpServletRequest request) {
		  		  
		  return "home";
	  }
	  
}

