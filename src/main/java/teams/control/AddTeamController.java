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

import static teams.util.ViewUtil.escapeViewParameters;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.LocaleResolver;

import teams.Application;
import teams.domain.Invitation;
import teams.domain.InvitationMessage;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Stem;
import teams.domain.Team;
import teams.interceptor.LoginInterceptor;
import teams.service.GrouperTeamService;
import teams.service.TeamInviteService;
import teams.util.AuditLog;
import teams.util.ControllerUtil;
import teams.util.DuplicateTeamException;
import teams.util.PermissionUtil;
import teams.util.TokenUtil;
import teams.util.ViewUtil;

/**
 * {@link Controller} that handles the add team page of a logged in user.
 */
@Controller
@SessionAttributes({"team", TokenUtil.TOKENCHECK})
public class AddTeamController {

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private Environment environment;

  @Autowired
  private ControllerUtil controllerUtil;

  @Autowired
  private AddMemberController addMemberController;

  @Autowired
  private TeamInviteService teamInviteService;

  @Value("${defaultStemName}")
  private String defaultStemName;

  @Value("${grouperPowerUser}")
  private String grouperPowerUser;

  @InitBinder
  protected void initBinder(ServletRequestDataBinder binder) throws Exception {
    binder.registerCustomEditor(Stem.class, "stem", new PropertyEditorSupport() {
      @Override
      public void setAsText(String text) {
        Stem stem = new Stem(text, "", "");
        setValue(stem);
      }
    });
  }

  @RequestMapping("/addteam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request) {
    ViewUtil.addViewToModelMap(request, modelMap);

    if (PermissionUtil.isGuest(request)) {
      return "redirect:home.shtml";
    }

    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    Team team = new Team();
    team.setViewable(true);
    List<Stem> stems = getStemsForMember(person.getId());
    modelMap.addAttribute("hasMultipleStems", stems.size() > 1);
    modelMap.addAttribute("stems", stems);
    modelMap.addAttribute("team", team);
    modelMap.addAttribute(TokenUtil.TOKENCHECK, TokenUtil.generateSessionToken());

    return "addteam";
  }

  @RequestMapping(value = "/doaddteam.shtml", method = RequestMethod.POST)
  public String addTeam(ModelMap modelMap,
                        @ModelAttribute("team") Team team,
                        HttpServletRequest request,
                        @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                        @RequestParam() String token,
                        SessionStatus status) throws IOException {

    TokenUtil.checkTokens(sessionToken, token, status);
    ViewUtil.addViewToModelMap(request, modelMap);

    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();

    String admin2 = request.getParameter("admin2");
    String admin2Message = request.getParameter("admin2message");

    modelMap.addAttribute("admin2", admin2);
    modelMap.addAttribute("admin2message", admin2Message);

    if (PermissionUtil.isGuest(request)) {
      throw new RuntimeException("User is not allowed to add a team!");
    }
    // Check if the user is not requesting the wrong stem.
    if (team.getStem() != null && !isPersonUsingAllowedStem(personId, team.getStem().getId())) {
      throw new RuntimeException("User is not allowed to add a team!");
    }

    String stemId = team.getStem() != null ? team.getStem().getId() : defaultStemName;
    // (Ab)using a Team bean, do not use for actual storage
    String teamName = team.getName();
    if (!StringUtils.hasText(teamName)) {
      modelMap.addAttribute("nameerror", "empty");
      return "addteam";
    }
    // Colons conflict with the stem name
    teamName = teamName.replace(":", "");

    String teamDescription = team.getDescription();

    String teamId;
    try {
      teamId = grouperTeamService.addTeam(teamName, teamName, teamDescription, stemId);
      AuditLog.log("User {} added team (name: {}, id: {}) with stem {}", personId, teamName, teamId, stemId);
      final Locale locale = localeResolver.resolveLocale(request);
      inviteAdmin(teamId, person, admin2, teamName, admin2Message, locale);
    } catch (DuplicateTeamException e) {
      modelMap.addAttribute("nameerror", "duplicate");
      return "addteam";
    }

    grouperTeamService.setVisibilityGroup(teamId, team.isViewable());
    grouperTeamService.addMember(teamId, person);
    grouperTeamService.addMemberRole(teamId, personId, Role.Admin, grouperPowerUser);

    status.setComplete();
    modelMap.clear();
    if (environment.acceptsProfiles(Application.GROUPZY_PROFILE_NAME)) {
      return escapeViewParameters("redirect:/%s/service-providers.shtml?view=", teamId);
    } else {
      return escapeViewParameters("redirect:detailteam.shtml?team=%s&view=%s", teamId, ViewUtil.getView(request));
    }
  }

  private void inviteAdmin(final String teamId, final Person inviter, final String admin2, final String teamName,
                           final String messageBody, final Locale locale) {
    if (!StringUtils.hasText(admin2) || !admin2.contains("@")) {
      return;
    }
    Invitation invitation = new Invitation(admin2, teamId);
    invitation.setIntendedRole(Role.Admin);
    invitation.setTimestamp(new Date().getTime());

    InvitationMessage message = new InvitationMessage(messageBody, inviter.getDisplayName());

    invitation.addInvitationMessage(message);
    teamInviteService.saveOrUpdate(invitation);
    Object[] messageValuesSubject = {teamName};

    String subject = messageSource.getMessage(AddMemberController.INVITE_SEND_INVITE_SUBJECT, messageValuesSubject, locale);

    addMemberController.sendInvitationByMail(invitation, subject, inviter, locale);
    AuditLog.log("Sent invitation and saved to database: team: {}, inviter: {}, hash: {}, email: {}, role: {}",
      teamId, inviter.getId(), invitation.getInvitationHash(), admin2, invitation.getIntendedRole());
  }

  private List<Stem> getStemsForMember(String personId) {
    List<Stem> allUsersStems = grouperTeamService.findStemsByMember(personId);
    List<Stem> stems = new ArrayList<>();

    if (allUsersStems.isEmpty()) {
      return allUsersStems;
    }

    // Now check if the stem has a members group and the user is actually in that members group
    for (Stem stem : allUsersStems) {
      // Always add the default stem
      if (stem.getId().equalsIgnoreCase(defaultStemName)) {
        stems.add(stem);
      }
      // Find the members team for the stem and check if the current person is member of that team
      String teamId = stem.getId() + ":" + "members";
      try {
        Team team = grouperTeamService.findTeamById(teamId);

        if (controllerUtil.isPersonMemberOfTeam(personId, team)) {
          stems.add(stem);
        }
      } catch (RuntimeException e) {
        // do nothing
      }
    }
    return stems;
  }

  private boolean isPersonUsingAllowedStem(String personId, String stemId) {
    return getStemsForMember(personId).stream()
        .anyMatch(allowedStem -> allowedStem.getId().equalsIgnoreCase(stemId));
  }
}
