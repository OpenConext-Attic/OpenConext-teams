package nl.surfnet.coin.teams.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class AddTeamController {
	
	  @RequestMapping("/addteam.shtml")
	  public String start(ModelMap modelMap, HttpServletRequest request) {
		  
		  return "addteam";
	  }
	  
	  @RequestMapping(value = "/doaddteam.shtml", method = RequestMethod.POST)
	  public RedirectView addTeam(ModelMap modelMap, HttpServletRequest request) {
		  
		  return new RedirectView("home.shtml?teams=my");
	  }

}
