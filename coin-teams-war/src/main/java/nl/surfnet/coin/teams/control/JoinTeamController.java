package nl.surfnet.coin.teams.control;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.service.TeamsAPIService;

import org.apache.http.client.ClientProtocolException;
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
 *         {@link Controller} that handles the join team page of a logged in
 *         user.
 */
@Controller
public class JoinTeamController {

  private static final String REQUEST_MEMBERSHIP_SUBJECT = "request.MembershipSubject";

  private static final String TEAM_NAME = "{team name}";
  
  private static final String USER_NAME = "{user}";

  @Autowired
  private TeamService teamService;

  @Autowired
  private TeamsAPIService teamsAPIService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LocaleResolver localeResolver;

  @RequestMapping("/jointeam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    String teamId = request.getParameter("team");
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }

    if (team != null) {
      modelMap.addAttribute("team", team);
    } else {
      // Team does not exist
      throw new RuntimeException("Parameter error.");
    }

    return "jointeam";
  }

  @RequestMapping(value = "/dojointeam.shtml", method = RequestMethod.POST)
  public RedirectView joinTeam(ModelMap modelMap, HttpServletRequest request)
      throws IllegalStateException, ClientProtocolException, IOException {

    String teamId = request.getParameter("team");
    String message = request.getParameter("message");
    String personId = (String) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }

    if (team == null) {
      throw new RuntimeException("Parameter error");
    }

    // TODO use SURFteams API
    sendJoinTeamMessage(team, personId, message,
        localeResolver.resolveLocale(request));

    return new RedirectView("home.shtml?teams=my");
  }

  private void sendJoinTeamMessage(Team team, String personId, String message,
      Locale locale) throws IllegalStateException, ClientProtocolException,
      IOException {
    // First replace the {user} with {0} {team name} with {1}
    Object[] messageValues = { personId, team.getName() };

    if (message != null) {
      message = StringUtils.replace(message, USER_NAME, "{0}");
      message = StringUtils.replace(message, TEAM_NAME, "{1}");
      MessageFormat formatter = new MessageFormat(message);

      message = formatter.format(messageValues);
    }
    String subject = messageSource.getMessage(REQUEST_MEMBERSHIP_SUBJECT,
        messageValues, locale);

    teamsAPIService.requestMembership(team.getId(), personId, message,
        subject);
  }

}
