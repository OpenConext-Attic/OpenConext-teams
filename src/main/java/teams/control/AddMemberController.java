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
package teams.control;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;
import static teams.util.TokenUtil.TOKENCHECK;
import static teams.util.TokenUtil.checkTokens;
import static teams.util.ViewUtil.escapeViewParameters;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Throwables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import teams.domain.Invitation;
import teams.domain.InvitationForm;
import teams.domain.InvitationMessage;
import teams.domain.Language;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Team;
import teams.interceptor.LoginInterceptor;
import teams.service.GrouperTeamService;
import teams.service.TeamInviteService;
import teams.service.impl.InvitationFormValidator;
import teams.service.impl.InvitationValidator;
import teams.service.mail.MailService;
import teams.util.AuditLog;
import teams.util.ControllerUtil;
import teams.util.TokenUtil;
import teams.util.ViewUtil;

/**
 * {@link Controller} that handles the add member page of a logged in user.
 */
@Controller
@SessionAttributes({"invitationForm", "invitation", TokenUtil.TOKENCHECK})
public class AddMemberController {
  protected static final String INVITE_SEND_INVITE_SUBJECT = "invite.SendInviteSubject";

  private static final String TEAM_PARAM = "team";
  private static final String LANGUAGES_PARAM = "languages";
  private static final String ROLES_PARAM = "roles";
  private static final String INVITATION_PARAM = "invitation";

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
  @RequestMapping(value = "/addmember.shtml", method = GET)
  public String addMembersToTeam(ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    Team team = controllerUtil.getTeam(request);

    if (!controllerUtil.hasUserAdministrativePrivileges(person, team.getId())) {
      throw new RuntimeException(String.format(
          "Requester (%s) is not member or does not have the correct privileges to add (a) member(s)", person.getId()));
    }

    InvitationForm form = new InvitationForm();
    form.setTeamId(team.getId());
    form.setInviter(person);
    form.setLanguage(Language.find(localeResolver.resolveLocale(request)).orElse(Language.English));

    modelMap.addAttribute(TOKENCHECK, TokenUtil.generateSessionToken());
    modelMap.addAttribute(TEAM_PARAM, team);
    modelMap.addAttribute("invitationForm", form);
    modelMap.addAttribute(ROLES_PARAM, newMemberRoles(person, team.getId()));
    modelMap.addAttribute(LANGUAGES_PARAM, Language.values());

    ViewUtil.addViewToModelMap(request, modelMap);

    return "addmember";
  }

  private Role[] newMemberRoles(Person person, String teamId) {
    if (controllerUtil.hasUserAdminPrivileges(person, teamId)) {
      return new Role[]{ Role.Admin, Role.Manager, Role.Member };
    } else if (controllerUtil.hasUserAdministrativePrivileges(person, teamId)) {
      return new Role[]{ Role.Member };
    }

    throw new RuntimeException(String.format("User %s has not enough privileges to invite others in team %s", person.getId(), teamId));
  }

  /**
   * In case someone clicks the cancel button
   *
   * @param form    {@link InvitationForm}
   * @param request {@link HttpServletRequest}
   * @return {@link RedirectView} to detail page of the team
   */
  @RequestMapping(value = "/doaddmember.shtml", method = POST, params = "cancelAddMember")
  public RedirectView cancelAddMembers(@ModelAttribute("invitationForm") InvitationForm form,
                                       HttpServletRequest request,
                                       SessionStatus status) {
    status.setComplete();

    return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s&view=%s", form.getTeamId(), ViewUtil.getView(request)), false, true, false);
  }

  @RequestMapping(value = "/doaddmember.shtml", method = POST)
  public String doAddMembersToTeam(@ModelAttribute(TOKENCHECK) String sessionToken,
                                 @ModelAttribute("invitationForm") InvitationForm form,
                                 BindingResult result, HttpServletRequest request,
                                 @RequestParam String token, SessionStatus status,
                                 ModelMap modelMap) throws IOException {

    Person person = (Person) request.getSession().getAttribute(PERSON_SESSION_KEY);
    String teamId = request.getParameter(TEAM_PARAM);

    checkTokens(sessionToken, token, status);
    checkIfUserIsAdminOrManager(person, teamId, status);
    correctRoleIfNeeded(person, form, teamId);

    new InvitationFormValidator().validate(form, result);

    InternetAddress[] emails = null;
    try {
      emails = getAllEmailAddresses(form);
    } catch (AddressException e) {
      result.rejectValue("emails", "error.WrongFormattedEmailList");
    }

    if (result.hasErrors()) {
      modelMap.addAttribute(ROLES_PARAM, newMemberRoles(person, form.getTeamId()));
      modelMap.addAttribute(LANGUAGES_PARAM, Language.values());
      modelMap.addAttribute(TEAM_PARAM, controllerUtil.getTeamById(form.getTeamId()));

      return "addmember";
    }

    doInviteMembers(emails, form);

    AuditLog.log("User {} sent invitations for team {}, with role {} to addresses: {}", person.getId(), form.getTeamId(), form.getIntendedRole(), emails);

    status.setComplete();
    modelMap.clear();

    return escapeViewParameters("redirect:detailteam.shtml?team=%s&view=%s", form.getTeamId(), ViewUtil.getView(request));
  }

  // if a non admin tries to add a role admin or manager -> make invitation for member
  private void correctRoleIfNeeded(Person person, InvitationForm form, String teamId) {
    boolean isAdmin = controllerUtil.hasUserAdminPrivileges(person, teamId);
    if (!(isAdmin || Role.Member.equals(form.getIntendedRole()))) {
      form.setIntendedRole(Role.Member);
    }
  }

  private void checkIfUserIsAdminOrManager(Person person, String teamId, SessionStatus status) {
    try {
      checkIfUserIsAdminOrManager(person, teamId);
    } catch (Exception e) {
      status.setComplete();
      Throwables.propagate(e);
    }
  }

  private void checkIfUserIsAdminOrManager(Person person, String teamId) {
    if (controllerUtil.hasUserAdministrativePrivileges(person, teamId)) {
      return;
    }

    throw new RuntimeException(
        String.format("Requester (%s) is not member or does not have the correct privileges to add (a) member(s)", person.getId()));
  }

  @RequestMapping(value = "/resendInvitation.shtml", method = GET)
  public String resendInvitation(ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(PERSON_SESSION_KEY);

    Invitation invitation = teamInviteService.findAllInvitationById(request.getParameter("id"))
        .orElseThrow(() -> new IllegalArgumentException("Cannot find the invitation. Invitations expire after 14 days."));

    checkIfUserIsAdminOrManager(person, invitation.getTeamId());

    modelMap.addAttribute(INVITATION_PARAM, invitation);
    modelMap.addAttribute(ROLES_PARAM, new Role[] {Role.Member, Role.Manager, Role.Admin});
    modelMap.addAttribute(LANGUAGES_PARAM, Language.values());

    invitation.getLatestInvitationMessage()
        .ifPresent(msg -> modelMap.addAttribute("messageText", msg.getMessage()));

    ViewUtil.addViewToModelMap(request, modelMap);

    return "resendinvitation";
  }

  @RequestMapping(value = "/doResendInvitation.shtml", method = RequestMethod.POST)
  public String doResendInvitation(ModelMap modelMap,
                                   @ModelAttribute("invitation") Invitation invitation,
                                   BindingResult result,
                                   HttpServletRequest request,
                                   @ModelAttribute(TOKENCHECK) String sessionToken,
                                   @RequestParam() String token,
                                   SessionStatus status) {

    checkTokens(sessionToken, token, status);

    new InvitationValidator().validate(invitation, result);

    String messageText = request.getParameter("messageText");

    if (result.hasErrors()) {
      modelMap.addAttribute("messageText", messageText);
      return "resendinvitation";
    }
    Person person = (Person) request.getSession().getAttribute(PERSON_SESSION_KEY);

    if (!controllerUtil.hasUserAdministrativePrivileges(person, invitation.getTeamId())) {
      status.setComplete();
      modelMap.clear();
      throw new RuntimeException(String.format(
          "Requester (%s) is not member or does not have the correct privileges to resend an invitation", person.getId()));
    }

    InvitationMessage invitationMessage = new InvitationMessage(messageText, person.getId());
    invitation.addInvitationMessage(invitationMessage);
    invitation.setTimestamp(new Date().getTime());
    teamInviteService.saveOrUpdate(invitation);

    Team team = controllerUtil.getTeamById(invitation.getTeamId());

    String subject = messageSource.getMessage(INVITE_SEND_INVITE_SUBJECT, new Object[] {team.getName()}, invitation.getLanguage().locale());
    sendInvitationByMail(invitation, subject, person);

    status.setComplete();
    modelMap.clear();

    return escapeViewParameters("redirect:detailteam.shtml?team=%s&view=%s", invitation.getTeamId(), ViewUtil.getView(request));
  }

  /**
   * Combines the input of the emails field and the csv file
   *
   * @param form {@link InvitationForm}
   * @return String with the emails
   * @throws IOException if the CSV file cannot be read
   * @throws AddressException if contains invalid email addresses
   */
  private InternetAddress[] getAllEmailAddresses(InvitationForm form) throws IOException, AddressException {
    StringBuilder sb = new StringBuilder();

    String emailString = form.getEmails();
    boolean appendEmails = StringUtils.hasText(emailString);
    if (form.hasCsvFile()) {
      sb.append(form.getCsvFileEmails());
      if (appendEmails) {
        sb.append(',');
      }
    }
    if (appendEmails) {
      sb.append(emailString);
    }

    return InternetAddress.parse(sb.toString());
  }

  private void doInviteMembers(InternetAddress[] emails, InvitationForm form) {
    Team team = controllerUtil.getTeamById(form.getTeamId());
    String inviterPersonId = form.getInviter().getId();

    String subject = messageSource.getMessage(INVITE_SEND_INVITE_SUBJECT, new Object[] {team.getName()}, form.getLanguage().locale());

    for (InternetAddress email : emails) {
      String emailAddress = email.getAddress();

      Invitation invitation = teamInviteService.findOpenInvitation(emailAddress, team)
          .orElse(new Invitation(emailAddress, team.getId()));

      if (invitation.isDeclined()) {
        continue;
      }

      InvitationMessage invitationMessage = new InvitationMessage(form.getMessage(), inviterPersonId);
      invitation.addInvitationMessage(invitationMessage);
      invitation.setTimestamp(new Date().getTime());
      invitation.setIntendedRole(form.getIntendedRole());
      invitation.setLanguage(form.getLanguage());

      teamInviteService.saveOrUpdate(invitation);

      sendInvitationByMail(invitation, subject, form.getInviter());

      AuditLog.log("Sent invitation and saved to database: team: {}, inviter: {}, email: {}, role: {}, hash: {}",
        team.getId(), inviterPersonId, emailAddress, form.getIntendedRole(), invitation.getInvitationHash());
    }
  }

  protected void sendInvitationByMail(Invitation invitation, String subject, Person inviter) {
    String html = composeInvitationMailMessage(invitation, inviter, "html");
    String plainText = composeInvitationMailMessage(invitation, inviter, "plaintext");

    MimeMessagePreparator preparator = mimeMessage -> {
      mimeMessage.addHeader("Precedence", "bulk");
      mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(invitation.getEmail()));
      mimeMessage.setFrom(new InternetAddress(systemEmail));
      mimeMessage.setSubject(subject);

      MimeMultipart rootMixedMultipart = controllerUtil.getMimeMultipartMessageBody(plainText, html);
      mimeMessage.setContent(rootMixedMultipart);
    };

    mailService.sendAsync(preparator);
  }

  private String composeInvitationMailMessage(Invitation invitation, Person inviter, String variant) {
    String templateName = "plaintext".equals(variant) ? "invitationmail-plaintext.ftl" : "invitationmail.ftl";

    Team team = grouperTeamService.findTeamById(invitation.getTeamId());

    Map<String, Object> templateVars = new HashMap<>();
    templateVars.put("invitation", invitation);
    templateVars.put("inviter", inviter);
    templateVars.put("team", team);
    templateVars.put("teamsURL", teamsUrl);
    templateVars.put("messages", messageSource);
    templateVars.put("locale", invitation.getLanguage().locale());

    try {
      Template template = freemarkerConfiguration.getTemplate(templateName, invitation.getLanguage().locale());
      return FreeMarkerTemplateUtils.processTemplateIntoString(template, templateVars);
    } catch (IOException | TemplateException e) {
      throw new RuntimeException("Failed to create invitation mail", e);
    }
  }
}
