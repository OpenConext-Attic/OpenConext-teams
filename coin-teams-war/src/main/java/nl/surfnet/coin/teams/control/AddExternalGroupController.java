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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.opensocial.models.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.api.client.domain.Group20Entry;
import nl.surfnet.coin.teams.domain.ExternalGroup;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.GroupProviderUserOauth;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.domain.TeamExternalGroup;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GroupProviderService;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.OauthGroupService;
import nl.surfnet.coin.teams.service.TeamExternalGroupDao;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.ExternalGroupUtil;
import nl.surfnet.coin.teams.util.TokenUtil;
import nl.surfnet.coin.teams.util.ViewUtil;

/**
 * Controller to add an external group to a SURFteam
 */
@Controller
@SessionAttributes({"team", TokenUtil.TOKENCHECK})
public class AddExternalGroupController {

  @Autowired
  private GroupProviderService groupProviderService;

  @Autowired
  private OauthGroupService oauthGroupService;

  @Autowired
  private GrouperTeamService teamService;

  @Autowired
  private TeamExternalGroupDao teamExternalGroupDao;

  @Autowired
  private ControllerUtil controllerUtil;

  private static final Logger log = LoggerFactory.getLogger(AddExternalGroupController.class);
  private static final String UTF_8 = "utf-8";

  @RequestMapping(value = "/addexternalgroup.shtml")
  public String showAddExternalGroupsForm(@RequestParam String teamId, ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();
    if (!controllerUtil.hasUserAdministrativePrivileges(person, teamId)) {
      throw new RuntimeException("Requester (" + person.getId() + ") is not member or does not have the correct " +
          "privileges to add external groups");
    }
    modelMap.addAttribute(TokenUtil.TOKENCHECK, TokenUtil.generateSessionToken());

    final Team team = teamService.findTeamById(teamId);
    modelMap.addAttribute("team", team);

    List<ExternalGroup> myExternalGroups = getExternalGroups(personId, request);
    myExternalGroups = filterLinkedExternalGroups(team, myExternalGroups);
    request.getSession().setAttribute("externalGroups", myExternalGroups);
    modelMap.addAttribute("team", team);
    ViewUtil.addViewToModelMap(request, modelMap);
    return "addexternalgroup";
  }

  @RequestMapping(value = "/deleteexternalgroup.shtml")
  public RedirectView deleteTeamExternalGroupLink(@ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                                  @RequestParam String teamId,
                                                  @RequestParam String groupIdentifier,
                                                  @RequestParam String token,
                                                  ModelMap modelMap, SessionStatus status,
                                                  HttpServletRequest request)
      throws UnsupportedEncodingException {
    TokenUtil.checkTokens(sessionToken, token, status);

    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    if (!controllerUtil.hasUserAdminPrivileges(person, teamId)) {
      throw new RuntimeException("Requester (" + person.getId() + ") is not member or does not have the correct " +
          "privileges to remove external groups");
    }

    TeamExternalGroup teamExternalGroup =
        teamExternalGroupDao.getByTeamIdentifierAndExternalGroupIdentifier(teamId, groupIdentifier);
    if (teamExternalGroup != null) {
      teamExternalGroupDao.delete(teamExternalGroup);
    }

    status.setComplete();
    modelMap.clear();
    return new RedirectView("detailteam.shtml?team=" + URLEncoder.encode(teamId, UTF_8) + "&view="
        + ViewUtil.getView(request), false, true, false);
  }

  /**
   * Gets a List of {@link ExternalGroup}'s the person is a member of.
   * First tries to get the list from the session. If this returns nothing, the groups are retrieved from the external
   * {@link GroupProvider}.
   *
   * @param personId unique identifier of a person
   * @param request  current {@link HttpServletRequest}
   * @return List of {@link ExternalGroup}'s, may be empty
   */
  private List<ExternalGroup> getExternalGroups(String personId, HttpServletRequest request) {
    @SuppressWarnings("unchecked") List<ExternalGroup> externalGroups =
        (List<ExternalGroup>) request.getSession().getAttribute("externalGroups");
    if (CollectionUtils.isNotEmpty(externalGroups)) {
      return externalGroups;
    }
    final List<GroupProviderUserOauth> oauthList = groupProviderService.getGroupProviderUserOauths(personId);
    externalGroups = new ArrayList<ExternalGroup>();
    final List<GroupProvider> groupProviders = new ArrayList<GroupProvider>();
    for (GroupProviderUserOauth oauth : oauthList) {
      final GroupProvider groupProvider = groupProviderService.getGroupProviderByStringIdentifier(oauth.getProvider());
      groupProviders.add(groupProvider);
      try {
        final Group20Entry entry = oauthGroupService.getGroup20Entry(oauth, groupProvider, 250, 0);
        if (entry == null) {
          continue;
        }
        externalGroups.addAll(ExternalGroupUtil.convertToExternalGroups(groupProvider, entry));
      } catch (RuntimeException e) {
        log.info("Failed to retrieve external groups for user " + personId + " and provider " + groupProvider.getIdentifier(), e);
      }
    }
    return externalGroups;
  }


  /**
   * Iterates over a List of {@link ExternalGroup}'s and removes the ExternalGroup's from the list that have already a
   * link to the SURFteam {@link Team}
   *
   * @param team           SURFteam
   * @param externalGroups List of {@link ExternalGroup}'s
   * @return List of ExternalGroups that are not yet linked to the Team, may be empty
   */
  private List<ExternalGroup> filterLinkedExternalGroups(Team team, List<ExternalGroup> externalGroups) {
    if (externalGroups.isEmpty()) {
      return externalGroups;
    }
    List<ExternalGroup> filteredGroups = externalGroups;

    final List<TeamExternalGroup> byTeamIdentifier = teamExternalGroupDao.getByTeamIdentifier(team.getId());
    if (byTeamIdentifier.isEmpty()) {
      return filteredGroups;
    }

    Iterator<ExternalGroup> iterator = filteredGroups.iterator();
    while (iterator.hasNext()) {
      ExternalGroup myNext = iterator.next();
      for (TeamExternalGroup teg : byTeamIdentifier) {
        if (myNext.getIdentifier().equals(teg.getExternalGroup().getIdentifier())) {
          iterator.remove();
        }
      }
    }
    return filteredGroups;
  }

  @RequestMapping(value = "/doaddexternalgroup.shtml", method = RequestMethod.POST)
  public RedirectView addExternalGroups(@ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                                   @ModelAttribute("team") Team team,
                                                   @RequestParam String token,
                                                   ModelMap modelMap, SessionStatus status,
                                                   HttpServletRequest request) throws UnsupportedEncodingException {

    TokenUtil.checkTokens(sessionToken, token, status);
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();
    if (!controllerUtil.hasUserAdministrativePrivileges(person, team.getId())) {
      throw new RuntimeException("Requester (" + person.getId() + ") is not member or does not have the correct " +
          "privileges to add external groups");
    }

    final List<ExternalGroup> myExternalGroups = getExternalGroups(personId, request);
    Map<String, ExternalGroup> map = new HashMap<String, ExternalGroup>();
    for (ExternalGroup e : myExternalGroups) {
      map.put(e.getIdentifier(), e);
    }
    final String[] chosenGroups = request.getParameterValues("externalGroups");

    List<TeamExternalGroup> teamExternalGroups = new ArrayList<TeamExternalGroup>();
    for (String identifier : chosenGroups) {
      TeamExternalGroup t = new TeamExternalGroup();
      t.setGrouperTeamId(team.getId());
      ExternalGroup externalGroup = map.get(identifier);
      t.setExternalGroup(externalGroup);
      teamExternalGroups.add(t);
      teamExternalGroupDao.saveOrUpdate(t);
    }

    request.getSession().removeAttribute("externalGroups");
    status.setComplete();
    modelMap.clear();
    return new RedirectView("detailteam.shtml?team=" + URLEncoder.encode(team.getId(), UTF_8) + "&view="
        + ViewUtil.getView(request), false, true, false);
  }
}
