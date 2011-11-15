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

package nl.surfnet.coin.teams.interceptor;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.MemberAttribute;
import nl.surfnet.coin.teams.service.MemberAttributeService;
import nl.surfnet.coin.teams.service.TeamPersonService;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import org.opensocial.models.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Intercepts calls to controllers to handle Single Sign On details from
 * Shibboleth and sets a Person object on the session when the user is logged
 * in.
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {

  private static final String GADGET = "gadget";
  public static final String PERSON_SESSION_KEY = "person";
  public static final String USER_STATUS_SESSION_KEY = "userStatus";
  private static final List<String> LOGIN_BYPASS = createLoginBypass();
  private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
  private static final String STATUS_GUEST = "guest";
  private static final String STATUS_MEMBER = "member";

  @Autowired
  private TeamEnvironment teamEnvironment;

  @Autowired
  private TeamPersonService personService;

  @Autowired
  private MemberAttributeService memberAttributeService;

  /*
   * Return the part of the path of the request
   */
  private String getRequestedPart(HttpServletRequest request) {
    return request.getRequestURI();
  }

  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response, Object handler) throws Exception {

    HttpSession session = request.getSession();

    String remoteUser = getRemoteUser(request);

    // Check session first:
    Person person = (Person) session.getAttribute(PERSON_SESSION_KEY);
    if (person == null || !person.getId().equals(remoteUser)) {

      if (StringUtils.hasText(remoteUser)) {
        person = personService.getPerson(remoteUser, remoteUser);
        // Add person to session:
        session.setAttribute(PERSON_SESSION_KEY, person);

        if (person == null) {
          String errorMessage = "Cannot find user: " + remoteUser;
          throw new ServletException(errorMessage);
        }
        handleGuestStatus(session, person);

      } else {
        // User is not logged in, and REMOTE_USER header is empty.
        // Check whether the user is requesting the landing page, if not
        // redirect him to the landing page.
        String url = getRequestedPart(request);
        String[] urlSplit = url.split("/");

        String view = request.getParameter("view");

        // Unprotect the items in bypass
        String urlPart = urlSplit[2];

        logger.trace("Request for '{}'", request.getRequestURI());
        logger.debug("urlPart: '{}'", urlPart);
        logger.trace("view '{}'", view);

        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";

        if (LOGIN_BYPASS.contains(urlPart)) {
          logger.trace("Bypassing", urlPart);
          return super.preHandle(request, response, handler);
        } else if (GADGET.equals(view)
                || "acceptInvitation.shtml".equals(urlPart)
                || "detailteam.shtml".equals(urlPart)) {
          logger.trace("Going to shibboleth");
          response.sendRedirect("/Shibboleth.sso/Login?target="
                  + request.getRequestURL()
                  + URLEncoder.encode(queryString, "utf-8"));
          return false;
          // If user is requesting SURFteams for a VO redirect to Federation Login
        } else {
          // Send redirect to landingpage if gadget is not requested in app view.
          logger.trace("Redirect to landingpage");
          response.sendRedirect(teamEnvironment.getTeamsURL() + "/landingpage.shtml");
          return false;
        }
      }
    }
    return super.preHandle(request, response, handler);
  }

  /**
   * Defines if the stored guest status matches the guest status from EngineBlock
   *
   * @param session {@link javax.servlet.http.HttpSession}
   * @param person  {@link org.opensocial.models.Person}
   */
  void handleGuestStatus(HttpSession session, Person person) {
    Member member = new Member(null, person);
    final List<MemberAttribute> memberAttributes =
            memberAttributeService.findAttributesForMemberId(member.getId());
    member.setMemberAttributes(memberAttributes);

    if (member.isGuest() != person.isGuest()) {
      member.setGuest(person.isGuest());
      memberAttributeService.saveOrUpdate(member.getMemberAttributes());
    }

    // Add the user status to the session
    String userStatus = person.isGuest() ? STATUS_GUEST : STATUS_MEMBER;
    session.setAttribute(USER_STATUS_SESSION_KEY, userStatus);
  }

  /**
   * Hook for subclasses to override the shibboleth default behaviour
   *
   * @param request
   *          the httpRequest
   * @return the String of the logged in user
   */
  protected String getRemoteUser(HttpServletRequest request) {
    return request.getHeader("REMOTE_USER");
  }

  /**
   * @param teamEnvironment
   *          the teamEnvironment to set
   */
  public void setTeamEnvironment(TeamEnvironment teamEnvironment) {
    this.teamEnvironment = teamEnvironment;
  }

  /**
   * @return the teamEnvironment
   */
  protected TeamEnvironment getTeamEnvironment() {
    return teamEnvironment;
  }

  /**
   * @return the loggedinuser
   */
  public static String getLoggedInUser() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    Person person = (Person) requestAttributes.getAttribute(PERSON_SESSION_KEY, RequestAttributes.SCOPE_SESSION);
    return person.getId();
  }

  /**
   * Set the User VO
   *
   * @param personService
   */
  public void setPersonService(TeamPersonService personService) {
    this.personService = personService;
  }

  public void setMemberAttributeService(MemberAttributeService memberAttributeService) {
    this.memberAttributeService = memberAttributeService;
  }

  /**
   * @return {@link List} of url parts to bypass authentication
   */
  private static List<String> createLoginBypass() {
    List<String> bypass = new ArrayList<String>();
    bypass.add("landingpage.shtml");
    bypass.add("js");
    bypass.add("css");
    bypass.add("media");
    bypass.add("teams.xml");
    bypass.add("declineInvitation.shtml");
    return bypass;
  }
}
