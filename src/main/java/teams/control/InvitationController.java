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

import nl.surfnet.coin.stoker.Stoker;
import nl.surfnet.coin.stoker.StokerEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.view.RedirectView;
import teams.Application;
import teams.domain.*;
import teams.interceptor.LoginInterceptor;
import teams.service.GrouperTeamService;
import teams.service.TeamInviteService;
import teams.service.TeamsDao;
import teams.util.AuditLog;
import teams.util.ControllerUtil;
import teams.util.TokenUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static teams.util.TokenUtil.checkTokens;
import static teams.util.ViewUtil.escapeViewParameters;

/**
 * {@link Controller} that handles the accept/decline of an Invitation
 */
@Controller
@SessionAttributes({"invitation", TokenUtil.TOKENCHECK})
public class InvitationController {

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private ControllerUtil controllerUtil;

  @Autowired(required = false)
  private TeamsDao teamsDao;

  @Autowired(required = false)
  private Stoker stoker;

  @Value("${grouperPowerUser}")
  private String grouperPowerUser;

  @Value("${group-name-context}")
  private String groupNameContext;

  @Autowired
  private Environment environment;

  /**
   * RequestMapping to show the accept invitation page.
   *
   * @param modelMap {@link ModelMap}
   * @param request  {@link HttpServletRequest}
   * @return accept invitation page
   */
  @RequestMapping(value = "/acceptInvitation.shtml")
  public String accept(ModelMap modelMap, HttpServletRequest request) throws UnsupportedEncodingException {
    Optional<Invitation> invitationO = getInvitationByRequest(request);
    if (!invitationO.isPresent()) {
      modelMap.addAttribute("action", "missing");
      return "invitationexception";
    }

    Invitation invitation = invitationO.get();

    if (invitation.isDeclined()) {
      modelMap.addAttribute("action", "declined");
      return "invitationexception";
    }
    if (invitation.isAccepted()) {
      modelMap.addAttribute("action", "accepted");
      String teamId = invitation.getTeamId();
      String teamUrl = escapeViewParameters("detailteam.shtml?team=%s", teamId);
      modelMap.addAttribute("teamUrl", teamUrl);

      return "invitationexception";
    }

    String teamId = invitation.getTeamId();
    if (!StringUtils.hasText(teamId)) {
      throw new RuntimeException("Invalid invitation");
    }
    Team team = controllerUtil.getTeamById(teamId);

    modelMap.addAttribute("invitation", invitation);
    modelMap.addAttribute("team", team);
    modelMap.addAttribute("date", new Date(invitation.getTimestamp()));
    modelMap.addAttribute("groupzyEnabled", environment.acceptsProfiles(Application.GROUPZY_PROFILE_NAME));

    if (environment.acceptsProfiles(Application.GROUPZY_PROFILE_NAME)) {
      Collection<TeamServiceProvider> serviceProviders = teamsDao.forTeam(groupNameContext + teamId);
      Collection<StokerEntry> eduGainServiceProviders = stoker.getEduGainServiceProviders(
          serviceProviders.stream().map(TeamServiceProvider::getSpEntityId).collect(toList()));
      modelMap.addAttribute("serviceProviders", eduGainServiceProviders);
    }
    return "acceptinvitation";
  }

  /**
   * RequestMapping to accept an invitation. If everything is okay, it redirects
   * to your new team detail view.
   *
   * @param request {@link HttpServletRequest}
   * @return detail view of your new team
   * @throws UnsupportedEncodingException if the server does not support utf-8
   */
  @RequestMapping(value = "/doAcceptInvitation.shtml")
  public RedirectView doAccept(HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    Invitation invitation = getInvitationByRequest(request)
        .orElseThrow(() -> new IllegalArgumentException("Cannot find your invitation. Invitations expire after 14 days."));

    if (invitation.isDeclined()) {
      throw new RuntimeException("Invitation is Declined");
    }
    if (invitation.isAccepted()) {
      throw new IllegalStateException("Invitation is already Accepted");
    }
    String teamId = invitation.getTeamId();
    if (!StringUtils.hasText(teamId)) {
      throw new RuntimeException("Invalid invitation");
    }
    controllerUtil.getTeamById(teamId);

    String memberId = person.getId();
    grouperTeamService.addMember(teamId, person);

    Role intendedRole = invitation.getIntendedRole();
    if (person.isGuest() && Role.Admin.equals(intendedRole)) {
      // cannot make a guest Admin
      invitation.setIntendedRole(Role.Manager);
    }
    intendedRole = invitation.getIntendedRole();
    grouperTeamService.addMemberRole(teamId, memberId, intendedRole, grouperPowerUser);
    AuditLog.log("User {} accepted invitation for team {} with intended role {}", person.getId(), teamId, intendedRole);
    invitation.setAccepted(true);
    teamInviteService.saveOrUpdate(invitation);

    return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s", teamId));
  }

  /**
   * RequestMapping to decline an invitation as receiver.
   * This URL is bypassed in {@link LoginInterceptor}
   *
   * @param modelMap {@link ModelMap}
   * @param request  {@link HttpServletRequest}
   * @return view for decline result
   */
  @RequestMapping(value = "/declineInvitation.shtml")
  public String decline(ModelMap modelMap, HttpServletRequest request) {
    String viewTemplate = "invitationdeclined";

    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    Optional<Invitation> invitationO = getInvitationByRequest(request);

    if (!invitationO.isPresent()) {
      // even if we can't find the invitation, we'll display success!
      return viewTemplate;
    }

    Invitation invitation = invitationO.get();

    invitation.setDeclined(true);
    teamInviteService.saveOrUpdate(invitation);
    AuditLog.log("User {} declined invitation for team {} with intended role {}", person.getId(), invitation.getTeamId(), invitation.getIntendedRole());

    return viewTemplate;
  }

  /**
   * RequestMapping to delete an invitation as admin
   *
   * @param request {@link javax.servlet.http.HttpServletRequest}
   * @return redirect to detailteam if everything is okay
   * @throws UnsupportedEncodingException in the rare condition utf-8 is not supported
   */
  @RequestMapping(value = "/deleteInvitation.shtml")
  public RedirectView deleteInvitation(HttpServletRequest request,
                                       @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                       @RequestParam String token,
                                       @RequestParam String id,
                                       SessionStatus status,
                                       ModelMap modelMap) {
    checkTokens(sessionToken, token, status);

    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    if (person == null) {
      status.setComplete();
      return new RedirectView("landingpage.shtml");
    }

    Invitation invitation = teamInviteService.findInvitationByInviteId(id).orElseThrow(IllegalArgumentException::new);

    String teamId = invitation.getTeamId();

    if (!controllerUtil.hasUserAdministrativePrivileges(person, teamId)) {
      throw new RuntimeException("Requester (" + person.getId() + ") is not member or does not have the correct " +
        "privileges to delete (a) member(s)");
    }

    teamInviteService.delete(invitation);
    AuditLog.log(
        "User {} deleted invitation for email {} for team {} with intended role {}",
        person.getId(), invitation.getEmail(), invitation.getTeamId(), invitation.getIntendedRole());

    status.setComplete();
    modelMap.clear();
    return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s", teamId));
  }

  @RequestMapping("/myinvitations.shtml")
  public String myInvitations(ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    String email = person.getEmail();
    if (!StringUtils.hasText(email)) {
      throw new IllegalArgumentException("Your profile does not contain an email address");
    }
    List<Invitation> invitations = teamInviteService.findPendingInvitationsByEmail(email);
    modelMap.addAttribute("invitations", invitations);
    List<Team> invitedTeams = new ArrayList<>();
    for (Invitation invitation : invitations) {
      Team team = controllerUtil.getTeamById(invitation.getTeamId());
      if (team != null) {
        invitedTeams.add(team);
      }
    }
    modelMap.addAttribute("teams", invitedTeams);
    return "myinvitations";
  }

  private Optional<Invitation> getInvitationByRequest(HttpServletRequest request) {
    String invitationId = request.getParameter("id");

    if (!StringUtils.hasText(invitationId)) {
      throw new IllegalArgumentException("Missing parameter to identify the invitation");
    }

    return teamInviteService.findInvitationByInviteId(invitationId);
  }

}
