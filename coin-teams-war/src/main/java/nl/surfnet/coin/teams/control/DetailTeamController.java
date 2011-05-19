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

import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.shared.service.MailService;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.JoinTeamRequestService;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.TeamPersonService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import nl.surfnet.coin.teams.util.ViewUtil;

/**
 * @author steinwelberg
 * 
 *         {@link Controller} that handles the detail team page of a logged in
 *         user.
 */
@Controller
public class DetailTeamController {

  //private static final String MANAGER = "1";
  private static final String ADMIN = "0";
  private static final String ADMIN_LEAVE_TEAM = "error.AdminCannotLeaveTeam";
  private static final String NOT_AUTHORIZED_DELETE_MEMBER = "error.NotAuthorizedToDeleteMember";
  private static final String UTF_8 = "utf-8";
  private static final String TEAM_PARAM = "team";
  private static final String MEMBER_PARAM = "member";
  private static final String ROLE_PARAM = "role";

  private static final int PAGESIZE = 10;


  @Autowired
  private TeamService teamService;

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private JoinTeamRequestService joinTeamRequestService;

  @Autowired
  private TeamPersonService teamPersonService;

  @Autowired
  private TeamEnvironment teamEnvironment;

  @Autowired
  private LocaleResolver localeResolver;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private MailService mailService;
  

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

    modelMap.addAttribute("invitations",
            teamInviteService.findInvitationsForTeam(team));

    int offset = 0;
    String offsetParam = request.getParameter("offset");
    if (StringUtils.hasText(offsetParam)) {
      try {
        offset = Integer.parseInt(offsetParam);
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
    modelMap.addAttribute("offset", offset);
    modelMap.addAttribute("resultset", team.getMembers().size());
    modelMap.addAttribute("pagesize", PAGESIZE);

    modelMap.addAttribute("onlyAdmin", onlyAdmin);
    modelMap.addAttribute(TEAM_PARAM, team);
    modelMap.addAttribute("adminRole", Role.Admin);
    modelMap.addAttribute("managerRole", Role.Manager);
    modelMap.addAttribute("memberRole", Role.Member);
    modelMap.addAttribute("noRole", Role.None);

    modelMap.addAttribute("maxInvitations", teamEnvironment.getMaxInvitations());

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

  private List<Person> getRequesters(Team team) {
    List<JoinTeamRequest> pendingRequests = joinTeamRequestService
        .findPendingRequests(team);
    List<Person> requestingPersons = new ArrayList<Person>(
        pendingRequests.size());
    for (JoinTeamRequest joinTeamRequest : pendingRequests) {
      requestingPersons.add(teamPersonService.getPerson(joinTeamRequest
          .getPersonId()));
    }
    return requestingPersons;
  }

  @RequestMapping(value = "/doleaveteam.shtml", method = RequestMethod.GET)
  public RedirectView leaveTeam(ModelMap modelMap, HttpServletRequest request)
      throws UnsupportedEncodingException {
    String teamId = URLDecoder.decode(request.getParameter(TEAM_PARAM), "utf-8");
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }

    if (team == null) {
      throw new RuntimeException("Parameter error.");
    }

    Set<Member> admins = teamService.findAdmins(team);
    Member[] adminsArray = admins.toArray(new Member[admins.size()]);

    if (admins.size() == 1 && adminsArray[0].getId().equals(personId)) {
      return new RedirectView("detailteam.shtml?team="
          + URLEncoder.encode(teamId, UTF_8) + "&view="
          + ViewUtil.getView(request) + "&mes=" + ADMIN_LEAVE_TEAM);
    }

    // Leave the team
    teamService.deleteMember(teamId, personId);

    return new RedirectView("home.shtml?teams=my&view="
        + ViewUtil.getView(request));
  }

  @RequestMapping(value = "/dodeleteteam.shtml", method = RequestMethod.GET)
  public RedirectView deleteTeam(ModelMap modelMap, HttpServletRequest request)
      throws UnsupportedEncodingException {
    String teamId = URLDecoder.decode(request.getParameter(TEAM_PARAM), UTF_8);
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();

    if (!StringUtils.hasText(teamId)) {
      throw new RuntimeException("Parameter error.");
    }

    Member member = teamService.findMember(teamId, personId);
    if (member.getRoles().contains(Role.Admin)) {
      // Delete the team
      teamService.deleteTeam(teamId);

      return new RedirectView("home.shtml?teams=my&view="
          + ViewUtil.getView(request));
    }

    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, UTF_8) + "&view="
        + ViewUtil.getView(request));
  }

  @RequestMapping(value = "/dodeletemember.shtml", method = RequestMethod.GET)
  public RedirectView deleteMember(ModelMap modelMap, HttpServletRequest request)
      throws UnsupportedEncodingException {
    String teamId = URLDecoder.decode(request.getParameter(TEAM_PARAM), UTF_8);
    String personId = URLDecoder
        .decode(request.getParameter(MEMBER_PARAM), UTF_8);
    Person ownerPerson = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String ownerId = ownerPerson.getId();

    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(personId)) {
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

      return new RedirectView("detailteam.shtml?team="
          + URLEncoder.encode(teamId, UTF_8) + "&view="
          + ViewUtil.getView(request));
      // if the owner is manager and the member is not an admin he can delete
      // the member
    } else if (owner.getRoles().contains(Role.Manager)
        && !member.getRoles().contains(Role.Admin) && !personId.equals(ownerId)) {
      // Delete the member
      teamService.deleteMember(teamId, personId);

      return new RedirectView("detailteam.shtml?team="
          + URLEncoder.encode(teamId, UTF_8) + "&view="
          + ViewUtil.getView(request));
    }

    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, UTF_8) + "&mes="
        + NOT_AUTHORIZED_DELETE_MEMBER  + "&view="
        + ViewUtil.getView(request));
  }

  @RequestMapping(value = "/doaddrole.shtml", method = RequestMethod.POST)
  public @ResponseBody
  String addRole(ModelMap modelMap, HttpServletRequest request)
          throws UnsupportedEncodingException {
    String teamId = request.getParameter(TEAM_PARAM);
    String memberId = request.getParameter(MEMBER_PARAM);
    String roleString = request.getParameter(ROLE_PARAM);

    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(memberId)
        || !StringUtils.hasText(roleString)) {
      return "error";
    }

    Role role = roleString.equals(ADMIN) ? Role.Admin : Role.Manager;

    return teamService.addMemberRole(teamId, memberId, role, false) ? "success"
        : "error";
  }

  @RequestMapping(value = "/doremoverole.shtml", method = RequestMethod.POST)
  public @ResponseBody
  String removeRole(ModelMap modelMap, HttpServletRequest request) {
    String teamId = request.getParameter(TEAM_PARAM);
    String memberId = request.getParameter(MEMBER_PARAM);
    String roleString = request.getParameter(ROLE_PARAM);
    Team team;

    // Some of the parameters weren't correctly filled
    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(memberId)
        || !StringUtils.hasText(roleString)) {
      return "error";
    }

    team = teamService.findTeamById(teamId);

    // is the team null? return error
    if (team == null) {
      return "error";
    }

    // The role admin can only be removed if there are more then one admins in a
    // team.
    if ((roleString.equals(ADMIN) && teamService.findAdmins(team).size() == 1)) {
      return "onlyOneAdmin";
    }

    Role role = roleString.equals(ADMIN) ? Role.Admin : Role.Manager;
    return teamService.removeMemberRole(teamId, memberId, role, false) ? "success"
        : "error";
  }

  @RequestMapping(value = "/dodeleterequest.shtml")
  public RedirectView deleteRequest(ModelMap modelMap, HttpServletRequest request)
          throws UnsupportedEncodingException {
    return doHandleJoinRequest(request, false);
  }


  @RequestMapping(value = "/doapproverequest.shtml")
  public RedirectView approveRequest(ModelMap modelMap, HttpServletRequest request)
          throws UnsupportedEncodingException {
    return doHandleJoinRequest(request, true);
  }

  private RedirectView doHandleJoinRequest(HttpServletRequest request, boolean approve)
          throws UnsupportedEncodingException {
    String teamId = URLDecoder.decode(request.getParameter(TEAM_PARAM), UTF_8);
    String memberId = URLDecoder.decode(request.getParameter(MEMBER_PARAM), UTF_8);

    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(memberId)) {
      throw new RuntimeException("Missing parameters for team or member");
    }

    Team team = teamService.findTeamById(teamId);
    if (team == null) {
      throw new RuntimeException("Cannot find team with id " + teamId);
    }

    Person memberToAdd = teamPersonService.getPerson(memberId);
    if (memberToAdd == null) {
      throw new RuntimeException("Cannot retrieve Person data for id " + memberId);
    }

    Person loggedInPerson = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    Member loggedInMember = teamService.findMember(teamId, loggedInPerson.getId());

    if (!(loggedInMember.getRoles().contains(Role.Admin) ||
            loggedInMember.getRoles().contains(Role.Manager))) {
      return new RedirectView("detailteam.shtml?team="
              + URLEncoder.encode(teamId, UTF_8)
              + "&mes=error.NotAuthorizedForAction"
              + "&view=" + ViewUtil.getView(request));
    }

    if (approve) {
      teamService.addMember(teamId, memberId);
      teamService.addMemberRole(teamId, memberId, Role.Member, true);
    }

    JoinTeamRequest pendingRequest = joinTeamRequestService.findPendingRequest(memberToAdd, team);
    if (pendingRequest != null) {
      joinTeamRequestService.delete(pendingRequest);
    }

    if (!approve) {
      Locale locale = localeResolver.resolveLocale(request);
      sendDeclineMail(memberToAdd, team, locale);
    }

    return new RedirectView("detailteam.shtml?team="
            + URLEncoder.encode(teamId, UTF_8)
            + "&view=" + ViewUtil.getView(request));
  }

  /**
   * Notifies the user that requested to join a team that his request has been declined
   *
   * @param memberToAdd {@link Person} that wanted to join the team
   * @param team        {@link Team} he wanted to join
   * @param locale      {@link Locale}
   */
  private void sendDeclineMail(final Person memberToAdd,
                               final Team team, final Locale locale) {
    String subject = messageSource.getMessage("request.mail.declined.subject", null, locale);
    Object[] bodyValues = {team.getName()};
    String body = messageSource.getMessage("request.mail.declined.body", bodyValues, locale);

    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(teamEnvironment.getSystemEmail());
    mailMessage.setTo(memberToAdd.getEmail());
    mailMessage.setSubject(subject);
    mailMessage.setText(body);

    mailService.sendAsync(mailMessage);
  }


}
