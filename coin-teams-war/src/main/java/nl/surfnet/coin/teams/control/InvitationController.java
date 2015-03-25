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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import nl.surfnet.coin.teams.domain.Person;
import nl.surfnet.coin.stoker.Stoker;
import nl.surfnet.coin.stoker.StokerEntry;
import nl.surfnet.coin.teams.domain.*;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.TeamsDao;
import nl.surfnet.coin.teams.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;


/**
 * {@link Controller} that handles the accept/decline of an Invitation
 */
@Controller
@SessionAttributes({ "invitation", TokenUtil.TOKENCHECK })
public class InvitationController {

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private TeamEnvironment teamEnvironment;

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private ControllerUtil controllerUtil;

  @Autowired(required = false)
  private TeamsDao teamsDao;

  @Autowired(required = false)
  private Stoker stoker;

  /**
   * RequestMapping to show the accept invitation page.
   *
   * @param modelMap {@link ModelMap}
   * @param request
   *          {@link HttpServletRequest}
   * @return accept invitation page
   * @throws UnsupportedEncodingException
   *           if the server does not support utf-8
   */
  @RequestMapping(value = "/acceptInvitation.shtml")
  public String accept(ModelMap modelMap, HttpServletRequest request) throws UnsupportedEncodingException {
    Invitation invitation = getInvitationByRequest(request);
    if (invitation == null) {
      modelMap.addAttribute("action", "missing");
      return "invitationexception";
    }
    if (invitation.isDeclined()) {
      modelMap.addAttribute("action", "declined");
      return "invitationexception";
    }
    if (invitation.isAccepted()) {
      modelMap.addAttribute("action", "accepted");
      String teamId = invitation.getTeamId();
      String teamUrl = "detailteam.shtml?team=" + URLEncoder.encode(teamId, "utf-8")
              + "&view=" + ViewUtil.getView(request);
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
    modelMap.addAttribute("groupzyEnabled", teamEnvironment.isGroupzyEnabled());

    String groupNameContext = teamEnvironment.getGroupNameContext();

    if(teamEnvironment.isGroupzyEnabled()) {
      Collection<TeamServiceProvider> serviceProviders = teamsDao.forTeam(groupNameContext + teamId);

      Collection<StokerEntry> eduGainServiceProviders = stoker.getEduGainServiceProviders(Collections2.transform(serviceProviders, new Function<TeamServiceProvider, String>() {
        @Override
        public String apply(TeamServiceProvider input) {
          return input.getSpEntityId();
        }
      }));
      modelMap.addAttribute("serviceProviders", eduGainServiceProviders);
    }
    ViewUtil.addViewToModelMap(request, modelMap);
    return "acceptinvitation";
  }

  /**
   * RequestMapping to accept an invitation. If everything is okay, it redirects
   * to your new team detail view.
   *
   * @param request
   *          {@link HttpServletRequest}
   * @return detail view of your new team
   * @throws UnsupportedEncodingException
   *           if the server does not support utf-8
   */
  @RequestMapping(value = "/doAcceptInvitation.shtml")
  public RedirectView doAccept(HttpServletRequest request)
      throws UnsupportedEncodingException {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);

    Invitation invitation = getInvitationByRequest(request);
    if (invitation == null) {
      throw new IllegalArgumentException(
          "Cannot find your invitation. Invitations expire after 14 days.");
    }
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
    grouperTeamService.addMemberRole(teamId, memberId, intendedRole, teamEnvironment.getGrouperPowerUser());
    AuditLog.log("User {} accepted invitation for team {} with intended role {}", person.getId(), teamId, intendedRole);
    invitation.setAccepted(true);
    teamInviteService.saveOrUpdate(invitation);

    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8") + "&view="
        + ViewUtil.getView(request));
  }

  /**
   * RequestMapping to decline an invitation as receiver.
   * This URL is bypassed in {@link LoginInterceptor}
   *
   * @param modelMap {@link ModelMap}
   * @param request
   *          {@link HttpServletRequest}
   * @return view for decline result
   */
  @RequestMapping(value = "/declineInvitation.shtml")
  public String decline(ModelMap modelMap,
                        HttpServletRequest request) {
    String viewTemplate = "invitationdeclined";

    Person person = (Person) request.getSession().getAttribute(
      LoginInterceptor.PERSON_SESSION_KEY);

    Invitation invitation = getInvitationByRequest(request);

    if (invitation == null) {
      // even if we can't find the invitation, we'll display success!
      return viewTemplate;
    }

    invitation.setDeclined(true);
    teamInviteService.saveOrUpdate(invitation);
    AuditLog.log("User {} declined invitation for team {} with intended role {}", person.getId(), invitation.getTeamId(), invitation.getIntendedRole());
    ViewUtil.addViewToModelMap(request, modelMap);
    return viewTemplate;
  }

  /**
   * RequestMapping to delete an invitation as admin
   *
   *
   * @param request
   *          {@link javax.servlet.http.HttpServletRequest}
   * @return redirect to detailteam if everything is okay
   * @throws UnsupportedEncodingException
   *           in the rare condition utf-8 is not supported
   */
  @RequestMapping(value = "/deleteInvitation.shtml")
  public RedirectView deleteInvitation(HttpServletRequest request,
                                       @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                       @RequestParam() String token,
                                       SessionStatus status,
                                       ModelMap modelMap)
          throws UnsupportedEncodingException {
    TokenUtil.checkTokens(sessionToken, token, status);
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);

    if (person == null) {
      status.setComplete();
      return new RedirectView("landingpage.shtml");
    }
    Invitation invitation = getAllInvitationByRequest(request);
    String teamId = invitation.getTeamId();

    if (!controllerUtil.hasUserAdministrativePrivileges(person, teamId)) {
      throw new RuntimeException("Requester (" + person.getId() + ") is not member or does not have the correct " +
        "privileges to delete (a) member(s)");
    }


    teamInviteService.delete(invitation);
    AuditLog.log("User {} deleted invitation for email {} for team {} with intended role {}", person.getId(), invitation.getEmail(), invitation.getTeamId(), invitation.getIntendedRole());

    status.setComplete();
    modelMap.clear();
    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8") + "&view="
        + ViewUtil.getView(request));
  }

  @RequestMapping("/resendInvitation.shtml")
  public String resendInvitation(ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    Invitation invitation = getAllInvitationByRequest(request);
        if (invitation == null) {
      throw new IllegalArgumentException(
          "Cannot find the invitation. Invitations expire after 14 days.");
    }

    Member member = grouperTeamService.findMember(invitation.getTeamId(), person.getId());
    if (member == null) {
      throw new SecurityException("You are not a member of this team");
    }
    Set<Role> roles = member.getRoles();
    if (!(roles.contains(Role.Admin) || roles.contains(Role.Manager))) {
      throw new SecurityException("You have insufficient rights to perform this action.");
    }

    modelMap.addAttribute("invitation", invitation);
    Role[] inviteRoles = {Role.Member, Role.Manager, Role.Admin};
    modelMap.addAttribute("roles", inviteRoles);
    InvitationMessage invitationMessage = invitation.getLatestInvitationMessage();
    if (invitationMessage != null) {
      modelMap.addAttribute("messageText", invitationMessage.getMessage());
    }
    ViewUtil.addViewToModelMap(request, modelMap);
    return "resendinvitation";
  }
  @RequestMapping("/myinvitations.shtml")
  public String myInvitations(ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    String email = person.getEmail();
    if (!StringUtils.hasText(email)) {
      throw new IllegalArgumentException("Your profile does not contain an email address");
    }
    List<Invitation> invitations = teamInviteService.findPendingInvitationsByEmail(email);
    modelMap.addAttribute("invitations", invitations);
    List<Team> invitedTeams = new ArrayList<Team>();
    for(Invitation invitation : invitations) {
      Team team = controllerUtil.getTeamById(invitation.getTeamId());
      if(team != null) {
        invitedTeams.add(team);
      }
    }
    modelMap.addAttribute("teams", invitedTeams);
    ViewUtil.addViewToModelMap(request, modelMap);
    return "myinvitations";
  }

  private Invitation getInvitationByRequest(HttpServletRequest request) {
    String invitationId = request.getParameter("id");

    if (!StringUtils.hasText(invitationId)) {
      throw new IllegalArgumentException(
          "Missing parameter to identify the invitation");
    }

    return teamInviteService.findInvitationByInviteId(invitationId);
  }

  private Invitation getAllInvitationByRequest(HttpServletRequest request) {
    String invitationId = request.getParameter("id");

    if (!StringUtils.hasText(invitationId)) {
      throw new IllegalArgumentException(
          "Missing parameter to identify the invitation");
    }

    return teamInviteService.findAllInvitationById(invitationId);
  }

}
