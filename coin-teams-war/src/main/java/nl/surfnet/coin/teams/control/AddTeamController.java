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

import nl.surfnet.coin.api.client.domain.Person;
import nl.surfnet.coin.teams.domain.*;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * {@link Controller} that handles the add team page of a logged in
 * user.
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
  private TeamEnvironment environment;

  @Autowired
  private ControllerUtil controllerUtil;

  @Autowired
  private AddMemberController addMemberController;

  @Autowired
  private TeamInviteService teamInviteService;

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

    // Check if the user has permission
    if (PermissionUtil.isGuest(request)) {
      //throw new RuntimeException("User is not allowed to view add team page");
      return "redirect:home.shtml";
    }

    Person person = (Person) request.getSession().getAttribute(
      LoginInterceptor.PERSON_SESSION_KEY);

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
                        SessionStatus status)
    throws IOException {
    TokenUtil.checkTokens(sessionToken, token, status);
    ViewUtil.addViewToModelMap(request, modelMap);

    Person person = (Person) request.getSession().getAttribute(
      LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();

    String admin2 = request.getParameter("admin2");
    modelMap.addAttribute("admin2", admin2);
    String admin2Message = request.getParameter("admin2message");
    modelMap.addAttribute("admin2message", admin2Message);

    // Check if the user has permission
    if (PermissionUtil.isGuest(request)) {
      throw new RuntimeException("User is not allowed to add a team!");
    }
    // Check if the user is not requesting the wrong stem.
    if (team.getStem() != null && !isPersonUsingAllowedStem(personId, team.getStem().getId())) {
      throw new RuntimeException("User is not allowed to add a team!");
    }
    String stemId = team.getStem() != null ? team.getStem().getId() : environment.getDefaultStemName();
    // (Ab)using a Team bean, do not use for actual storage
    String teamName = team.getName();
    // Form not completely filled in.
    if (!StringUtils.hasText(teamName)) {
      modelMap.addAttribute("nameerror", "empty");
      return "addteam";
    }
    // Colons conflict with the stem name
    teamName = teamName.replace(":", "");

    String teamDescription = team.getDescription();

    // Add the team
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

    // Set the visibility of the group
    grouperTeamService.setVisibilityGroup(teamId, team.isViewable());

    // Add the person who has added the team as admin to the team.
    grouperTeamService.addMember(teamId, person);

    // Give him the right permissions, add as the super user
    grouperTeamService.addMemberRole(teamId, personId, Role.Admin, environment.getGrouperPowerUser());

    status.setComplete();
    modelMap.clear();
    if (environment.isGroupzyEnabled()) {
      return String.format("redirect:/teams/%s/service-providers.shtml?view=", teamId, ViewUtil.getView(request));
    } else {
      return "redirect:detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8") + "&view="
        + ViewUtil.getView(request);

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

    String subject = messageSource.getMessage(AddMemberController.INVITE_SEND_INVITE_SUBJECT,
      messageValuesSubject, locale);


    addMemberController.sendInvitationByMail(invitation, subject, inviter, locale);
    AuditLog.log("Sent invitation and saved to database: team: {}, inviter: {}, hash: {}, email: {}, role: {}",
      teamId, inviter.getId(), invitation.getInvitationHash(), admin2, invitation.getIntendedRole());
  }


  private List<Stem> getStemsForMember(String personId) {
    List<Stem> allUsersStems = grouperTeamService.findStemsByMember(personId);
    List<Stem> stems = new ArrayList<Stem>();

    if (allUsersStems.size() == 0) {
      return allUsersStems;
    }

    // Now check if the stem has a members group and the user is actually in that members group
    for (Stem stem : allUsersStems) {
      // Always add the default stem
      if (stem.getId().equalsIgnoreCase(environment.getDefaultStemName())) {
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
    List<Stem> allowedStems = getStemsForMember(personId);
    boolean isAllowed = false;

    for (Stem allowedStem : allowedStems) {
      if (allowedStem.getId().equalsIgnoreCase(stemId)) {
        isAllowed = true;
      }
    }
    return isAllowed;
  }
}
