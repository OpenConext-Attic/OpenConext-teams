package nl.surfnet.coin.teams.control;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.ShindigActivityService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.service.TeamsAPIService;

import org.apache.http.client.ClientProtocolException;
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
 *         {@link Controller} that handles the add member page of a logged in
 *         user.
 */
@Controller
public class AddMemberController {

  private static final String TEAM_NAME = "{team name}";

  private static final String INVITER_NAME = "{inviter name}";

  private static final String INVITE_SEND_INVITE_SUBJECT = "invite.SendInviteSubject";

  private static final String ACTIVITY_NEW_MEMBER_BODY = "activity.NewMemberBody";

  private static final String ACTIVITY_NEW_MEMBER_TITLE = "activity.NewMemberTitle";

  @Autowired
  private TeamService teamService;

  @Autowired
  private TeamsAPIService teamsAPIService;

  @Autowired
  private ShindigActivityService shindigActivityService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LocaleResolver localeResolver;

  @RequestMapping("/addmember.shtml")
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

    return "addmember";
  }

  @RequestMapping(value = "/doaddmember.shtml", method = RequestMethod.POST)
  public RedirectView addTeam(ModelMap modelMap, HttpServletRequest request)
      throws RequestException, IOException {

    String teamId = URLDecoder.decode(request.getParameter("team"), "utf-8");
    String emailString = request.getParameter("memberEmail");
    String message = request.getParameter("message");
    String personId = (String) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);

    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(emailString)
        || !StringUtils.hasText(message)) {
      throw new RuntimeException("Parameter error.");
    }

    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }

    if (team == null) {
    	throw new RuntimeException("Parameter error.");
    }
    modelMap.addAttribute("team", team);

    // Parse the email addresses to see whether they are valid
    InternetAddress[] emails;
    try {
      emails = InternetAddress.parse(emailString);
    } catch (AddressException e) {
      return new RedirectView("addmember.shtml?team="
          + URLEncoder.encode(teamId, "utf-8")
          + "&mes=error.wrongFormattedEmailList");
    }

    // Send the invitation
    sendInvitations(team, teamService.findMember(team, personId), emailString,
        message, localeResolver.resolveLocale(request));

    // Add an activity for every member that has been invited to the team.
    for (InternetAddress email : emails) {
      addActivity(team.getId(), team.getName(), email.getAddress(), personId,
          localeResolver.resolveLocale(request));

    }

    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(team.getId(), "utf-8"));
  }

  private void sendInvitations(Team team, Member member, String emails,
      String message, Locale locale) throws IllegalStateException,
      ClientProtocolException, IOException {
    // First replace the {inviter name} and {team name} with {0} and {1}
    // respectively
    message = StringUtils.replace(message, INVITER_NAME, "{0}");
    message = StringUtils.replace(message, TEAM_NAME, "{1}");
    MessageFormat formatter = new MessageFormat(message);

    Object[] messageValuesSubject = { team.getName() };
    Object[] messageValuesMessage = { member.getName(), team.getName()  };

    message = formatter.format(messageValuesMessage);
    String subject = messageSource.getMessage(INVITE_SEND_INVITE_SUBJECT,
        messageValuesSubject, locale);

    teamsAPIService.sentInvitations(emails, team.getId(), message, subject);
  }

  private void addActivity(String teamId, String teamName, String email,
      String personId, Locale locale) throws RequestException, IOException {
    Object[] messageValues = { email, teamName };

    String title = messageSource.getMessage(ACTIVITY_NEW_MEMBER_TITLE,
        messageValues, locale);
    String body = messageSource.getMessage(ACTIVITY_NEW_MEMBER_BODY,
        messageValues, locale);

    // Add the activity
    shindigActivityService.addActivity(personId, teamId, title, body);
  }

}
