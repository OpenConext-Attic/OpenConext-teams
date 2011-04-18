package nl.surfnet.coin.teams.control;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Locale;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.opensocial.RequestException;
import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.shared.service.MailService;
import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.InvitationForm;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.ShindigActivityService;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.service.impl.InvitationFormValidator;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import nl.surfnet.coin.teams.util.ViewUtil;

/**
 * @author steinwelberg
 *         <p/>
 *         {@link Controller} that handles the add member page of a logged in
 *         user.
 */
@Controller
@SessionAttributes("invitationForm")
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
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
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
    InvitationForm form = new InvitationForm();
    form.setTeamId(team.getId());
    form.setInviter(person);

    Locale locale = localeResolver.resolveLocale(request);
    form.setMessage(messageSource.getMessage("jsp.addmember.Message", new Object[]{}, locale));
    modelMap.addAttribute("invitationForm", form);
    ViewUtil.addViewToModelMap(request, modelMap);

    return "addmember";
  }

  /**
   * In case someone clicks the cancel button
   *
   * @param form    {@link InvitationForm}
   * @param request {@link HttpServletRequest}
   * @return {@link RedirectView} to detail page of the team
   * @throws UnsupportedEncodingException if {@link #UTF_8} is not supported
   */
  @RequestMapping(value = "/doaddmember.shtml", method = RequestMethod.POST, params = "cancelAddMember")
  public RedirectView cancelAddMembers(@ModelAttribute("invitationForm") InvitationForm form,
                                       HttpServletRequest request)
          throws UnsupportedEncodingException {
    return new RedirectView("detailteam.shtml?team="
            + URLEncoder.encode(form.getTeamId(), UTF_8) + "&view="
            + ViewUtil.getView(request));
  }

  @RequestMapping(value = "/doaddmember.shtml", method = RequestMethod.POST)
  public String addMembersToTeam(ModelMap modelMap,
                                 @ModelAttribute("invitationForm") InvitationForm form,
                                 BindingResult result,
                                 HttpServletRequest request)
          throws RequestException, IOException {
    Validator validator = new InvitationFormValidator();
    validator.validate(form, result);

    modelMap.addAttribute(TEAM_PARAM, teamService.findTeamById(form.getTeamId()));

    if (result.hasErrors()) {
      return "addmember";
    }

    MultipartFile csvFile = form.getCsvFile();
    String emailString = form.getEmails();
    StringBuilder sb = new StringBuilder();
    boolean appendEmails = StringUtils.hasText(emailString);
    if (csvFile != null && csvFile.getSize() > 0) {
      sb.append(IOUtils.toCharArray(csvFile.getInputStream()));
      if (appendEmails) {
        sb.append(',');
      }
    }
    if (appendEmails) {
      sb.append(emailString);
    }

    // Parse the email addresses to see whether they are valid
    InternetAddress[] emails;
    try {
      emails = InternetAddress.parse(sb.toString());
    } catch (AddressException e) {
      result.addError(new FieldError("invitationForm", "emails", "error.wrongFormattedEmailList"));
      return "addmember";
    }

    Locale locale = localeResolver.resolveLocale(request);
    doInviteMembers(emails, form, locale);


    return "redirect:/detailteam.shtml?team="
            + URLEncoder.encode(form.getTeamId(), UTF_8) + "&view="
            + ViewUtil.getView(request);
  }

  private void doInviteMembers(final InternetAddress[] emails,
                               final InvitationForm form,
                               final Locale locale)
          throws RequestException, IOException {
    // Send the invitation
    String teamId = form.getTeamId();
    Team team = teamService.findTeamById(teamId);
    String message = form.getMessage();
    String inviterPersonId = form.getInviter().getId();

    Member inviter = teamService.findMember(team, inviterPersonId);
    message = formatMessage(message, inviter, team);
    String teamName = team.getName();
    Object[] messageValuesSubject = {teamName};
    String subject = messageSource.getMessage(INVITE_SEND_INVITE_SUBJECT,
            messageValuesSubject, locale);

    // Add an activity for every member that has been invited to the team.
    for (InternetAddress email : emails) {
      String emailAddress = email.getAddress();
      if (teamInviteService.alreadyInvited(emailAddress, team)) {
        continue;
      }
      Invitation invitation = new Invitation(emailAddress, teamId,
              inviter.getId());
      teamInviteService.saveOrUpdate(invitation);
      sendInvitationByMail(message, locale, subject, invitation);
      addActivity(teamId, teamName, emailAddress, inviterPersonId,
              locale);
    }
  }

  private String formatMessage(final String message, final Member member,
                               final Team team) {
    // First replace the {inviter name} and {team name} with {0} and {1}
    // respectively
    // Footer is added later (is specific per invitee)
    String messageBody = StringUtils.replace(message, INVITER_NAME, "{0}");
    messageBody = StringUtils.replace(messageBody, TEAM_NAME, "{1}");
    MessageFormat formatter = new MessageFormat(messageBody);

    Object[] messageValuesMessage = {member.getName(), team.getName()};

    messageBody = formatter.format(messageValuesMessage);
    return messageBody;
  }

  private void sendInvitationByMail(final String message, final Locale locale,
                                    final String subject, final Invitation invitation) {
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


  private void addActivity(final String teamId, final String teamName,
                           final String email, final String personId,
                           final Locale locale)
          throws RequestException, IOException {
    Object[] messageValues = {email, teamName};

    String title = messageSource.getMessage(ACTIVITY_NEW_MEMBER_TITLE,
            messageValues, locale);
    String body = messageSource.getMessage(ACTIVITY_NEW_MEMBER_BODY,
            messageValues, locale);

    // Add the activity
    shindigActivityService.addActivity(personId, teamId, title, body);
  }

}
