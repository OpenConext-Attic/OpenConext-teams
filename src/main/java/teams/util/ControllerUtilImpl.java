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
package teams.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import teams.domain.Invitation;
import teams.domain.Member;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Team;
import teams.service.GrouperTeamService;
import teams.service.mail.MailService;

/**
 * This class includes methods that are often used by controllers
 */
@Component("controllerUtil")
public class ControllerUtilImpl implements ControllerUtil {

  private static final Logger log = LoggerFactory.getLogger(ControllerUtilImpl.class);

  private static final String REQUEST_MEMBERSHIP_SUBJECT = "request.MembershipSubject";

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private Configuration freemarkerConfiguration;

  @Autowired
  private MailService mailService;

  @Value("${systemEmail}")
  private String systemEmail;

  @Autowired
  private MessageSource messageSource;

  @Value("${teamsURL}")
  private String teamsUrl;

  /**
   * Get the team from the {@link HttpServletRequest} request.
   *
   * @param request the {@link HttpServletRequest}
   * @return The {@link Team} team
   * @throws RuntimeException if the team cannot be found
   */
  public Team getTeam(HttpServletRequest request) {
    return getTeamById(request.getParameter("team"));
  }

  /**
   * Get the team from the {@link String} teamId.
   *
   * @param teamId the {@link String} teamId
   * @return The {@link Team} team
   * @throws RuntimeException if the team cannot be found
   */
  public Team getTeamById(String teamId) {
    checkArgument(StringUtils.hasText(teamId));

    return grouperTeamService.findTeamById(teamId);
  }

  /**
   * Checks if the current user has administrative privileges (whether he is admin OR manager) for a given team.
   *
   * @param person {@link Person}
   * @param teamId {@link String} the team Id for which the person's privileges are checked
   * @return {@link boolean} <code>true/code> if the user is admin AND/OR manager <code>false</code> if the user isn't
   */
  public boolean hasUserAdministrativePrivileges(Person person, String teamId) {
    // Check if the requester is member of the team AND
    // Check if the requester has the role admin or manager, so he is allowed to invite new members.
    Member member = grouperTeamService.findMember(teamId, person.getId());
    return member != null && (member.getRoles().contains(Role.Admin) || member.getRoles().contains(Role.Manager));
  }

  /**
   * Checks if the current user has admin privileges for a given team.
   *
   * @param person {@link Person}
   * @param teamId {@link String} the team Id for which the person's privileges are checked
   * @return {@link boolean} <code>true/code> if the user is admin AND/OR manager <code>false</code> if the user isn't
   */
  public boolean hasUserAdminPrivileges(Person person, String teamId) {
    // Check if the requester is member of the team AND
    // Check if the requester has the role admin or manager, so he is allowed to invite new members.
    Member member = grouperTeamService.findMember(teamId, person.getId());
    return member != null && (member.getRoles().contains(Role.Admin));
  }

  public boolean isPersonMemberOfTeam(Person person, Team team) {
    return team.getMembers().stream().anyMatch(m -> m.getId().equals(person.getId()));
  }

  private MimeMultipart getMimeMultipartMessageBody(String plainText, String html) throws MessagingException {
    MimeBodyPart textPart = new MimeBodyPart();
    textPart.setText(plainText, "utf-8");

    MimeBodyPart htmlPart = new MimeBodyPart();
    htmlPart.setContent(html, "text/html; charset=utf-8");

    MimeMultipart multiPart = new MimeMultipart("alternative");
    multiPart.addBodyPart(textPart); // least important
    multiPart.addBodyPart(htmlPart); // most important
    return multiPart;
  }

  @Override
  public void sendInvitationMail(Invitation invitation, String subject, Person inviter) {
    String html = composeInvitationMailMessage(invitation, inviter, "html");
    String plainText = composeInvitationMailMessage(invitation, inviter, "plaintext");

    MimeMessagePreparator preparator = mimeMessage -> {
      mimeMessage.addHeader("Precedence", "bulk");
      mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(invitation.getEmail()));
      mimeMessage.setFrom(new InternetAddress(systemEmail));
      mimeMessage.setSubject(subject);

      MimeMultipart rootMixedMultipart = getMimeMultipartMessageBody(plainText, html);
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

  @Override
  public void sendAcceptMail(Person memberToAdd, Team team, Locale locale) {
    String subject = messageSource.getMessage("request.mail.accepted.subject", null, locale);
    String html = composeAcceptMailMessage(team, locale, "html");
    String plainText = composeAcceptMailMessage(team, locale, "plaintext");

    MimeMessagePreparator preparator = mimeMessage -> {
      mimeMessage.addHeader("Precedence", "bulk");

      mimeMessage.setFrom(new InternetAddress(systemEmail));
      mimeMessage.setRecipients(Message.RecipientType.TO, new Address[]{new InternetAddress(memberToAdd.getEmail())});
      mimeMessage.setSubject(subject);

      MimeMultipart rootMixedMultipart = getMimeMultipartMessageBody(plainText, html);
      mimeMessage.setContent(rootMixedMultipart);
    };

    mailService.sendAsync(preparator);
  }

  private String composeAcceptMailMessage(Team team, Locale locale, String variant) {
    String templateName = "plaintext".equals(variant) ? "joinrequest-acceptmail-plaintext.ftl" : "joinrequest-acceptmail.ftl";
    Map<String, Object> templateVars = new HashMap<>();
    templateVars.put("team", team);

    try {
      return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(templateName, locale), templateVars);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create accept join request mail", e);
    } catch (TemplateException e) {
      throw new RuntimeException("Failed to create accept join request mail", e);
    }
  }

  @Override
  public void sendDeclineMail(Person memberToAdd, Team team, Locale locale) {
    checkArgument(!isNullOrEmpty(memberToAdd.getEmail()));

    String subject = messageSource.getMessage("request.mail.declined.subject", null, locale);
    String html = composeDeclineMailMessage(team, locale, "html");
    String plainText = composeDeclineMailMessage(team, locale, "plaintext");

    MimeMessagePreparator preparator = mimeMessage -> {
        mimeMessage.addHeader("Precedence", "bulk");

        mimeMessage.setFrom(new InternetAddress(systemEmail));
        mimeMessage.setRecipients(Message.RecipientType.TO, new Address[] { new InternetAddress(memberToAdd.getEmail()) } );
        mimeMessage.setSubject(subject);

        MimeMultipart rootMixedMultipart = getMimeMultipartMessageBody(plainText, html);
        mimeMessage.setContent(rootMixedMultipart);
    };

    mailService.sendAsync(preparator);
  }

  private String composeDeclineMailMessage(Team team, Locale locale, String variant) {
    String templateName = "plaintext".equals(variant) ? "joinrequest-declinemail-plaintext.ftl" : "joinrequest-declinemail.ftl";
    Map<String, Object> templateVars = new HashMap<>();
    templateVars.put("team", team);

    try {
      return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(templateName, locale), templateVars);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create decline join request mail", e);
    } catch (TemplateException e) {
      throw new RuntimeException("Failed to create decline join request mail", e);
    }
  }

  @Override
  public void sendJoinTeamMail(Team team, Person person, String message, Locale locale) {
    Object[] subjectValues = {team.getName()};
    String subject = messageSource.getMessage(REQUEST_MEMBERSHIP_SUBJECT, subjectValues, locale);

    Set<Member> admins = grouperTeamService.findAdmins(team);
    if (CollectionUtils.isEmpty(admins)) {
      throw new RuntimeException("Team '" + team.getName() + "' has no admins to mail invites");
    }

    String html = composeJoinRequestMailMessage(team, person, message, locale, "html");
    String plainText = composeJoinRequestMailMessage(team, person, message, locale, "plaintext");

    List<InternetAddress> bcc = new ArrayList<>();
    for (Member admin : admins) {
      try {
        bcc.add(new InternetAddress(admin.getEmail()));
      } catch (AddressException ae) {
        log.debug("Admin has malformed email address", ae);
      }
    }
    if (bcc.isEmpty()) {
      throw new RuntimeException("Team '" + team.getName() + "' has no admins with valid email addresses to mail invites");
    }

    MimeMessagePreparator preparator = mimeMessage -> {
      mimeMessage.addHeader("Precedence", "bulk");

      mimeMessage.setFrom(new InternetAddress(systemEmail));
      mimeMessage.setRecipients(Message.RecipientType.BCC, bcc.toArray(new InternetAddress[bcc.size()]));
      mimeMessage.setSubject(subject);

      MimeMultipart rootMixedMultipart = getMimeMultipartMessageBody(plainText, html);
      mimeMessage.setContent(rootMixedMultipart);
    };

    mailService.sendAsync(preparator);
  }

  private String composeJoinRequestMailMessage(Team team, Person person, String message, Locale locale, String variant) {
    String templateName = "plaintext".equals(variant) ? "joinrequestmail-plaintext.ftl" : "joinrequestmail.ftl";
    Map<String, Object> templateVars = new HashMap<>();
    templateVars.put("requesterName", person.getDisplayName());
    // for unknown reasons Freemarker cannot call person.getEmail()
    templateVars.put("requesterEmail", person.getEmail());
    templateVars.put("team", team);
    templateVars.put("teamsURL", teamsUrl);
    templateVars.put("message", message);

    try {
      return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(templateName, locale), templateVars);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create invitation mail", e);
    } catch (TemplateException e) {
      throw new RuntimeException("Failed to create invitation mail", e);
    }
  }
}
