package nl.surfnet.coin.teams.control;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.ViewUtil;

/**
 * {@link Controller} that handles the accept/decline of an Invitation
 */
@Controller
@SessionAttributes("invitation")
public class InvitationController {

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private TeamService teamService;

  /**
   * RequestMapping to show the accept invitation page.
   *
   * @param modelMap {@link ModelMap}
   * @param request
   *          {@link HttpServletRequest}
   * @return accept invitation page
   */
  @RequestMapping(value = "acceptInvitation.shtml")
  public String accept(ModelMap modelMap, HttpServletRequest request) {
    Invitation invitation = getInvitationByRequest(request);

    String teamId = invitation.getTeamId();
    if (!StringUtils.hasText(teamId)) {
      throw new RuntimeException("Invalid invitation");
    }
    Team team = teamService.findTeamById(teamId);
    if (team == null) {
      throw new RuntimeException("Invalid invitation");
    }

    modelMap.addAttribute("invitation", invitation);
    modelMap.addAttribute("team", team);
    modelMap.addAttribute("date", new Date(invitation.getTimestamp()));

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
  @RequestMapping(value = "doAcceptInvitation.shtml")
  public RedirectView doAccept(HttpServletRequest request)
      throws UnsupportedEncodingException {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);

    Invitation invitation = getInvitationByRequest(request);
    if (invitation == null) {
      throw new IllegalArgumentException(
          "Cannot find your invitation. Invitations expire after 14 days.");
    }

    String teamId = invitation.getTeamId();
    if (!StringUtils.hasText(teamId)) {
      throw new RuntimeException("Invalid invitation");
    }
    Team team = teamService.findTeamById(teamId);
    if (team == null) {
      throw new RuntimeException("Invalid invitation");
    }

    String memberId = person.getId();
    teamService.addMember(teamId, memberId);
    teamService.addMemberRole(teamId, memberId, Role.Member, true);

    teamInviteService.delete(invitation);

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
  @RequestMapping(value = "declineInvitation.shtml")
  public String decline(ModelMap modelMap,
                        HttpServletRequest request) {
    String viewTemplate = "invitationdeclined";

    Invitation invitation = getInvitationByRequest(request);

    boolean found = (invitation != null);
    if (found) {
      invitation.setDeclined(true);
      teamInviteService.saveOrUpdate(invitation);
    }

    modelMap.addAttribute("result", found);
    return viewTemplate;
  }

  /**
   * RequestMapping to delete an invitation as admin
   *
   * @param modelMap
   *          {@link ModelMap}
   * @param request
   *          {@link HttpServletRequest}
   * @return redirect to detailteam if everything is okay
   * @throws UnsupportedEncodingException
   *           in the rare condition utf-8 is not supported
   */
  @RequestMapping(value = "deleteInvitation.shtml")
  public RedirectView deleteInvitation(ModelMap modelMap,
      HttpServletRequest request) throws UnsupportedEncodingException {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    if (person == null) {
      return new RedirectView("landingpage.shtml");
    }
    Invitation invitation = getInvitationByRequest(request);
    String teamId = invitation.getTeamId();
    teamInviteService.delete(invitation);

    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8") + "&view="
        + ViewUtil.getView(request));
  }

  @RequestMapping("resendInvitation.shtml")
  public String resendInvitation(ModelMap modelMap, HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    Invitation invitation = getInvitationByRequest(request);
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

    ViewUtil.addViewToModelMap(request, modelMap);
    return "resendinvitation";
  }

  private Invitation getInvitationByRequest(HttpServletRequest request) {
    String invitationId = request.getParameter("id");

    if (!StringUtils.hasText(invitationId)) {
      throw new IllegalArgumentException(
          "Missing parameter to identify the invitation");
    }

    return teamInviteService.findInvitationByInviteId(invitationId);
  }

}
