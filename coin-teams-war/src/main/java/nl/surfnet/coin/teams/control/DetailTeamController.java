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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.opensocial.service.PersonService;
import nl.surfnet.coin.shared.service.MailService;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Pager;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.JoinTeamRequestService;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import nl.surfnet.coin.teams.util.TokenUtil;
import nl.surfnet.coin.teams.util.ViewUtil;

import org.json.JSONException;
import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author steinwelberg
 * 
 *         {@link Controller} that handles the detail team page of a logged in
 *         user.
 */
@Controller
@SessionAttributes({ TokenUtil.TOKENCHECK })
public class DetailTeamController {

  // private static final String MANAGER = "1";
  private static final String ADMIN = "0";
  private static final String ADMIN_LEAVE_TEAM = "error.AdminCannotLeaveTeam";
  private static final String NOT_AUTHORIZED_DELETE_MEMBER = "error.NotAuthorizedToDeleteMember";
  private static final String UTF_8 = "utf-8";
  private static final String TEAM_PARAM = "team";
  private static final String MEMBER_PARAM = "member";
  private static final String ROLE_PARAM = "role";

  private static final int PAGESIZE = 10;
  private static final String APPLICATION_JSON = "application/json";
  private static final String STATUS = "status";
  private static final String SUCCESS = "success";
  private static final String ERROR = "error";

  @Autowired
  private TeamService teamService;

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private JoinTeamRequestService joinTeamRequestService;

  @Autowired
  @Qualifier("opensocialPersonService")
  private PersonService teamPersonService;

  @Autowired
  private TeamEnvironment teamEnvironment;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private MailService mailService;

  @Autowired
  private ControllerUtil controllerUtil;

  @RequestMapping("/detailteam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request)
      throws IOException {

    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();
    String teamId = URLDecoder.decode(request.getParameter(TEAM_PARAM), UTF_8);
    if (!StringUtils.hasText(teamId)) {
      throw new IllegalArgumentException("Missing parameter for team");
    }
    Set<Role> roles = new HashSet<Role>();
    String message = request.getParameter("mes");

    Team team = teamService.findTeamById(request.getParameter(TEAM_PARAM));

    List<Member> members = team.getMembers();

    // Iterate over the members to get the roles for the logged in user.
    for (Member member : members) {
      if (member.getId().equals(personId)) {
        roles = member.getRoles();
      }
    }

    if (StringUtils.hasText(message)) {
      modelMap.addAttribute("message", message);
    }

    // Check if there is only one admin for a team
    boolean onlyAdmin = teamService.findAdmins(team).size() <= 1;
    modelMap.addAttribute("onlyAdmin", onlyAdmin);

    modelMap.addAttribute("invitations",
        teamInviteService.findInvitationsForTeam(team));

    int offset = getOffset(request);
    Pager membersPager = new Pager(team.getMembers().size(), offset, PAGESIZE);
    modelMap.addAttribute("pager", membersPager);

    modelMap.addAttribute(TEAM_PARAM, team);
    modelMap.addAttribute("adminRole", Role.Admin);
    modelMap.addAttribute("managerRole", Role.Manager);
    modelMap.addAttribute("memberRole", Role.Member);
    modelMap.addAttribute("noRole", Role.None);
    modelMap.addAttribute(TokenUtil.TOKENCHECK,
        TokenUtil.generateSessionToken());

    modelMap
        .addAttribute("maxInvitations", teamEnvironment.getMaxInvitations());

    ViewUtil.addViewToModelMap(request, modelMap);

    if (roles.contains(Role.Admin)) {
      modelMap.addAttribute("pendingRequests", getRequesters(team));
      modelMap.addAttribute(ROLE_PARAM, Role.Admin);
    } else if (roles.contains(Role.Manager)) {
      modelMap.addAttribute("pendingRequests", getRequesters(team));
      modelMap.addAttribute(ROLE_PARAM, Role.Manager);
    } else if (roles.contains(Role.Member)) {
      modelMap.addAttribute(ROLE_PARAM, Role.Member);
    } else {
      modelMap.addAttribute(ROLE_PARAM, Role.None);
    }
    return "detailteam";
  }

  private int getOffset(HttpServletRequest request) {
    int offset = 0;
    String offsetParam = request.getParameter("offset");
    if (StringUtils.hasText(offsetParam)) {
      try {
        offset = Integer.parseInt(offsetParam);
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
    return offset;
  }

  private List<Person> getRequesters(Team team) {
    List<JoinTeamRequest> pendingRequests = joinTeamRequestService
        .findPendingRequests(team);
    List<Person> requestingPersons = new ArrayList<Person>(
        pendingRequests.size());
    for (JoinTeamRequest joinTeamRequest : pendingRequests) {
      requestingPersons.add(teamPersonService.getPerson(
          joinTeamRequest.getPersonId(), LoginInterceptor.getLoggedInUser()));
    }
    return requestingPersons;
  }

  @RequestMapping(value = "/doleaveteam.shtml", method = RequestMethod.POST)
  public RedirectView leaveTeam(ModelMap modelMap, HttpServletRequest request,
      @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
      @RequestParam() String token, SessionStatus status)
      throws UnsupportedEncodingException {
    String teamId = URLDecoder
        .decode(request.getParameter(TEAM_PARAM), "utf-8");
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }

    if (team == null) {
      status.setComplete();
      modelMap.clear();
      throw new RuntimeException("Parameter error.");
    }

    Set<Member> admins = teamService.findAdmins(team);
    Member[] adminsArray = admins.toArray(new Member[admins.size()]);

    if (admins.size() == 1 && adminsArray[0].getId().equals(personId)) {
      status.setComplete();
      return new RedirectView("detailteam.shtml?team="
          + URLEncoder.encode(teamId, UTF_8) + "&view="
          + ViewUtil.getView(request) + "&mes=" + ADMIN_LEAVE_TEAM, false,
          true, false);
    }

    // Leave the team
    teamService.deleteMember(teamId, personId);

    status.setComplete();
    modelMap.clear();
    return new RedirectView("home.shtml?teams=my&view="
        + ViewUtil.getView(request));
  }

  @RequestMapping(value = "/dodeleteteam.shtml", method = RequestMethod.POST)
  public RedirectView deleteTeam(ModelMap modelMap, HttpServletRequest request,
      @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
      @RequestParam() String token, SessionStatus status)
      throws UnsupportedEncodingException {
    TokenUtil.checkTokens(sessionToken, token, status);

    String teamId = URLDecoder.decode(request.getParameter(TEAM_PARAM), UTF_8);
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();

    if (!StringUtils.hasText(teamId)) {
      status.setComplete();
      modelMap.clear();
      throw new RuntimeException("Parameter error.");
    }

    Member member = teamService.findMember(teamId, personId);
    if (member.getRoles().contains(Role.Admin)) {
      // Delete the team
      teamService.deleteTeam(teamId);

      status.setComplete();
      return new RedirectView("home.shtml?teams=my&view="
          + ViewUtil.getView(request), false, true, false);
    }

    status.setComplete();
    modelMap.clear();
    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, UTF_8) + "&view="
        + ViewUtil.getView(request));
  }

  @RequestMapping(value = "/dodeletemember.shtml", method = RequestMethod.GET)
  public RedirectView deleteMember(ModelMap modelMap,
      HttpServletRequest request,
      @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
      @RequestParam() String token, SessionStatus status)
      throws UnsupportedEncodingException {
    TokenUtil.checkTokens(sessionToken, token, status);
    String teamId = URLDecoder.decode(request.getParameter(TEAM_PARAM), UTF_8);
    String personId = URLDecoder.decode(request.getParameter(MEMBER_PARAM),
        UTF_8);
    Person ownerPerson = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String ownerId = ownerPerson.getId();

    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(personId)) {
      status.setComplete();
      modelMap.clear();
      throw new RuntimeException("Parameter error.");
    }

    // fetch the logged in member
    Member owner = teamService.findMember(teamId, ownerId);
    Member member = teamService.findMember(teamId, personId);

    // Check whether the owner is admin and thus is granted to delete the
    // member.
    // Check whether the member that should be deleted is the logged in user.
    // This should not be possible, a logged in user should click the resign
    // from team button.
    if (owner.getRoles().contains(Role.Admin) && !personId.equals(ownerId)) {

      // Delete the member
      teamService.deleteMember(teamId, personId);

      status.setComplete();
      modelMap.clear();
      return new RedirectView("detailteam.shtml?team="
          + URLEncoder.encode(teamId, UTF_8) + "&view="
          + ViewUtil.getView(request));
      // if the owner is manager and the member is not an admin he can delete
      // the member
    } else if (owner.getRoles().contains(Role.Manager)
        && !member.getRoles().contains(Role.Admin) && !personId.equals(ownerId)) {
      // Delete the member
      teamService.deleteMember(teamId, personId);

      status.setComplete();
      modelMap.clear();
      return new RedirectView("detailteam.shtml?team="
          + URLEncoder.encode(teamId, UTF_8) + "&view="
          + ViewUtil.getView(request));
    }

    status.setComplete();
    modelMap.clear();
    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, UTF_8) + "&mes="
        + NOT_AUTHORIZED_DELETE_MEMBER + "&view=" + ViewUtil.getView(request));
  }

  @RequestMapping(value = "/doaddremoverole.shtml", method = RequestMethod.POST)
  public RedirectView addOrRemoveRole(ModelMap modelMap,
      HttpServletRequest request,
      @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
      @RequestParam() String token, SessionStatus status) throws IOException,
      JSONException {
    TokenUtil.checkTokens(sessionToken, token, status);
    String teamId = request.getParameter("teamId");
    String memberId = request.getParameter("memberId");
    String roleString = request.getParameter("roleId");
    int offset = getOffset(request);
    String action = request.getParameter("doAction");
    if (!StringUtils.hasText(teamId)) {
      status.setComplete();
      modelMap.clear();
      return new RedirectView("home.shtml?teams=my" + "&view="
          + ViewUtil.getView(request));
    }
    if (!StringUtils.hasText(memberId) || !StringUtils.hasText(roleString)
        || !validAction(action)) {
      status.setComplete();
      modelMap.clear();
      return new RedirectView("detailteam.shtml?team="
          + URLEncoder.encode(teamId, UTF_8) + "&view="
          + ViewUtil.getView(request) + "&mes=no.role.action" + "&offset="
          + offset);
    }
    String message;
    if (action.equalsIgnoreCase("remove")) {
      Team team = teamService.findTeamById(teamId);
      // is the team null? return error
      if (team == null) {
        status.setComplete();
        modelMap.clear();
        return new RedirectView("home.shtml?teams=my" + "&view="
            + ViewUtil.getView(request));
      }
      message = removeRole(request, teamId, memberId, roleString, team);
    } else {
      message = addRole(request, teamId, memberId, roleString, offset);
    }

    status.setComplete();
    modelMap.clear();
    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, UTF_8) + "&view="
        + ViewUtil.getView(request) + "&mes=" + message + "&offset=" + offset);
  }

  private boolean validAction(String action) {
    return StringUtils.hasText(action)
        && (action.equalsIgnoreCase("remove") || action.equalsIgnoreCase("add"));
  }

  private String removeRole(HttpServletRequest request, String teamId,
      String memberId, String roleString, Team team)
      throws UnsupportedEncodingException {
    // The role admin can only be removed if there are more then one admins in a
    // team.
    if ((roleString.equals(ADMIN) && teamService.findAdmins(team).size() == 1)) {
      return "no.role.added.admin.status";
    }
    Role role = roleString.equals(ADMIN) ? Role.Admin : Role.Manager;
    return (teamService.removeMemberRole(teamId, memberId, role, false) ? "role.removed"
        : "no.role.removed");
  }

  private String addRole(HttpServletRequest request, String teamId,
      String memberId, String roleString, int offset)
      throws UnsupportedEncodingException {
    Role role = roleString.equals(ADMIN) ? Role.Admin : Role.Manager;
    Member other = teamService.findMember(teamId, memberId);
    // Guests may not become admin
    if (other.isGuest() && role == Role.Admin) {
      return "no.role.added.guest.status";
    }
    return (teamService.addMemberRole(teamId, memberId, role, false) ? "role.added"
        : "no.role.added");
  }

  @RequestMapping(value = "/dodeleterequest.shtml", method = RequestMethod.POST)
  public RedirectView deleteRequest(HttpServletRequest request,
      ModelMap modelMap,
      @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
      @RequestParam() String token, SessionStatus status)
      throws UnsupportedEncodingException {
    return doHandleJoinRequest(modelMap, request, sessionToken, token, status,
        false);
  }

  @RequestMapping(value = "/doapproverequest.shtml", method = RequestMethod.POST)
  public RedirectView approveRequest(HttpServletRequest request,
      ModelMap modelMap,
      @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
      @RequestParam() String token, SessionStatus status)
      throws UnsupportedEncodingException {
    return doHandleJoinRequest(modelMap, request, sessionToken, token, status,
        true);
  }

  private RedirectView doHandleJoinRequest(ModelMap modelMap,
      HttpServletRequest request, String sessionToken, String token,
      SessionStatus status, boolean approve)
      throws UnsupportedEncodingException {
    TokenUtil.checkTokens(sessionToken, token, status);
    String teamId = URLDecoder.decode(request.getParameter(TEAM_PARAM), UTF_8);
    String memberId = URLDecoder.decode(request.getParameter(MEMBER_PARAM),
        UTF_8);

    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(memberId)) {
      status.setComplete();
      modelMap.clear();
      throw new RuntimeException("Missing parameters for team or member");
    }

    Team team = teamService.findTeamById(teamId);
    if (team == null) {
      status.setComplete();
      modelMap.clear();
      throw new RuntimeException("Cannot find team with id " + teamId);
    }

    Person personToAddAsMember = teamPersonService.getPerson(memberId,
        LoginInterceptor.getLoggedInUser());
    if (personToAddAsMember == null) {
      status.setComplete();
      modelMap.clear();
      throw new RuntimeException("Cannot retrieve Person data for id "
          + memberId);
    }

    JoinTeamRequest pendingRequest = joinTeamRequestService.findPendingRequest(
        personToAddAsMember, team);

    Person loggedInPerson = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);

    // Check if there is an invitation for this approval request
    if (pendingRequest == null) {
      status.setComplete();
      modelMap.clear();
      throw new RuntimeException("Member (" + loggedInPerson.getId()
          + ") is trying to add a member " + "(" + personToAddAsMember.getId()
          + ") without a membership request");
    }

    // Check if the user has the correct privileges
    if (!controllerUtil.hasUserAdministrativePrivileges(loggedInPerson, teamId)) {
      status.setComplete();
      modelMap.clear();
      return new RedirectView("detailteam.shtml?team="
          + URLEncoder.encode(teamId, UTF_8)
          + "&mes=error.NotAuthorizedForAction" + "&view="
          + ViewUtil.getView(request));
    }

    if (approve) {
      teamService.addMember(teamId, personToAddAsMember);
      teamService.addMemberRole(teamId, memberId, Role.Member, true);
    }

    // Cleanup request
    joinTeamRequestService.delete(pendingRequest);

    if (!approve) {
      Locale locale = localeResolver.resolveLocale(request);
      sendDeclineMail(personToAddAsMember, team, locale);
    }

    status.setComplete();
    modelMap.clear();
    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, UTF_8) + "&view="
        + ViewUtil.getView(request));
  }

  /**
   * Notifies the user that requested to join a team that his request has been
   * declined
   * 
   * @param memberToAdd
   *          {@link Person} that wanted to join the team
   * @param team
   *          {@link Team} he wanted to join
   * @param locale
   *          {@link Locale}
   */
  private void sendDeclineMail(final Person memberToAdd, final Team team,
      final Locale locale) {
    String subject = messageSource.getMessage("request.mail.declined.subject",
        null, locale);
    Object[] bodyValues = { team.getName() };
    String body = messageSource.getMessage("request.mail.declined.body",
        bodyValues, locale);

    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(teamEnvironment.getSystemEmail());
    mailMessage.setTo(memberToAdd.getEmail());
    mailMessage.setSubject(subject);
    mailMessage.setText(body);

    mailService.sendAsync(mailMessage);
  }
}
