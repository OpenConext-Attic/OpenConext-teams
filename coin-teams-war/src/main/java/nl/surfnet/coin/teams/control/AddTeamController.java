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

import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Stem;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.ShindigActivityService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.*;
import org.opensocial.RequestException;
import org.opensocial.models.Person;
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
import java.util.List;
import java.util.Locale;

/**
 * @author steinwelberg
 *         <p/>
 *         {@link Controller} that handles the add team page of a logged in
 *         user.
 */
@Controller
@SessionAttributes({ "team", TokenUtil.TOKENCHECK })
public class AddTeamController {

  private static final String ACTIVITY_NEW_TEAM_BODY = "activity.NewTeamBody";

  private static final String ACTIVITY_NEW_TEAM_TITLE = "activity.NewTeamTitle";

  @Autowired
  private TeamService teamService;

  @Autowired
  private ShindigActivityService shindigActivityService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private TeamEnvironment environment;

  @Autowired
  private ControllerUtil controllerUtil;

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
          throws RequestException, IOException {
    TokenUtil.checkTokens(sessionToken, token, status);
    ViewUtil.addViewToModelMap(request, modelMap);

    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();

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

    // If viewablilityStatus is set this means that the team should be private
    String viewabilityStatus = request.getParameter("viewabilityStatus");
    boolean viewable = !StringUtils.hasText(viewabilityStatus);
    team.setViewable(viewable);

    // Add the team
    String teamId;
    try {
      teamId = teamService.addTeam(teamName, teamName, teamDescription, stemId);
    } catch (DuplicateTeamException e) {
      modelMap.addAttribute("nameerror", "duplicate");
      return "addteam";
    }

    // Set the visibility of the group
    teamService.setVisibilityGroup(teamId, viewable);

    // Add the person who has added the team as admin to the team.
    teamService.addMember(teamId, person);

    // Give him the right permissions, add as the super user
    teamService.addMemberRole(teamId, personId, Role.Admin, true);

    // Add the activity to the COIN portal
    addActivity(teamId, teamName, personId,
            localeResolver.resolveLocale(request));

    status.setComplete();
    modelMap.clear();
    return "redirect:detailteam.shtml?team="
            + URLEncoder.encode(teamId, "utf-8") + "&view="
            + ViewUtil.getView(request);
  }

  private void addActivity(String teamId, String teamName, String personId,
                           Locale locale) throws RequestException, IOException {
    Object[] messageValues = { teamName };

    String title = messageSource.getMessage(ACTIVITY_NEW_TEAM_TITLE,
            messageValues, locale);
    String body = messageSource.getMessage(ACTIVITY_NEW_TEAM_BODY,
            messageValues, locale);

    shindigActivityService.addActivity(personId, teamId, title, body);
  }

  private List<Stem> getStemsForMember(String personId) {
    List<Stem> allUsersStems = teamService.findStemsByMember(personId);
    List<Stem> stems = new ArrayList<Stem>();

    if (allUsersStems.size() == 0 ) {
      return allUsersStems;
    }

    // Now check if the stem has a members group and the user is actually in that members group
    for(Stem stem : allUsersStems) {
      // Always add the default stem
      if (stem.getId().equalsIgnoreCase(environment.getDefaultStemName())) {
        stems.add(stem);
      }
      // Find the members team for the stem and check if the current person is member of that team
      String teamId = stem.getId() + ":" + "members";
      try {
        Team team = teamService.findTeamById(teamId);

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
