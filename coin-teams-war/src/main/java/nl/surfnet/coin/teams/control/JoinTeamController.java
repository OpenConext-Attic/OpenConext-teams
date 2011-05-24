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

import java.io.IOException;
import java.net.URLEncoder;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
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
import nl.surfnet.coin.teams.util.ViewUtil;

/**
 * {@link Controller} that handles the join team page of a logged in
 * user.
 */
@Controller
@SessionAttributes(JoinTeamController.JOIN_TEAM_REQUEST)
public class JoinTeamController {

  private static final String REQUEST_MEMBERSHIP_SUBJECT = "request.MembershipSubject";

  public static final String JOIN_TEAM_REQUEST = "joinTeamRequest";

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
      throw new RuntimeException("Cannot find team for parameter 'team'");
    }
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);


    modelMap.addAttribute("team", team);
    JoinTeamRequest joinTeamRequest =
            joinTeamRequestService.findPendingRequest(person, team);
    if (joinTeamRequest == null) {
      joinTeamRequest = new JoinTeamRequest(person.getId(), team.getId());
    }

    if (!StringUtils.hasText(joinTeamRequest.getMessage())) {
      Locale locale = localeResolver.resolveLocale(request);
      Object[] messageValues = {person.getDisplayName(), team.getName()};
      joinTeamRequest.setMessage(messageSource.getMessage(
              "jsp.jointeam.Message", messageValues, locale));
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

    Team team = teamService.findTeamById(joinTeamRequest.getGroupId());

    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);

    String message = joinTeamRequest.getMessage();
    // First send mail, then optionally create record in db
    sendJoinTeamMessage(team, person, message,
            localeResolver.resolveLocale(request));

    joinTeamRequest.setTimestamp(new Date().getTime());
    joinTeamRequestService.saveOrUpdate(joinTeamRequest);

    return new RedirectView("home.shtml?teams=my&view="
            + ViewUtil.getView(request));
  }

  private void sendJoinTeamMessage(final Team team, final Person person,
                                   final String message, final Locale locale)
          throws IllegalStateException, IOException {

    Object[] subjectValues = {team.getName()};
    String subject = messageSource.getMessage(REQUEST_MEMBERSHIP_SUBJECT,
            subjectValues, locale);

    StringBuilder messageBody = new StringBuilder();
    if (message != null) {
      messageBody.append(message);
    }

    Object[] footerValues = {environment.getTeamsURL(), person.getDisplayName(),
            person.getEmail(), URLEncoder.encode(team.getId(), "utf-8")};
    messageBody.append(messageSource.getMessage("request.mail.GoToUrlToAccept",
            footerValues, locale));

    Set<Member> admins = teamService.findAdmins(team);
    if (CollectionUtils.isEmpty(admins)) {
      throw new IllegalStateException("Team '" + team.getName()
              + "' has no admins to mail invites");
    }
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(environment.getSystemEmail());
    List<String> bcc = new ArrayList<String>();
    for (Member admin : admins) {
      bcc.add(admin.getEmail());
    }
    mailMessage.setBcc(bcc.toArray(new String[bcc.size()]));
    mailMessage.setSubject(subject);
    mailMessage.setText(messageBody.toString());
    mailService.sendAsync(mailMessage);
  }

  public void setTeamService(TeamService teamService) {
    this.teamService = teamService;
  }
}
