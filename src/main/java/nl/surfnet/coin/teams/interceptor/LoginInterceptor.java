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

package nl.surfnet.coin.teams.interceptor;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.MemberAttribute;
import nl.surfnet.coin.teams.domain.Person;
import nl.surfnet.coin.teams.service.MemberAttributeService;
import nl.surfnet.coin.teams.util.AuditLog;

/**
 * Intercepts calls to controllers to handle Single Sign On details from
 * Shibboleth and sets a Person object on the session when the user is logged
 * in.
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {

  public static final String PERSON_SESSION_KEY = "person";
  public static final String EXTERNAL_GROUPS_SESSION_KEY = "externalGroupsSessionKey";
  public static final String USER_STATUS_SESSION_KEY = "userStatus";
  private static final List<String> LOGIN_BYPASS = Arrays.asList("landingpage.shtml", "js", "css", "media", "teams.xml", "declineInvitation.shtml");
  private static final List<String> LANDING_BYPASS = Arrays.asList("acceptInvitation.shtml");

  private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
  private static final String STATUS_GUEST = "guest";
  private static final String STATUS_MEMBER = "member";
  public static final String TEAMS_COOKIE = "SURFconextTeams";

  private final String teamsUrl;

  private final MemberAttributeService memberAttributeService;

  public LoginInterceptor(String teamsUrl, MemberAttributeService memberAttributeService) {
    this.teamsUrl = teamsUrl;
    this.memberAttributeService = memberAttributeService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler) throws Exception {

    HttpSession session = request.getSession();

    String nameId = request.getHeader("name-id");

    // Check session first:
    Person person = (Person) session.getAttribute(PERSON_SESSION_KEY);
    if (person == null || !person.getId().equals(nameId)) {

      if (StringUtils.hasText(nameId)) {
        person = constructPerson(request);
        // Add person to session:
        session.setAttribute(PERSON_SESSION_KEY, person);

        AuditLog.log("Login by user {}", person.getId());
        handleGuestStatus(session, person);

      } else {
        // User is not logged in, and name-id header is empty.
        // Check whether the user is requesting the landing page, if not
        // redirect him to the landing page.
        String url = request.getRequestURI();
        String[] urlSplit = url.split("/");

        String view = request.getParameter("view");

        String urlPart = urlSplit.length < 1 ? "/" : urlSplit[1];

        logger.debug("Request for '{}'", request.getRequestURI());
        logger.debug("urlPart: '{}'", urlPart);
        logger.debug("view '{}'", view);

        if (LOGIN_BYPASS.contains(urlPart)) {
          logger.debug("Bypassing {}", urlPart);
          return super.preHandle(request, response, handler);
        } else {
          if (getTeamsCookie(request).contains("skipLanding") || LANDING_BYPASS.contains(urlPart)) {
            String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
            response.sendRedirect(teamsUrl + "/Shibboleth.sso/Login?target="
              + request.getRequestURL()
              + queryString);
            return false;
          } else {
            // Send redirect to landingpage if gadget is not requested in app view.
            logger.debug("Redirect to landingpage");
            response.sendRedirect(teamsUrl + "/landingpage.shtml");
            return false;
          }
        }
      }
    }
    return super.preHandle(request, response, handler);
  }

  private Person constructPerson(HttpServletRequest request) {
    String id = request.getHeader("name-id");
    String name = request.getHeader("uid");
    String email = request.getHeader("Shib-InetOrgPerson-mail");
    String schacHomeOrganization = request.getHeader("schacHomeOrganization");
    String status = request.getHeader("coin-user-status");
    String displayName = request.getHeader("displayName");
    return new Person(id, name, email, schacHomeOrganization, status, displayName);
  }

  private String getTeamsCookie(HttpServletRequest request) {
    String result = "";
    Cookie[] cookies = request.getCookies();
    if (null != cookies) {
      for (Cookie current : cookies) {
        if (current.getName().equals(TEAMS_COOKIE)) {
          result = current.getValue();
          break;
        }
      }
    }
    return result;
  }

  /**
   * Defines if the stored guest status matches the guest status from EngineBlock
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

}
