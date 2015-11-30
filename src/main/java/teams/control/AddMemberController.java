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
import java.util.Locale;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.google.common.base.Throwables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.view.RedirectView;

import teams.domain.Invitation;
import teams.domain.InvitationMessage;
import teams.domain.Language;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Team;
import teams.interceptor.LoginInterceptor;
import teams.service.TeamInviteService;
import teams.util.AuditLog;
import teams.util.ControllerUtil;
import teams.util.TokenUtil;
import teams.util.ViewUtil;

/**
 * {@link Controller} that handles the add member page of a logged in user.
 */
@Controller
@SessionAttributes(TokenUtil.TOKENCHECK)
public class AddMemberController {
  protected static final String INVITE_SEND_INVITE_SUBJECT = "invite.SendInviteSubject";

  private static final String ROLES_PARAM = "roles";

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private ControllerUtil controllerUtil;

  /**
   * Shows form to invite others to your {@link Team}
   */
  @RequestMapping(value = "/addmember.shtml", method = GET)
  public String addMembersToTeam(Model model, Locale locale, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    Team team = controllerUtil.getTeam(request);

    checkUserHasAdministrativePrivileges(person, team.getId());

    InvitationForm form = new InvitationForm();
    form.setTeamId(team.getId());
    form.setLanguage(Language.find(locale).orElse(Language.English));

    model.addAttribute(TOKENCHECK, TokenUtil.generateSessionToken());
    model.addAttribute("invitationForm", form);
    model.addAttribute(ROLES_PARAM, newMemberRoles(person, team.getId()));

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
   */
  @RequestMapping(value = "/doaddmember.shtml", method = POST, params = "cancelAddMember")
  public RedirectView cancelAddMembers(@ModelAttribute("invitationForm") InvitationForm form,
                                       HttpServletRequest request,
                                       SessionStatus status) {
    status.setComplete();

    return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s&view=%s", form.getTeamId(), ViewUtil.getView(request)), false, true, false);
  }

  @RequestMapping(value = "/doaddmember.shtml", method = POST)
  public String doAddMembersToTeam(@ModelAttribute(TOKENCHECK) String sessionToken, @RequestParam String token,
                                 @ModelAttribute("invitationForm") InvitationForm form, BindingResult result,
                                 HttpServletRequest request, SessionStatus status,
                                 Model model) throws IOException {

    Person person = (Person) request.getSession().getAttribute(PERSON_SESSION_KEY);

    checkTokens(sessionToken, token, status);
    checkIfUserIsAdminOrManager(person, form.getTeamId(), status);
    correctRoleIfNeeded(person, form, form.getTeamId());

    new InvitationFormValidator().validate(form, result);

    InternetAddress[] emails = null;
    try {
      emails = getAllEmailAddresses(form);
    } catch (AddressException e) {
      result.rejectValue("emails", "error.WrongFormattedEmailList");
    }

    if (result.hasErrors()) {
      model.addAttribute(ROLES_PARAM, newMemberRoles(person, form.getTeamId()));

      return "addmember";
    }

    doInviteMembers(person, emails, form);

    AuditLog.log("User {} sent invitations for team {}, with role {} to addresses: {}", person.getId(), form.getTeamId(), form.getIntendedRole(), emails);

    status.setComplete();

    return escapeViewParameters("redirect:detailteam.shtml?team=%s&view=%s", form.getTeamId(), ViewUtil.getView(request));
  }

  // if a non admin tries to add a role admin or manager -> make invitation for member
  private void correctRoleIfNeeded(Person person, InvitationForm form, String teamId) {
    boolean isAdmin = controllerUtil.hasUserAdminPrivileges(person, teamId);
    if (!(isAdmin || Role.Member.equals(form.getIntendedRole()))) {
      form.setIntendedRole(Role.Member);
    }
  }

  @RequestMapping(value = "/resendInvitation.shtml", method = GET)
  public String resendInvitation(@RequestParam("id") String invitationId, Model model, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(PERSON_SESSION_KEY);

    Invitation invitation = teamInviteService.findInvitationByInviteId(invitationId)
        .orElseThrow(() -> new IllegalArgumentException("Cannot find the invitation. Invitations expire after 14 days."));

    checkIfUserIsAdminOrManager(person, invitation.getTeamId());

    ResendInvitationCommand command = new ResendInvitationCommand(invitation);
    invitation.getLatestInvitationMessage().ifPresent(msg -> command.setMessageText(msg.getMessage()));

    model.addAttribute("resendInvitationCommand", command);
    model.addAttribute(ROLES_PARAM, new Role[] {Role.Member, Role.Manager, Role.Admin});

    return "resendinvitation";
  }

  @RequestMapping(value = "/doResendInvitation.shtml", method = RequestMethod.POST)
  public String doResendInvitation(Model model,
                                   @Valid @ModelAttribute ResendInvitationCommand command, BindingResult result,
                                   @ModelAttribute(TOKENCHECK) String sessionToken, @RequestParam String token,
                                   HttpServletRequest request, SessionStatus status) {
    Person person = (Person) request.getSession().getAttribute(PERSON_SESSION_KEY);

    checkTokens(sessionToken, token, status);
    checkUserHasAdministrativePrivileges(person, command.getTeamId(), status);

    if (result.hasErrors()) {
      model.addAttribute(ROLES_PARAM, new Role[] {Role.Member, Role.Manager, Role.Admin});
      return "resendinvitation";
    }

    InvitationMessage invitationMessage = new InvitationMessage(command.getMessageText(), person.getId());

    Invitation invitation = teamInviteService.findInvitationByInviteId(command.getInvitationId())
        .orElseThrow(() -> new IllegalArgumentException("Cannot find the invitation. Invitations expire after 14 days."));

    command.apply(invitation);
    invitation.addInvitationMessage(invitationMessage);
    invitation.setTimestamp(new Date().getTime());
    teamInviteService.saveOrUpdate(invitation);

    Team team = controllerUtil.getTeamById(invitation.getTeamId());

    String subject = messageSource.getMessage(INVITE_SEND_INVITE_SUBJECT, new Object[] {team.getName()}, invitation.getLanguage().locale());
    controllerUtil.sendInvitationMail(invitation, subject, person);

    status.setComplete();

    return escapeViewParameters("redirect:detailteam.shtml?team=%s&view=%s", invitation.getTeamId(), ViewUtil.getView(request));
  }

  /**
   * Combines the input of the emails field and the csv file
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

  private void doInviteMembers(Person inviter, InternetAddress[] emails, InvitationForm form) {
    Team team = controllerUtil.getTeamById(form.getTeamId());

    String subject = messageSource.getMessage(INVITE_SEND_INVITE_SUBJECT, new Object[] {team.getName()}, form.getLanguage().locale());

    for (InternetAddress email : emails) {
      String emailAddress = email.getAddress();

      Invitation invitation = teamInviteService.findOpenInvitation(emailAddress, team)
          .orElse(new Invitation(emailAddress, team.getId()));

      if (invitation.isDeclined()) {
        continue;
      }

      InvitationMessage invitationMessage = new InvitationMessage(form.getMessage(), inviter.getId());
      invitation.addInvitationMessage(invitationMessage);
      invitation.setTimestamp(new Date().getTime());
      invitation.setIntendedRole(form.getIntendedRole());
      invitation.setLanguage(form.getLanguage());

      teamInviteService.saveOrUpdate(invitation);

      controllerUtil.sendInvitationMail(invitation, subject, inviter);

      AuditLog.log("Sent invitation and saved to database: team: {}, inviter: {}, email: {}, role: {}, hash: {}",
        team.getId(), inviter.getId(), emailAddress, form.getIntendedRole(), invitation.getInvitationHash());
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

  private void checkUserHasAdministrativePrivileges(Person person, String teamId) {
    if (!controllerUtil.hasUserAdministrativePrivileges(person, teamId)) {
      throw new RuntimeException(String.format(
          "Requester (%s) is not member or does not have the correct privileges to add (a) member(s)", person.getId()));
    }
  }

  private void checkUserHasAdministrativePrivileges(Person person, String teamId, SessionStatus status) {
    try {
      checkUserHasAdministrativePrivileges(person, teamId);
    } catch (Exception e) {
      status.setComplete();
      Throwables.propagate(e);
    }
  }

  @ModelAttribute("languages")
  public Language[] languages() {
    return Language.values();
  }

  @ModelAttribute(ViewUtil.VIEW)
  public String view(HttpServletRequest request) {
    return ViewUtil.getView(request);
  }
}
