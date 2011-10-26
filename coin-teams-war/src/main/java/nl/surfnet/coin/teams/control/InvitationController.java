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

import nl.surfnet.coin.teams.domain.*;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.TokenUtil;
import nl.surfnet.coin.teams.util.ViewUtil;
import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * {@link Controller} that handles the accept/decline of an Invitation
 */
@Controller
@SessionAttributes({ "invitation", TokenUtil.TOKENCHECK })
public class InvitationController {

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private TeamService teamService;

  @Autowired
  private ControllerUtil controllerUtil;

  /**
   * RequestMapping to show the accept invitation page.
   *
   * @param modelMap {@link ModelMap}
   * @param request
   *          {@link HttpServletRequest}
   * @return accept invitation page
   */
  @RequestMapping(value = "/acceptInvitation.shtml")
  public String accept(ModelMap modelMap, HttpServletRequest request) {
    Invitation invitation = getInvitationByRequest(request);
    if (invitation==null) {
      modelMap.addAttribute("action", "accepted");
      return "invitationexception";
    }
    if (invitation.isDeclined()) {
      modelMap.addAttribute("action", "accepted");
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
    ViewUtil.addViewToModelMap(request, modelMap);
    return "acceptinvitation";
  }

  /**
   * RequestMapping to show the accept invitation page.
   *
   * @param modelMap {@link ModelMap}
   * @param request
   *          {@link HttpServletRequest}
   * @return accept invitation page
   */
  @RequestMapping(value = "/vo/{voName}/acceptInvitation.shtml")
  public String acceptVO(@PathVariable String voName, ModelMap modelMap, HttpServletRequest request) {
    return accept(modelMap, request);
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
    String teamId = invitation.getTeamId();
    if (!StringUtils.hasText(teamId)) {
      throw new RuntimeException("Invalid invitation");
    }
    Team team = controllerUtil.getTeamById(teamId);

    String memberId = person.getId();
    teamService.addMember(teamId, person);
    teamService.addMemberRole(teamId, memberId, Role.Member, true);

    teamInviteService.delete(invitation);

    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8") + "&view="
        + ViewUtil.getView(request));
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
  @RequestMapping(value = "/vo/{voName}/doAcceptInvitation.shtml")
  public RedirectView doAcceptVO(@PathVariable String voName, HttpServletRequest request)
      throws UnsupportedEncodingException {
    return doAccept(request);
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

    Invitation invitation = getInvitationByRequest(request);

    if (invitation == null) {
      // even if we can't find the invitation, we'll display success!
      return viewTemplate;
    }

    invitation.setDeclined(true);
    teamInviteService.saveOrUpdate(invitation);
    ViewUtil.addViewToModelMap(request, modelMap);
    return viewTemplate;
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
  @RequestMapping(value = "/vo/{voName}/declineInvitation.shtml")
  public String declineVO(@PathVariable String voName, ModelMap modelMap,
                        HttpServletRequest request) {
    return decline(modelMap, request);
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
    teamInviteService.delete(invitation);

    status.setComplete();
    modelMap.clear();
    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8") + "&view="
        + ViewUtil.getView(request));
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
  @RequestMapping(value = "/vo/{voName}/deleteInvitation.shtml")
  public RedirectView deleteInvitationVO(HttpServletRequest request,
                                         @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                         @RequestParam() String token,
                                         SessionStatus status,
                                         ModelMap modelMap)
          throws UnsupportedEncodingException {
    return deleteInvitation(request, sessionToken, token, status, modelMap);
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

    Member member = teamService.findMember(invitation.getTeamId(), person.getId());
    if (member == null) {
      throw new SecurityException("You are not a member of this team");
    }
    Set<Role> roles = member.getRoles();
    if (!(roles.contains(Role.Admin) || roles.contains(Role.Manager))) {
      throw new SecurityException("You have insufficient rights to perform this action.");
    }

    modelMap.addAttribute("invitation", invitation);
    InvitationMessage invitationMessage = invitation.getLatestInvitationMessage();
    if (invitationMessage != null) {
      modelMap.addAttribute("messageText", invitationMessage.getMessage());
    }
    ViewUtil.addViewToModelMap(request, modelMap);
    return "resendinvitation";
  }

  @RequestMapping("vo/{voName}/resendInvitation.shtml")
  public String resendInvitationVO(@PathVariable String voName, ModelMap modelMap, HttpServletRequest request) {
    return resendInvitation(modelMap, request);
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

  @RequestMapping("/vo/{voName}/myinvitations.shtml")
  public String myInvitationsVO(@PathVariable String voName, ModelMap modelMap, HttpServletRequest request) {
    return  myInvitations(modelMap, request);
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
