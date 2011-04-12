package nl.surfnet.coin.teams.control;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.shared.service.MailService;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.JoinTeamRequestService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;

/**
 * {@link Controller} that handles the join team page of a logged in
 * user.
 */
@Controller
public class JoinTeamController {

  private static final String REQUEST_MEMBERSHIP_SUBJECT = "request.MembershipSubject";

  private static final String TEAM_NAME = "{team name}";

  private static final String USER_NAME = "{user}";

  @Autowired
  private TeamService teamService;

  @Autowired
  private JoinTeamRequestService joinTeamRequestService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private MailService mailService;

  @Autowired
  private TeamEnvironment environment;

  @RequestMapping("/jointeam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    String teamId = request.getParameter("team");
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }

    if (team == null) {
      // Team does not exist
      throw new RuntimeException("Cannot find team for parameter 'team'");
    }

    modelMap.addAttribute("team", team);

    return "jointeam";
  }

  @RequestMapping(value = "/dojointeam.shtml", method = RequestMethod.POST)
  public RedirectView joinTeam(ModelMap modelMap, HttpServletRequest request)
    throws IllegalStateException, IOException {

    String teamId = request.getParameter("team");
    String message = request.getParameter("message");
    Person person = (Person) request.getSession().getAttribute(
      LoginInterceptor.PERSON_SESSION_KEY);

    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }

    if (team == null) {
      throw new RuntimeException("Cannot find team for parameter 'team'");
    }

    JoinTeamRequest joinRequest = new JoinTeamRequest();
    joinRequest.setGroupId(team.getId());
    joinRequest.setPersonId(person.getId());
    joinRequest.setTimestamp(new Date().getTime());
    joinTeamRequestService.saveOrUpdate(joinRequest);

    sendJoinTeamMessage(team, person, message,
      localeResolver.resolveLocale(request));

    return new RedirectView("home.shtml?teams=my");
  }

  private void sendJoinTeamMessage(Team team, Person person, String message,
                                   Locale locale) throws IllegalStateException,
    IOException {

    // First replace the {user} with {0} {team name} with {1}
    Object[] messageValues = {person.getDisplayName(), team.getName()};

    if (message == null) {
      message = "";
    }
    message = StringUtils.replace(message, USER_NAME, "{0}");
    message = StringUtils.replace(message, TEAM_NAME, "{1}");
    MessageFormat formatter = new MessageFormat(message);

    message = formatter.format(messageValues);

    Object[] footerValues = {environment.getTeamsURL(), person.getDisplayName(),
      person.getEmail()};
    message += messageSource.getMessage("request.mail.GoToUrlToAccept",
      footerValues, locale);

    String subject = messageSource.getMessage(REQUEST_MEMBERSHIP_SUBJECT,
      messageValues, locale);

    Set<Member> admins = teamService.findAdmins(team);
    if (CollectionUtils.isEmpty(admins)) {
      throw new IllegalStateException("Team '" + team.getName() + "' has no admins to mail invites");
    }
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(environment.getSystemEmail());
    List<String> bcc = new ArrayList<String>();
    for (Member admin : admins) {
      bcc.add(admin.getEmail());
    }
    mailMessage.setBcc(bcc.toArray(new String[bcc.size()]));
    mailMessage.setSubject(subject);
    mailMessage.setText(message);
    mailService.sendAsync(mailMessage);
  }

  public void setTeamService(TeamService teamService) {
    this.teamService = teamService;
  }
}
