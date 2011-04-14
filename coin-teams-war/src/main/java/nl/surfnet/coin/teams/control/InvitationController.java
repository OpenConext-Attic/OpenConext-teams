package nl.surfnet.coin.teams.control;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.teams.domain.Invitation;
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
public class InvitationController {

  @Autowired
  private TeamInviteService teamInviteService;

  @Autowired
  private TeamService teamService;

  /**
   * RequestMapping to accept an invitation. If everything is okay,
   * it redirects to your new team detail view.
   *
   * @param request {@link HttpServletRequest}
   * @return detail view of your new team
   * @throws UnsupportedEncodingException if the server does not support
   *                                      utf-8
   */
  @RequestMapping(value = "acceptInvitation.shtml")
  public RedirectView accept(HttpServletRequest request)
          throws UnsupportedEncodingException {
    teamInviteService.cleanupExpiredInvitations();
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    if (person == null) {
      return new RedirectView("landingpage.shtml");
    }

    Invitation invitation = getInvitationByRequest(request);

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
            + URLEncoder.encode(teamId, "utf-8")
            + "&view=" + ViewUtil.getView(request));
  }

  /**
   * RequestMapping to decline an invitation.
   *
   * @param request {@link HttpServletRequest}
   * @return if everything is ok, go to home
   */
  @RequestMapping(value = "/declineInvitation.shtml")
  public RedirectView decline(HttpServletRequest request) {
    teamInviteService.cleanupExpiredInvitations();
    Person person = (Person) request.getSession().getAttribute(
            LoginInterceptor.PERSON_SESSION_KEY);
    if (person == null) {
      return new RedirectView("landingpage.shtml");
    }
    Invitation invitation = getInvitationByRequest(request);
    invitation.setDeclined(true);
    teamInviteService.saveOrUpdate(invitation);

    return new RedirectView("home.shtml?teams=my&view="
            + ViewUtil.getView(request));
  }

  private Invitation getInvitationByRequest(HttpServletRequest request) {
    String invitationId = request.getParameter("id");

    if (!StringUtils.hasText(invitationId)) {
      throw new IllegalArgumentException(
              "Missing parameter to identify the invitation");
    }

    Invitation invitation = teamInviteService.findInvitationByInviteId(invitationId);
    if (invitation == null) {
      throw new IllegalArgumentException(
              "Cannot find your invitation. They expire after 14 days.");
    }
    return invitation;
  }

}
