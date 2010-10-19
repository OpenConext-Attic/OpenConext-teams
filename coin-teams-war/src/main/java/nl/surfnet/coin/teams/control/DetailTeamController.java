package nl.surfnet.coin.teams.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class DetailTeamController {

	  @RequestMapping("/detailteam.shtml")
	  public String start(ModelMap modelMap, HttpServletRequest request) {
		  
		  int role = new Integer(request.getParameter("role"));
		  		
		  if (role == 0) {
			  return "detailteam-not-member";
		  } else if (role == 1) {
			  return "detailteam-member";
		  } else if (role == 2) {
			  return "detailteam-manager";
		  } else if (role == 3) {
			  return "detailteam-admin";
		  } else {
			  
		  }
		  return "home";
	  }
}
