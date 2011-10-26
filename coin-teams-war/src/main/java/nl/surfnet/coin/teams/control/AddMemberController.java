/*
 * Copyright 2011 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.coin.teams.control;

import nl.surfnet.coin.shared.service.MailService;
import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.InvitationForm;
import nl.surfnet.coin.teams.domain.InvitationMessage;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.ShindigActivityService;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.service.impl.InvitationFormValidator;
import nl.surfnet.coin.teams.service.impl.InvitationValidator;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import nl.surfnet.coin.teams.util.TokenUtil;
import nl.surfnet.coin.teams.util.ViewUtil;
import org.apache.commons.io.IOUtils;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Locale;

/**
 * @author steinwelberg
 *         <p/>
 *         {@link Controller} that handles the add member page of a logged in
 *         user.
 */
@Controller
@SessionAttributes({"invitationForm", "invitation", TokenUtil.TOKENCHECK})
public class AddMemberController {

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

  @Autowired
  private ControllerUtil controllerUtil;

  /**
   * Shows form to invite others to your {@link Team}
   *
   * @param modelMap {@link ModelMap}
   * @param request  {@link HttpServletRequest}
   * @return name of the add member form
   */
  @RequestMapping("/addmember.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    Team team = controllerUtil.getTeam(request);

    if (!controllerUtil.hasUserAdministrativePrivileges(person, team.getId())) {
      throw new RuntimeException("Requester (" + person.getId() + ") is not member or does not have the correct " +
              "privileges to add (a) member(s)");
    }

    modelMap.addAttribute(TokenUtil.TOKENCHECK, TokenUtil.generateSessionToken());
    modelMap.addAttribute(TEAM_PARAM, team);
    InvitationForm form = new InvitationForm();
    form.setTeamId(team.getId());
    form.setInviter(person);

    Locale locale = localeResolver.resolveLocale(request);
    Object[] messageParams = {person.getDisplayName(), team.getName()};
    form.setMessage(messageSource.getMessage("jsp.addmember.Message", messageParams, locale));
    modelMap.addAttribute("invitationForm", form);
    ViewUtil.addViewToModelMap(request, modelMap);

    return "addmember";
  }

  /**
   * Shows form to invite others to your {@link Team} coming from a vo specific link
   *
   * @param modelMap {@link ModelMap}
   * @param request  {@link HttpServletRequest}
   * @return name of the add member form
   */
  @RequestMapping("/vo/{voName}/addmember.shtml")
  public String startVO(@PathVariable String voName, ModelMap modelMap, HttpServletRequest request) {
    return start(modelMap, request);
  }

  /**
   * In case someone clicks the cancel button
   *
   * @param form    {@link InvitationForm}
   * @param request {@link HttpServletRequest}
   * @return {@link RedirectView} to detail page of the team
   * @throws UnsupportedEncodingException if {@link #UTF_8} is not supported
   */
  @RequestMapping(value = "/doaddmember.shtml", method = RequestMethod.POST,
          params = "cancelAddMember")
  public RedirectView cancelAddMembers(@ModelAttribute("invitationForm") InvitationForm form,
                                       HttpServletRequest request,
                                       SessionStatus status)
          throws UnsupportedEncodingException {
    status.setComplete();
    return new RedirectView("detailteam.shtml?team="
            + URLEncoder.encode(form.getTeamId(), UTF_8) + "&view="
            + ViewUtil.getView(request), false, true, false);
  }

  /**
   * Called after submitting the add members form
   *
   *
   *
   * @param form     {@link nl.surfnet.coin.teams.domain.InvitationForm} from the session
   * @param result   {@link org.springframework.validation.BindingResult}
   * @param request  {@link javax.servlet.http.HttpServletRequest}
   * @param modelMap {@link org.springframework.ui.ModelMap}
   * @return the name of the form if something is wrong
   *         before handling the invitation,
   *         otherwise a redirect to the detailteam url
   * @throws IOException if something goes wrong handling the invitation
   */
  @RequestMapping(value = "/doaddmember.shtml", method = RequestMethod.POST)
  public String addMembersToTeam(@ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                 @ModelAttribute("invitationForm") InvitationForm form,
                                 BindingResult result, HttpServletRequest request,
                                 @RequestParam() String token, SessionStatus status,
                                 ModelMap modelMap)
          throws IOException {
    TokenUtil.checkTokens(sessionToken, token, status);
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    if (!controllerUtil.hasUserAdministrativePrivileges(person, request.getParameter(TEAM_PARAM))) {
      status.setComplete();
      throw new RuntimeException("Requester (" + person.getId() + ") is not member or does not have the correct " +
              "privileges to add (a) member(s)");
    }

    Validator validator = new InvitationFormValidator();
    validator.validate(form, result);

    if (result.hasErrors()) {
      modelMap.addAttribute(TEAM_PARAM, controllerUtil.getTeamById(form.getTeamId()));
      return "addmember";
    }

    // Parse the email addresses to see whether they are valid
    InternetAddress[] emails;
    try {
      emails = InternetAddress.parse(getAllEmailAddresses(form));
    } catch (AddressException e) {
      result.addError(new FieldError("invitationForm", "emails", "error.wrongFormattedEmailList"));
      return "addmember";
    }

    Locale locale = localeResolver.resolveLocale(request);
    doInviteMembers(emails, form, locale);

    status.setComplete();
    modelMap.clear();
    return "redirect:detailteam.shtml?team="
            + URLEncoder.encode(form.getTeamId(), UTF_8) + "&view="
            + ViewUtil.getView(request);
  }

  /**
   * Called after submitting the add members form
   *
   *
   *
   *
   * @param form     {@link nl.surfnet.coin.teams.domain.InvitationForm} from the session
   * @param result   {@link org.springframework.validation.BindingResult}
   * @param request  {@link javax.servlet.http.HttpServletRequest}
   * @param modelMap {@link org.springframework.ui.ModelMap}
   * @return the name of the form if something is wrong
   *         before handling the invitation,
   *         otherwise a redirect to the detailteam url
   * @throws IOException if something goes wrong handling the invitation
   */
  @RequestMapping(value = "/vo/{voName}/doaddmember.shtml", method = RequestMethod.POST)
  public String addMembersToTeamVO(@ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                   @ModelAttribute("invitationForm") InvitationForm form,
                                   BindingResult result,
                                   HttpServletRequest request,
                                   @RequestParam() String token,
                                   SessionStatus status,
                                   ModelMap modelMap)
          throws IOException {
    return addMembersToTeam(sessionToken, form, result, request, token, status, modelMap);
  }

  @RequestMapping("/doResendInvitation.shtml")
  public String doResendInvitation(ModelMap modelMap,
                                   @ModelAttribute("invitation") Invitation invitation,
                                   BindingResult result,
                                   HttpServletRequest request)
          throws UnsupportedEncodingException {
    Validator validator = new InvitationValidator();
    validator.validate(invitation, result);
    String messageText = request.getParameter("messageText");
    if (result.hasErrors()) {
      modelMap.addAttribute("messageText", messageText);
      return "resendinvitation";
    }
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);

    if (!controllerUtil.hasUserAdministrativePrivileges(person, invitation.getTeamId())) {
      throw new RuntimeException("Requester (" + person.getId() + ") is not member or does not have the correct " +
              "privileges to resend an invitation");
    }
    InvitationMessage invitationMessage =
            new InvitationMessage(messageText, person.getId());
    invitation.addInvitationMessage(invitationMessage);
    invitation.setTimestamp(new Date().getTime());
    teamInviteService.saveOrUpdate(invitation);

    Locale locale = localeResolver.resolveLocale(request);
    String teamId = invitation.getTeamId();
    Team team = teamService.findTeamById(teamId);
    Object[] messageValuesSubject = {team.getName()};

    String subject = messageSource.getMessage(INVITE_SEND_INVITE_SUBJECT,
            messageValuesSubject, locale);
    sendInvitationByMail(invitation, subject, locale);
    return "redirect:detailteam.shtml?team="
            + URLEncoder.encode(teamId, UTF_8) + "&view="
            + ViewUtil.getView(request);
  }

  @RequestMapping("/vo/{voName}/doResendInvitation.shtml")
  public String doResendInvitationVO(@PathVariable String voName,
                                   ModelMap modelMap,
                                   @ModelAttribute("invitation") Invitation invitation,
                                   BindingResult result,
                                   HttpServletRequest request)
          throws UnsupportedEncodingException {
    return doResendInvitation(modelMap, invitation, result, request);
  }

  /**
   * Combines the input of the emails field and the csv file
   *
   * @param form {@link InvitationForm}
   * @return String with the emails
   * @throws IOException if the CSV file cannot be read
   */
  private String getAllEmailAddresses(InvitationForm form) throws IOException {
    StringBuilder sb = new StringBuilder();

    MultipartFile csvFile = form.getCsvFile();
    String emailString = form.getEmails();
    boolean appendEmails = StringUtils.hasText(emailString);
    if (form.hasCsvFile()) {
      sb.append(IOUtils.toCharArray(csvFile.getInputStream()));
      if (appendEmails) {
        sb.append(',');
      }
    }
    if (appendEmails) {
      sb.append(emailString);
    }

    return sb.toString();
  }

  private void doInviteMembers(final InternetAddress[] emails,
                               final InvitationForm form,
                               final Locale locale)
          throws IOException {
    // Send the invitation
    String teamId = form.getTeamId();
    Team team = controllerUtil.getTeamById(teamId);
    String inviterPersonId = form.getInviter().getId();

    Object[] messageValuesSubject = {team.getName()};
    String subject = messageSource.getMessage(INVITE_SEND_INVITE_SUBJECT,
            messageValuesSubject, locale);

    // Add an activity for every member that has been invited to the team.
    for (InternetAddress email : emails) {
      String emailAddress = email.getAddress();

      Invitation invitation = teamInviteService.findInvitation(emailAddress, team);
      boolean newInvitation = invitation == null;

      if (newInvitation) {
        invitation = new Invitation(emailAddress, teamId
        );
      } else if (invitation.isDeclined()) {
        continue;
      }
      InvitationMessage invitationMessage = new InvitationMessage(form.getMessage(), inviterPersonId);
      invitation.addInvitationMessage(invitationMessage);
      invitation.setTimestamp(new Date().getTime());
      teamInviteService.saveOrUpdate(invitation);
      sendInvitationByMail(invitation, subject, locale);

      if (newInvitation) {
        addActivity(team, emailAddress, inviterPersonId,
                locale);
      }
    }
  }

  /**
   * Sends an email based on the {@link Invitation}
   *
   * @param invitation {@link Invitation} that contains the necessary data
   * @param subject    of the email
   * @param locale     {@link Locale}
   */
  private void sendInvitationByMail(final Invitation invitation,
                                    final String subject, final Locale locale) {
    Object[] messageValuesFooter = {environment.getTeamsURL(),
            invitation.getInvitationHash()};
    String footer = messageSource.getMessage(
            "invite.MessageFooter", messageValuesFooter, locale);

    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(environment.getSystemEmail());
    mailMessage.setTo(invitation.getEmail());
    mailMessage.setSubject(subject);

    StringBuffer sb = new StringBuffer();
    InvitationMessage latestInvitationMessage = invitation.getLatestInvitationMessage();
    if (latestInvitationMessage != null) {
      sb.append(latestInvitationMessage.getMessage());
    }
    sb.append(footer);
    mailMessage.setText(sb.toString());

    mailService.sendAsync(mailMessage);
  }


  private void addActivity(final Team team,
                           final String email, final String personId,
                           final Locale locale)
          throws IOException {
    Object[] messageValues = {email, team.getName()};

    String title = messageSource.getMessage(ACTIVITY_NEW_MEMBER_TITLE,
            messageValues, locale);
    String body = messageSource.getMessage(ACTIVITY_NEW_MEMBER_BODY,
            messageValues, locale);

    // Add the activity
    shindigActivityService.addActivity(personId, team.getId(), title, body);
  }
}
