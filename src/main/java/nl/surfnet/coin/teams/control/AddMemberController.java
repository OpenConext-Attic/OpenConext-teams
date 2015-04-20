/*
 * Copyright 2012 SURFnet bv, The Netherlands
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.InvitationForm;
import nl.surfnet.coin.teams.domain.InvitationMessage;
import nl.surfnet.coin.teams.domain.Person;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.impl.InvitationFormValidator;
import nl.surfnet.coin.teams.service.impl.InvitationValidator;
import nl.surfnet.coin.teams.service.mail.MailService;
import nl.surfnet.coin.teams.util.AuditLog;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.TokenUtil;
import nl.surfnet.coin.teams.util.ViewUtil;

/**
 * {@link Controller} that handles the add member page of a logged in
 * user.
 */
@Controller
@SessionAttributes({"invitationForm", "invitation", TokenUtil.TOKENCHECK})
public class AddMemberController {
  protected static final String INVITE_SEND_INVITE_SUBJECT = "invite.SendInviteSubject";

  private static final String UTF_8 = "utf-8";
  private static final String TEAM_PARAM = "team";

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private MailService mailService;

  @Autowired
  private ControllerUtil controllerUtil;

  @Autowired
  private Configuration freemarkerConfiguration;

  @Value("${teamsURL}")
  private String teamsUrl;

  @Value("${systemEmail}")
  private String systemEmail;

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
    modelMap.addAttribute("invitationForm", form);
    addNewMemberRolesToModelMap(person, team.getId(), modelMap);
    ViewUtil.addViewToModelMap(request, modelMap);

    return "addmember";
  }

  private void addNewMemberRolesToModelMap(Person person, String teamId, ModelMap modelMap) {

    Role[] roles;
    if (controllerUtil.hasUserAdminPrivileges(person, teamId)) {
      roles = new Role[]{Role.Admin, Role.Manager, Role.Member};
    } else if (controllerUtil.hasUserAdministrativePrivileges(person, teamId)) {
      roles = new Role[]{Role.Member};
    } else {
      throw new RuntimeException("User " + person.getId() +
        " has not enough privileges to invite others in team " + teamId);
    }
    modelMap.addAttribute("roles", roles);
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
      + form.getTeamId() + "&view="
      + ViewUtil.getView(request), false, true, false);
  }

  /**
   * Called after submitting the add members form
   *
   * @param form     {@link nl.surfnet.coin.teams.domain.InvitationForm} from the session
   * @param result   {@link org.springframework.validation.BindingResult}
   * @param request  {@link javax.servlet.http.HttpServletRequest}
   * @param modelMap {@link org.springframework.ui.ModelMap}
   * @return the name of the form if something is wrong
   * before handling the invitation,
   * otherwise a redirect to the detailteam url
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
    addNewMemberRolesToModelMap(person, form.getTeamId(), modelMap);

    final boolean isAdminOrManager = controllerUtil.hasUserAdministrativePrivileges(person, request.getParameter(TEAM_PARAM));
    if (!isAdminOrManager) {
      status.setComplete();
      throw new RuntimeException("Requester (" + person.getId() + ") is not member or does not have the correct " +
        "privileges to add (a) member(s)");
    }
    final boolean isAdmin = controllerUtil.hasUserAdminPrivileges(person, request.getParameter(TEAM_PARAM));
    // if a non admin tries to add a role admin or manager -> make invitation for member
    if (!(isAdmin || Role.Member.equals(form.getIntendedRole()))) {
      form.setIntendedRole(Role.Member);
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
    AuditLog.log("User {} sent invitations for team {}, with role {} to addresses: {}", person.getId(), form.getTeamId(), form.getIntendedRole(), emails);

    status.setComplete();
    modelMap.clear();
    return "redirect:detailteam.shtml?team="
      + form.getTeamId() + "&view="
      + ViewUtil.getView(request);
  }

  @RequestMapping(value = "/doResendInvitation.shtml", method = RequestMethod.POST)
  public String doResendInvitation(ModelMap modelMap,
                                   @ModelAttribute("invitation") Invitation invitation,
                                   BindingResult result,
                                   HttpServletRequest request,
                                   @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                   @RequestParam() String token,
                                   SessionStatus status)
    throws UnsupportedEncodingException {
    TokenUtil.checkTokens(sessionToken, token, status);
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
      status.setComplete();
      modelMap.clear();
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
    Team team = grouperTeamService.findTeamById(teamId);
    Object[] messageValuesSubject = {team.getName()};

    String subject = messageSource.getMessage(INVITE_SEND_INVITE_SUBJECT,
      messageValuesSubject, locale);
    sendInvitationByMail(invitation, subject, person, locale);
    status.setComplete();
    modelMap.clear();
    return "redirect:detailteam.shtml?team="
      + teamId + "&view="
      + ViewUtil.getView(request);
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
                               final Locale locale) {
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

      Invitation invitation = teamInviteService.findOpenInvitation(emailAddress, team);
      boolean newInvitation = (invitation == null);

      if (newInvitation) {
        invitation = new Invitation(emailAddress, teamId);
      } else if (invitation.isDeclined()) {
        continue;
      }
      InvitationMessage invitationMessage = new InvitationMessage(form.getMessage(), inviterPersonId);
      invitation.addInvitationMessage(invitationMessage);
      invitation.setTimestamp(new Date().getTime());
      invitation.setIntendedRole(form.getIntendedRole());
      teamInviteService.saveOrUpdate(invitation);
      sendInvitationByMail(invitation, subject, form.getInviter(), locale);

      AuditLog.log("Sent invitation and saved to database: team: {}, inviter: {}, email: {}, role: {}, hash: {}",
        teamId, inviterPersonId, emailAddress, form.getIntendedRole(), invitation.getInvitationHash());
    }
  }

  /**
   * Sends an email based on the {@link Invitation}
   *
   * @param invitation {@link Invitation} that contains the necessary data
   * @param subject    of the email
   * @param inviter    {@link Person} who sends the invitation
   * @param locale     {@link Locale} for the mail
   */
  protected void sendInvitationByMail(final Invitation invitation,
                                      final String subject,
                                      final Person inviter,
                                      final Locale locale) {

    final String html = composeInvitationMailMessage(invitation, inviter, locale, "html");
    final String plainText = composeInvitationMailMessage(invitation, inviter, locale, "plaintext");

    MimeMessagePreparator preparator = new MimeMessagePreparator() {
      public void prepare(MimeMessage mimeMessage) throws MessagingException {
        mimeMessage.addHeader("Precedence", "bulk");
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(invitation.getEmail()));
        mimeMessage.setFrom(new InternetAddress(systemEmail));
        mimeMessage.setSubject(subject);

        MimeMultipart rootMixedMultipart = controllerUtil.getMimeMultipartMessageBody(plainText, html);
        mimeMessage.setContent(rootMixedMultipart);
      }
    };

    mailService.sendAsync(preparator);
  }


  String composeInvitationMailMessage(Invitation invitation, Person inviter, Locale locale, String variant) {
    String templateName;
    if ("plaintext".equals(variant)) {
      templateName = "invitationmail-plaintext.ftl";
    } else {
      templateName = "invitationmail.ftl";
    }
    Map<String, Object> templateVars = new HashMap<>();

    templateVars.put("invitation", invitation);
    templateVars.put("inviter", inviter);
    final Team team = grouperTeamService.findTeamById(invitation.getTeamId());
    templateVars.put("team", team);
    templateVars.put("teamsURL", teamsUrl);

    try {
      return FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerConfiguration.getTemplate(templateName, locale), templateVars
      );
    } catch (IOException | TemplateException e) {
      throw new RuntimeException("Failed to create invitation mail", e);
    }
  }
}
