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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.view.RedirectView;
import teams.domain.ExternalGroup;
import teams.domain.Person;
import teams.domain.Team;
import teams.domain.TeamExternalGroup;
import teams.interceptor.LoginInterceptor;
import teams.service.GrouperTeamService;
import teams.service.TeamExternalGroupDao;
import teams.service.VootClient;
import teams.util.AuditLog;
import teams.util.ControllerUtil;
import teams.util.TokenUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static teams.util.TokenUtil.TOKENCHECK;
import static teams.util.TokenUtil.checkTokens;
import static teams.util.ViewUtil.escapeViewParameters;

@Controller
@SessionAttributes(TOKENCHECK)
public class AddExternalGroupController {

  protected static final String EXTERNAL_GROUPS_SESSION_KEY = "externalGroups";

  @Autowired
  private VootClient vootClient;

  @Autowired
  private GrouperTeamService teamService;

  @Autowired
  private TeamExternalGroupDao teamExternalGroupDao;

  @Autowired
  private ControllerUtil controllerUtil;

  @RequestMapping(value = "/addexternalgroup.shtml")
  public String showAddExternalGroupsForm(@RequestParam String teamId, Model model, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    Team team = teamService.findTeamById(teamId);

    checkUserHasAdministrativePrivileges(person, team);

    List<ExternalGroup> myExternalGroups = filterLinkedExternalGroups(team, getExternalGroups(person.getId(), request.getSession()));
    request.getSession().setAttribute(EXTERNAL_GROUPS_SESSION_KEY, myExternalGroups);

    model.addAttribute(TOKENCHECK, TokenUtil.generateSessionToken());
    model.addAttribute("teamId", team.getId());
    model.addAttribute("team", team);

    return "addexternalgroup";
  }

  @RequestMapping(value = "/deleteexternalgroup.shtml")
  public RedirectView deleteTeamExternalGroupLink(
    @ModelAttribute(TOKENCHECK) String sessionToken,
    @RequestParam String teamId,
    @RequestParam String groupIdentifier,
    @RequestParam String token,
    ModelMap modelMap, SessionStatus status, HttpServletRequest request) {

    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    checkTokens(sessionToken, token, status);
    Team team = controllerUtil.getTeamById(teamId);
    checkUserIsAdmin(person, team);

    TeamExternalGroup teamExternalGroup = teamExternalGroupDao.getByTeamIdentifierAndExternalGroupIdentifier(teamId, groupIdentifier);
    if (teamExternalGroup != null) {
      teamExternalGroupDao.delete(teamExternalGroup);
      AuditLog.log("User {} deleted external group from team {}: {}", person.getId(), teamId, teamExternalGroup.getExternalGroup());
    }

    status.setComplete();
    modelMap.clear();

    return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s", teamId), false, true, false);
  }

  /*
   * Gets a List of {@link ExternalGroup}'s the person is a member of. First
   * tries to get the list from the session. If this returns nothing, the groups are retrieved from the VootService.
   */
  @SuppressWarnings("unchecked")
  private List<ExternalGroup> getExternalGroups(String personId, HttpSession session) {
    List<ExternalGroup> externalGroups = (List<ExternalGroup>) session.getAttribute(EXTERNAL_GROUPS_SESSION_KEY);
    if (!CollectionUtils.isEmpty(externalGroups)) {
      return externalGroups;
    }
    return vootClient.groups(personId);
  }

  private List<ExternalGroup> filterLinkedExternalGroups(Team team, List<ExternalGroup> externalGroups) {
    if (externalGroups.isEmpty()) {
      return externalGroups;
    }

    List<String> existingLinkedGroups = teamExternalGroupDao.getByTeamIdentifier(team.getId()).stream()
        .map(teg -> teg.getExternalGroup().getIdentifier())
        .collect(toList());

    return externalGroups.stream()
        .filter(externalGroup -> !existingLinkedGroups.contains(externalGroup.getIdentifier()))
        .collect(toList());
  }

  @RequestMapping(value = "/doaddexternalgroup.shtml", method = RequestMethod.POST)
  public RedirectView addExternalGroups(@ModelAttribute(TOKENCHECK) String sessionToken,
                                        @ModelAttribute("teamId") String teamId,
                                        @RequestParam String token,
                                        ModelMap modelMap, SessionStatus status, HttpServletRequest request) {

    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    checkTokens(sessionToken, token, status);
    Team team = controllerUtil.getTeamById(teamId);
    checkUserHasAdministrativePrivileges(person, team);

    String personId = person.getId();

    Map<String, ExternalGroup> externalGroupMap = getExternalGroups(personId, request.getSession()).stream()
        .collect(Collectors.toMap(ExternalGroup::getIdentifier, Function.identity()));

    List<TeamExternalGroup> teamExternalGroups = Arrays.stream(request.getParameterValues(EXTERNAL_GROUPS_SESSION_KEY))
        .map(identifier -> {
          TeamExternalGroup t = new TeamExternalGroup();
          t.setGrouperTeamId(teamId);
          t.setExternalGroup(externalGroupMap.get(identifier));
          return t;
        }).collect(toList());

    teamExternalGroups.forEach(teamExternalGroupDao::saveOrUpdate);
    teamExternalGroups.forEach(eg -> AuditLog.log("User {} added external group to team {}: {}", personId, teamId, eg));

    request.getSession().removeAttribute(EXTERNAL_GROUPS_SESSION_KEY);
    status.setComplete();
    modelMap.clear();

    return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s", teamId), false, true, false);
  }

  private void checkUserHasAdministrativePrivileges(Person person, Team team) {
    if (!controllerUtil.hasUserAdministrativePrivileges(person, team)) {
      throw new RuntimeException(String.format("Requester (%s) is not member or does not have the correct privileges", person.getId()));
    }
  }

  private void checkUserIsAdmin(Person person, Team team) {
    if (!controllerUtil.hasUserAdminPrivileges(person, team)) {
      throw new RuntimeException(String.format("Requester (%s) is not member or does not have the correct privileges", person.getId()));
    }
  }

}
