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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Person;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.JoinTeamRequestService;
import nl.surfnet.coin.teams.service.mail.MailService;
import nl.surfnet.coin.teams.util.AuditLog;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.ViewUtil;


/**
 * {@link Controller} that handles the join team page of a logged in
 * user.
 */
@Controller
@SessionAttributes(JoinTeamController.JOIN_TEAM_REQUEST)
public class JoinTeamController {

  private static final String REQUEST_MEMBERSHIP_SUBJECT = "request.MembershipSubject";
  private static final Logger log = LoggerFactory.getLogger(JoinTeamController.class);

  public static final String JOIN_TEAM_REQUEST = "joinTeamRequest";

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private JoinTeamRequestService joinTeamRequestService;

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

  @RequestMapping("/jointeam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    String teamId = request.getParameter("team");
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = grouperTeamService.findTeamById(teamId);
    }

    if (team == null) {
      throw new RuntimeException("Cannot find team for parameter 'team'");
    }
    Person person = (Person) request.getSession().getAttribute(
      LoginInterceptor.PERSON_SESSION_KEY);


    modelMap.addAttribute("team", team);
    JoinTeamRequest joinTeamRequest =
      joinTeamRequestService.findPendingRequest(person.getId(), team.getId());
    if (joinTeamRequest == null) {
      joinTeamRequest = new JoinTeamRequest(person.getId(), team.getId(), person.getEmail(), person.getDisplayName());
    }

    modelMap.addAttribute(JOIN_TEAM_REQUEST, joinTeamRequest);

    ViewUtil.addViewToModelMap(request, modelMap);

    return "jointeam";
  }

  @RequestMapping(value = "/dojointeam.shtml", method = RequestMethod.POST)
  public RedirectView joinTeam(ModelMap modelMap,
                               @ModelAttribute(JOIN_TEAM_REQUEST) JoinTeamRequest joinTeamRequest,
                               HttpServletRequest request)
    throws IOException {

    ViewUtil.addViewToModelMap(request, modelMap);

    Team team = controllerUtil.getTeamById(joinTeamRequest.getGroupId());

    if (!team.isViewable()) {
      throw new IllegalStateException("The team you requested to join is private.");
    }

    Person person = (Person) request.getSession().getAttribute(
      LoginInterceptor.PERSON_SESSION_KEY);

    String message = joinTeamRequest.getMessage();
    // First send mail, then optionally create record in db
    sendJoinTeamMessage(team, person, message,
      localeResolver.resolveLocale(request));

    joinTeamRequest.setTimestamp(new Date().getTime());
    joinTeamRequest.setDisplayName(person.getDisplayName());
    joinTeamRequest.setEmail(person.getEmail());
    joinTeamRequestService.saveOrUpdate(joinTeamRequest);
    AuditLog.log("User {} requested to join team {}", joinTeamRequest.getPersonId(), team.getId());
    return new RedirectView("home.shtml?teams=my&view="
      + ViewUtil.getView(request));
  }

  private void sendJoinTeamMessage(final Team team, final Person person,
                                   final String message, final Locale locale)
    throws IllegalStateException, IOException {

    Object[] subjectValues = {team.getName()};
    final String subject = messageSource.getMessage(REQUEST_MEMBERSHIP_SUBJECT,
      subjectValues, locale);

    final Set<Member> admins = grouperTeamService.findAdmins(team);
    if (CollectionUtils.isEmpty(admins)) {
      throw new RuntimeException("Team '" + team.getName()
        + "' has no admins to mail invites");
    }

    final String html = composeJoinRequestMailMessage(team, person, message, locale, "html");
    final String plainText = composeJoinRequestMailMessage(team, person, message, locale, "plaintext");

    final List<InternetAddress> bcc = new ArrayList<InternetAddress>();
    for (Member admin : admins) {
      try {
        bcc.add(new InternetAddress(admin.getEmail()));
      } catch (AddressException ae) {
        log.debug("Admin has malformed email address", ae);
      }
    }
    if (bcc.isEmpty()) {
      throw new RuntimeException("Team '" + team.getName()
        + "' has no admins with valid email addresses to mail invites");
    }

    MimeMessagePreparator preparator = new MimeMessagePreparator() {
      public void prepare(MimeMessage mimeMessage) throws MessagingException {
        mimeMessage.addHeader("Precedence", "bulk");

        mimeMessage.setFrom(new InternetAddress(systemEmail));
        mimeMessage.setRecipients(Message.RecipientType.BCC, bcc.toArray(new InternetAddress[bcc.size()]));
        mimeMessage.setSubject(subject);

        MimeMultipart rootMixedMultipart = controllerUtil.getMimeMultipartMessageBody(plainText, html);
        mimeMessage.setContent(rootMixedMultipart);
      }
    };

    mailService.sendAsync(preparator);

  }

  String composeJoinRequestMailMessage(final Team team, final Person person,
                                       final String message, final Locale locale,
                                       final String variant) {
    String templateName;
    if ("plaintext".equals(variant)) {
      templateName = "joinrequestmail-plaintext.ftl";
    } else {
      templateName = "joinrequestmail.ftl";
    }
    Map<String, Object> templateVars = new HashMap<String, Object>();
    templateVars.put("requesterName", person.getDisplayName());
    // for unknown reasons Freemarker cannot call person.getEmail()
    templateVars.put("requesterEmail", person.getEmail());
    templateVars.put("team", team);
    templateVars.put("teamsURL", teamsUrl);
    templateVars.put("message", message);

    try {
      return FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerConfiguration.getTemplate(templateName, locale), templateVars
      );
    } catch (IOException e) {
      throw new RuntimeException("Failed to create invitation mail", e);
    } catch (TemplateException e) {
      throw new RuntimeException("Failed to create invitation mail", e);
    }
  }

}
