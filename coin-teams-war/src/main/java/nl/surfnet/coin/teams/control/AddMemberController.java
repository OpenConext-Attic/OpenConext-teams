package nl.surfnet.coin.teams.control;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Locale;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import org.opensocial.RequestException;
import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.shared.service.MailService;
import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.ShindigActivityService;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import nl.surfnet.coin.teams.util.ViewUtil;

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

  private static final String UTF_8 = "utf-8";
  private static final String TEAM_PARAM = "team";

  @Autowired
  private TeamService teamService;

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private ShindigActivityService shindigActivityService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private MailService mailService;
  
  @Autowired
  private TeamEnvironment environment;

  @RequestMapping("/addmember.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    String teamId = request.getParameter(TEAM_PARAM);
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }

    if (team != null) {
      modelMap.addAttribute(TEAM_PARAM, team);
    } else {
      // Team does not exist
      throw new RuntimeException("Parameter error.");
    }

    ViewUtil.addViewToModelMap(request, modelMap);

    return "addmember";
  }

  
  @RequestMapping(value = "/doaddmember.shtml", method = RequestMethod.POST)
  public RedirectView addMembersToTeam(ModelMap modelMap, HttpServletRequest request)
      throws RequestException, IOException {

    String teamId = URLDecoder.decode(request.getParameter(TEAM_PARAM), UTF_8);
    String emailString = request.getParameter("memberEmail");
    String message = request.getParameter("message");
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();

    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(emailString)
        || !StringUtils.hasText(message)) {
      throw new IllegalArgumentException("Missing required parameters team, memberEmail or message");
    }

    Team team = teamService.findTeamById(teamId);

    if (team == null) {
      throw new IllegalArgumentException("Received incorrect team value");
    }
    modelMap.addAttribute(TEAM_PARAM, team);

    // Parse the email addresses to see whether they are valid
    InternetAddress[] emails;
    try {
      emails = InternetAddress.parse(emailString);
    } catch (AddressException e) {
      return new RedirectView("addmember.shtml?team="
          + URLEncoder.encode(teamId, UTF_8)
          + "&mes=error.wrongFormattedEmailList&view="
          + ViewUtil.getView(request));
    }

    Locale locale = localeResolver.resolveLocale(request);
    doInviteMembers(emails, team, message, personId, locale);


    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(team.getId(), UTF_8) + "&view="
        + ViewUtil.getView(request));
  }

  private void doInviteMembers(InternetAddress[] emails, Team team,
                               String message, String inviterPersonId,
                               Locale locale)
          throws RequestException, IOException {
    // Send the invitation
    Member inviter = teamService.findMember(team, inviterPersonId);
    message = formatMessage(message, inviter, team);
    Object[] messageValuesSubject = { team.getName() };
    String subject = messageSource.getMessage(INVITE_SEND_INVITE_SUBJECT,
        messageValuesSubject, locale);

    // Add an activity for every member that has been invited to the team.
    for (InternetAddress email : emails) {
      if (teamInviteService.alreadyInvited(email.getAddress(), team)) {
        continue;
      }
      Invitation invitation = new Invitation(email.getAddress(), team.getId(),
              inviter.getId());
      teamInviteService.saveOrUpdate(invitation);
      sendInvitationByMail(message, locale, subject, invitation);
      addActivity(team.getId(), team.getName(), email.getAddress(), inviterPersonId,
              locale);
    }
  }

  private String formatMessage(String message, Member member, Team team) {
    // First replace the {inviter name} and {team name} with {0} and {1}
    // respectively
    // Footer is added later (is specific per invitee)
    message = StringUtils.replace(message, INVITER_NAME, "{0}");
    message = StringUtils.replace(message, TEAM_NAME, "{1}");
    MessageFormat formatter = new MessageFormat(message);

    Object[] messageValuesMessage = { member.getName(), team.getName() };

    message = formatter.format(messageValuesMessage);
    return message;
  }

  private void sendInvitationByMail(String message, Locale locale,
                                    String subject, Invitation invitation) {
    Object[] messageValuesFooter = {environment.getTeamsURL(),
            invitation.getInvitationHash()};
    String footer = messageSource.getMessage(
            "invite.MessageFooter", messageValuesFooter, locale);

    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(environment.getSystemEmail());
    mailMessage.setTo(invitation.getEmail());
    mailMessage.setSubject(subject);
    mailMessage.setText(message + footer);
    mailService.sendAsync(mailMessage);
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
