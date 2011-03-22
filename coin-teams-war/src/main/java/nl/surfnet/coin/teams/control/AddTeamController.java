package nl.surfnet.coin.teams.control;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.ShindigActivityService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.DuplicateTeamException;

import org.opensocial.RequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author steinwelberg
 * 
 *         {@link Controller} that handles the add team page of a logged in
 *         user.
 */
@Controller
public class AddTeamController {

  private static final String ACTIVITY_NEW_TEAM_BODY = "activity.NewTeamBody";

  private static final String ACTIVITY_NEW_TEAM_TITLE = "activity.NewTeamTitle";

  @Autowired
  private TeamService teamService;

  @Autowired
  private ShindigActivityService shindigActivityService;

  @Autowired
  private MessageSource messageSource;
  
  @Autowired 
  private LocaleResolver localeResolver;

  @RequestMapping("/addteam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    return "addteam";
  }

  @RequestMapping(value = "/doaddteam.shtml", method = RequestMethod.POST)
  public RedirectView addTeam(ModelMap modelMap, HttpServletRequest request)
      throws RequestException, IOException, DuplicateTeamException {

    String personId = (String) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String teamName = request.getParameter("team");
    String teamDescription = request.getParameter("description");

    // If viewablilityStatus is set this means that the team should be private
    boolean viewable = !StringUtils.hasText(request
        .getParameter("viewabilityStatus"));

    // Form not completely filled in.
    if (!StringUtils.hasText(teamName)) {
      throw new RuntimeException("Parameter error.");
    }

    // Add the team
    String teamId = "";
    teamId = teamService.addTeam(teamName, teamName, teamDescription);    

    // Set the visibility of the group
    teamService.setVisibilityGroup(teamId, viewable);

    // Add the person who has added the team as admin to the team.
    teamService.addMember(teamId, personId);

    // Give him the right permissions, add as the super user
    teamService.addMemberRole(teamId, personId, Role.Admin, true);

    // Add the activity to the COIN portal
    addActivity(teamId, teamName, personId, localeResolver.resolveLocale(request));

    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8"));
  }

  private void addActivity(String teamId, String teamName, String personId, Locale locale)
      throws RequestException, IOException {
    Object[] messageValues = { teamName };

    String title = messageSource.getMessage(ACTIVITY_NEW_TEAM_TITLE, messageValues, locale);
    String body = messageSource.getMessage(ACTIVITY_NEW_TEAM_BODY, messageValues, locale);

    // Add the activity
    //shindigActivityService.addActivity(personId, teamId, title, body);
  }
}
