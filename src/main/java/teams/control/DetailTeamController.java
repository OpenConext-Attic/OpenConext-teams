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

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;
import teams.Application;
import teams.domain.*;
import teams.interceptor.LoginInterceptor;
import teams.service.*;
import teams.util.AuditLog;
import teams.util.ControllerUtil;
import teams.util.TokenUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;
import static teams.util.TokenUtil.*;
import static teams.util.ViewUtil.escapeViewParameters;

/**
 * {@link Controller} that handles the detail team page of a logged in user.
 */
@Controller
@SessionAttributes(TokenUtil.TOKENCHECK)
public class DetailTeamController {

  private static final Logger LOG = LoggerFactory.getLogger(DetailTeamController.class);

  private static final String ADMIN = "0";
  private static final String ADMIN_LEAVE_TEAM = "error.AdminCannotLeaveTeam";
  private static final String NOT_AUTHORIZED_DELETE_MEMBER = "error.NotAuthorizedToDeleteMember";
  protected static final String MEMBER_PARAM = "member";
  protected static final String ROLE_PARAM = "role";
  protected static final String PENDING_REQUESTS_PARAM = "pendingRequests";
  protected static final String INVITATIONS_PARAM = "invitations";

  private static final int PAGESIZE = 10;

  @Autowired
  private VootClient vootClient;

  @Autowired
  private GrouperTeamService grouperTeamService;

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private JoinTeamRequestService joinTeamRequestService;

  @Autowired
  private TeamExternalGroupDao teamExternalGroupDao;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private ControllerUtil controllerUtil;

  @Autowired
  private MessageSource messageSource;

  @Value("${grouperPowerUser}")
  private String grouperPowerUser;

  @Value("${maxInvitations}")
  private Integer maxInvitations;

  @Autowired
  private Environment environment;

  @RequestMapping("/detailteam.shtml")
  public String detailTeam(ModelMap modelMap, HttpServletRequest request, Locale locale,
      @RequestParam("team") String teamId,
      @RequestParam(value = "mes", required = false) String message) throws IOException {

    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    Team team = findTeam(teamId, () -> {});

    Set<Role> roles = team.getMembers().stream()
        .filter(m -> m.getId().equals(person.getId()))
        .findFirst()
        .map(Member::getRoles)
        .orElse(Collections.emptySet());

    if (StringUtils.hasText(message) && messageExists(message, locale)) {
      modelMap.addAttribute("message", message);
    }

    // Check if there is only one admin for a team
    boolean onlyAdmin = grouperTeamService.findAdmins(team).size() <= 1;
    modelMap.addAttribute("onlyAdmin", onlyAdmin);

    modelMap.addAttribute(INVITATIONS_PARAM, teamInviteService.findInvitationsForTeamExcludeAccepted(team));

    int offset = getOffset(request);
    modelMap.addAttribute("pager", new Pager(team.getMembers().size(), offset, PAGESIZE));

    modelMap.addAttribute("team", team);
    modelMap.addAttribute("adminRole", Role.Admin);
    modelMap.addAttribute("managerRole", Role.Manager);
    modelMap.addAttribute("memberRole", Role.Member);
    modelMap.addAttribute("noRole", Role.None);
    modelMap.addAttribute(TOKENCHECK, generateSessionToken());

    modelMap.addAttribute("maxInvitations", maxInvitations);

    if (roles.contains(Role.Admin)) {
      modelMap.addAttribute(PENDING_REQUESTS_PARAM, getRequesters(team));
      modelMap.addAttribute(ROLE_PARAM, Role.Admin);
    } else if (roles.contains(Role.Manager)) {
      modelMap.addAttribute(PENDING_REQUESTS_PARAM, getRequesters(team));
      modelMap.addAttribute(ROLE_PARAM, Role.Manager);
    } else if (roles.contains(Role.Member)) {
      modelMap.addAttribute(ROLE_PARAM, Role.Member);
    } else {
      modelMap.addAttribute(ROLE_PARAM, Role.None);
    }

    if (!Role.None.equals(modelMap.get(ROLE_PARAM))) {
      addLinkedExternalGroupsToModelMap(person.getId(), teamId, modelMap);
    }
    modelMap.addAttribute("groupzyEnabled", environment.acceptsProfiles(Application.GROUPZY_PROFILE_NAME));

    return "detailteam";
  }

  private boolean messageExists(String message, Locale locale) {
    try {
      messageSource.getMessage(message, new Object[] {}, locale);
      return true;
    } catch (NoSuchMessageException e) {
      return false;
    }
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
    List<JoinTeamRequest> pendingRequests = joinTeamRequestService.findPendingRequests(team.getId());

    return pendingRequests.stream()
        .map(request -> new Person(request.getPersonId(), null, request.getEmail(), null, null, request.getDisplayName())).collect(Collectors.toList());
  }

  private void addLinkedExternalGroupsToModelMap(String personId, String teamId, ModelMap modelMap) {
    List<TeamExternalGroup> teamExternalGroups = teamExternalGroupDao.getByTeamIdentifier(teamId);
    if (!teamExternalGroups.isEmpty()) {
      List<ExternalGroup> groups = vootClient.groups(personId);
      Map<String, ExternalGroupProvider> groupProviderMap = new HashMap<>();
      for (ExternalGroup group : groups) {
        groupProviderMap.put(group.getGroupProviderIdentifier(), group.getGroupProvider());
      }
      modelMap.addAttribute("groupProviderMap", groupProviderMap);
      modelMap.addAttribute("teamExternalGroups", teamExternalGroups);
    }
  }

  @RequestMapping(value = "/doleaveteam.shtml", method = RequestMethod.POST)
  public RedirectView leaveTeam(ModelMap modelMap, HttpServletRequest request,
                                @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                @RequestParam String token, @RequestParam("team") String teamId, SessionStatus status) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();

    Runnable endingRequest = () -> {
      status.setComplete();
      modelMap.clear();
    };

    Team team = findTeam(teamId, endingRequest);

    Set<Member> admins = grouperTeamService.findAdmins(team);

    if (admins.size() == 1 && admins.iterator().next().getId().equals(personId)) {
      status.setComplete();
      return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s&mes=%s", teamId, ADMIN_LEAVE_TEAM), false, true, false);
    }

    grouperTeamService.deleteMember(team, personId);
    AuditLog.log("User {} left team {}", personId, teamId);

    endingRequest.run();

    return new RedirectView("home.shtml?teams=my");
  }

  @RequestMapping(value = "/dodeleteteam.shtml", method = RequestMethod.POST)
  public RedirectView deleteTeam(ModelMap modelMap, HttpServletRequest request,
                                 @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                 @RequestParam String token, @RequestParam("team") String teamId, SessionStatus status) throws UnsupportedEncodingException {
    checkTokens(sessionToken, token, status);

    Runnable endingRequest = () -> {
      status.setComplete();
      modelMap.clear();
    };

    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();

    validateArgument(teamId, endingRequest);

    Team team = grouperTeamService.findTeamById(teamId);

    Member member = grouperTeamService.findMember(team, personId);
    if (member.getRoles().contains(Role.Admin)) {
      // Delete the team
      List<Invitation> invitationsForTeam = teamInviteService.findAllInvitationsForTeam(team);
      for (Invitation invitation : invitationsForTeam) {
        teamInviteService.delete(invitation);
      }
      List<TeamExternalGroup> teamExternalGroups = teamExternalGroupDao.getByTeamIdentifier(teamId);
      for (TeamExternalGroup teamExternalGroup : teamExternalGroups) {
        teamExternalGroupDao.delete(teamExternalGroup);
      }
      grouperTeamService.deleteTeam(teamId);

      AuditLog.log("User {} deleted team {}", personId, teamId);

      status.setComplete();

      return new RedirectView("home.shtml?teams=my", false, true, false);
    }

    endingRequest.run();

    return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s", teamId));
  }

  @RequestMapping(value = "/dodeletemember.shtml", method = RequestMethod.GET)
  public RedirectView deleteMember(ModelMap modelMap,
                                   HttpServletRequest request,
                                   @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                   @RequestParam String token, @RequestParam("team") String teamId, SessionStatus status) throws UnsupportedEncodingException {
    checkTokens(sessionToken, token, status);

    String personId = decode(request.getParameter(MEMBER_PARAM), UTF_8.name());
    Person ownerPerson = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    String ownerId = ownerPerson.getId();

    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(personId)) {
      status.setComplete();
      modelMap.clear();
      throw new RuntimeException("Parameter error.");
    }
    Team team = grouperTeamService.findTeamById(teamId);
    // fetch the logged in member
    Member owner = grouperTeamService.findMember(team, ownerId);
    Member member = grouperTeamService.findMember(team, personId);

    // Check whether the owner is admin and thus is granted to delete the
    // member.
    // Check whether the member that should be deleted is the logged in user.
    // This should not be possible, a logged in user should click the resign
    // from team button.
    if (owner.getRoles().contains(Role.Admin) && !personId.equals(ownerId)) {
      grouperTeamService.deleteMember(team, personId);
      AuditLog.log("Admin user {} deleted user {} from team {}", ownerId, personId, teamId);

      status.setComplete();
      modelMap.clear();

      return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s", teamId));

      // if the owner is manager and the member is not an admin he can delete the member
    } else if (owner.getRoles().contains(Role.Manager) && !member.getRoles().contains(Role.Admin) && !personId.equals(ownerId)) {
      grouperTeamService.deleteMember(team, personId);
      AuditLog.log("Manager user {} deleted user {} from team {}", ownerId, personId, teamId);

      status.setComplete();
      modelMap.clear();

      return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s", teamId));
    }

    status.setComplete();
    modelMap.clear();

    return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s&mes=%s", teamId, NOT_AUTHORIZED_DELETE_MEMBER));
  }

  @RequestMapping(value = "/doaddremoverole.shtml", method = RequestMethod.POST)
  public RedirectView addOrRemoveRole(ModelMap modelMap, HttpServletRequest request,
                                      @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                      @RequestParam String token, SessionStatus status) throws IOException {
    checkTokens(sessionToken, token, status);

    String teamId = request.getParameter("teamId");
    String memberId = request.getParameter("memberId");
    String roleString = request.getParameter("roleId");
    int offset = getOffset(request);
    String action = request.getParameter("doAction");

    if (!StringUtils.hasText(teamId)) {
      status.setComplete();
      modelMap.clear();
      return new RedirectView("home.shtml?teams=my");
    }

    if (!StringUtils.hasText(memberId) || !StringUtils.hasText(roleString) || !validAction(action)) {
      status.setComplete();
      modelMap.clear();
      return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s&mes=no.role.action&offset=%s", teamId, offset));
    }

    Person person = (Person) request.getSession().getAttribute(PERSON_SESSION_KEY);
    Team team = grouperTeamService.findTeamById(teamId);
    if (team == null) {
      status.setComplete();
      modelMap.clear();
      return new RedirectView("home.shtml?teams=my");
    }
    String message;
    if (action.equalsIgnoreCase("remove")) {
      // is the team null? return error
      message = removeRole(team, memberId, roleString, person.getId());
    } else {
      message = addRole(team, memberId, roleString, person.getId());
    }

    status.setComplete();
    modelMap.clear();

    return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s&mes=%s&offset=%d", teamId, message, offset));
  }

  private boolean validAction(String action) {
    return StringUtils.hasText(action) && (action.equalsIgnoreCase("remove") || action.equalsIgnoreCase("add"));
  }

  private String removeRole(Team team, String memberId, String roleString, String loggedInUserId) {
    // The role admin can only be removed if there are more then one admins in a team.
    if (roleString.equals(ADMIN) && grouperTeamService.findAdmins(team).size() == 1) {
      return "no.role.added.admin.status";
    }
    Role role = roleString.equals(ADMIN) ? Role.Admin : Role.Manager;
    if (grouperTeamService.removeMemberRole(team, memberId, role, loggedInUserId)) {
      AuditLog.log("User {} removed role {} from user {} in team {}", loggedInUserId, role, memberId, team.getId());
      return "role.removed";
    } else {
      return "no.role.removed";
    }
  }

  private String addRole(Team team, String memberId, String roleString, String loggedInUserId) {
    Role role = roleString.equals(ADMIN) ? Role.Admin : Role.Manager;
    Member other = grouperTeamService.findMember(team, memberId);
    // Guests may not become admin
    if (other.isGuest() && role == Role.Admin) {
      return "no.role.added.guest.status";
    }

    if (grouperTeamService.addMemberRole(team, memberId, role, loggedInUserId)) {
      AuditLog.log("User {} added role {} to user {} in team {}", loggedInUserId, role, memberId, team.getId());
      return "role.added";
    } else {
      return "no.role.added";
    }
  }

  @RequestMapping(value = "/dodeleterequest.shtml", method = RequestMethod.POST)
  public RedirectView deleteJoinRequest(HttpServletRequest request, ModelMap modelMap,
                                    @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                    @RequestParam String token, @RequestParam("team") String teamId, SessionStatus status) throws UnsupportedEncodingException {
    return doHandleJoinRequest(modelMap, request, sessionToken, token, teamId, status, false);
  }

  @RequestMapping(value = "/doapproverequest.shtml", method = RequestMethod.POST)
  public RedirectView approveJoinRequest(HttpServletRequest request, ModelMap modelMap,
                                     @ModelAttribute(TokenUtil.TOKENCHECK) String sessionToken,
                                     @RequestParam() String token, @RequestParam("team") String teamId, SessionStatus status) throws UnsupportedEncodingException {
    return doHandleJoinRequest(modelMap, request, sessionToken, token, teamId, status, true);
  }

  private RedirectView doHandleJoinRequest(ModelMap modelMap,
                                           HttpServletRequest request, String sessionToken, String token, String teamId,
                                           SessionStatus status, boolean approve) throws UnsupportedEncodingException {
    checkTokens(sessionToken, token, status);

    Runnable endingRequest = () -> {
      status.setComplete();
      modelMap.clear();
    };

    String memberId = decode(request.getParameter(MEMBER_PARAM), UTF_8.name());
    Team team = findTeam(teamId, endingRequest);
    JoinTeamRequest pendingRequest = findJoinTeamRequest(memberId, team, endingRequest);

    Person loggedInPerson = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);

    // Check if the user has the correct privileges
    if (!controllerUtil.hasUserAdministrativePrivileges(loggedInPerson, team)) {
      endingRequest.run();

      return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s&mes=error.NotAuthorizedForAction", teamId));
    }

    Person requester = new Person(pendingRequest.getPersonId(), null, pendingRequest.getEmail(), null, null, pendingRequest.getDisplayName());

    if (approve) {
      grouperTeamService.addMember(team, requester);
      grouperTeamService.addMemberRole(team, memberId, Role.Member, grouperPowerUser);
      AuditLog.log("User {} approved join-team-request of user {} in team {}", loggedInPerson.getId(), requester.getId(), teamId);
    }

    joinTeamRequestService.delete(pendingRequest);

    AuditLog.log("Deleted join-team-request for user {} in team {}", pendingRequest.getPersonId(), teamId);

    trySendEmailToRequester(approve, requester, team, localeResolver.resolveLocale(request));

    endingRequest.run();

    return new RedirectView(escapeViewParameters("detailteam.shtml?team=%s", teamId));
  }


  private void validateArgument(String argument, Runnable argumentMissing) {
    if (Strings.isNullOrEmpty(argument)) {
      argumentMissing.run();
      throw new RuntimeException("Missing parameters for team or member");
    }
  }

  private Team findTeam(String teamId, Runnable missing) {
    validateArgument(teamId, missing);

    Optional<Team> team = Optional.ofNullable(grouperTeamService.findTeamById(teamId));

    if (!team.isPresent()) {
      missing.run();
    }

    return team.orElseThrow(() -> new RuntimeException("Cannot find team with id " + teamId));
  }

  private JoinTeamRequest findJoinTeamRequest(String memberId, Team team, Runnable missing) {
    validateArgument(memberId, missing);

    Optional<JoinTeamRequest> pendingRequest = Optional.ofNullable(joinTeamRequestService.findPendingRequest(memberId, team.getId()));

    if (!pendingRequest.isPresent()) {
      missing.run();
    }

    return pendingRequest.orElseThrow(() -> new RuntimeException(String.format("Could not find join team request for %s", memberId)));
  }

  private void trySendEmailToRequester(boolean approve, Person person, Team team, Locale locale) {
    if (Strings.isNullOrEmpty(person.getEmail())) {
      LOG.debug("Could not send email, because {} has no email", person.getName());
      return;
    }

    if (approve) {
      controllerUtil.sendAcceptMail(person, team, locale);
    } else {
      controllerUtil.sendDeclineMail(person, team, locale);
    }
  }

}
