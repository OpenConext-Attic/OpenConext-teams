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
package teams.interceptor;

import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import teams.domain.Member;
import teams.domain.MemberAttribute;
import teams.domain.Person;
import teams.migration.MigrationService;
import teams.provision.UserDetailsManager;
import teams.repository.PersonRepository;
import teams.service.MemberAttributeService;
import teams.util.AuditLog;

/**
 * Intercepts calls to controllers to handle Single Sign On details from
 * Shibboleth and sets a Person object on the session when the user is logged in.
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {

  private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

  public static final String PERSON_SESSION_KEY = "person";
  public static final String EXTERNAL_GROUPS_SESSION_KEY = "externalGroupsSessionKey";
  public static final String USER_STATUS_SESSION_KEY = "userStatus";
  private static final List<String> LOGIN_BYPASS = Arrays.asList("landingpage.shtml", "js", "css", "media", "teams.xml", "declineInvitation.shtml", "migrate");
  private static final List<String> LANDING_BYPASS = Arrays.asList("acceptInvitation.shtml", "migrate");

  public static final String STATUS_GUEST = "guest";
  public static final String STATUS_MEMBER = "member";
  public static final String TEAMS_COOKIE = "SURFconextTeams";
  public static final String NOT_PROVIDED_SAML_ATTRIBUTES_SHTML = "/NotProvidedSamlAttributes.shtml";
  public static final String API_VOOT_URL = "api/voot";

  private final String teamsUrl;
  private final PersonRepository personRepository;

  public LoginInterceptor(String teamsURL, PersonRepository personRepository) {
    this.teamsUrl = teamsURL;
    this.personRepository = personRepository;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (request.getRequestURI().endsWith(NOT_PROVIDED_SAML_ATTRIBUTES_SHTML)) {
      return super.preHandle(request, response, handler);
    }
    HttpSession session = request.getSession();

    String nameId = request.getHeader("name-id");

    // Check session first:
    Person person = (Person) session.getAttribute(PERSON_SESSION_KEY);
    if (person == null || !person.getId().equals(nameId)) {

      if (StringUtils.hasText(nameId)) {
        Optional<Person> optionalPersonFromHeaders = constructPerson(request);
        if (optionalPersonFromHeaders.isPresent()) {
          person = optionalPersonFromHeaders.get();
          Optional<teams.migration.Person> optionalPersonFromDatabase = personRepository.findByUrn(person.getId());
          if (optionalPersonFromDatabase.isPresent()) {
            teams.migration.Person personFromDatabase = optionalPersonFromDatabase.get();
            if (person.isGuest() != personFromDatabase.isGuest()) {
              personFromDatabase.setGuest(person.isGuest());
              if (personFromDatabase.getEmail().equals(MigrationService.UNKNOWN)) {
                personFromDatabase.setEmail(person.getEmail());
                personFromDatabase.setName(person.getName());
              }
              personRepository.save(personFromDatabase);
            }
          } else {
            teams.migration.Person newPerson = new teams.migration.Person(person.getId(), person.getName(), person.getEmail(), person.isGuest(), Instant.now());
            personRepository.save(newPerson);
          }
        } else {
          response.sendRedirect(teamsUrl + NOT_PROVIDED_SAML_ATTRIBUTES_SHTML);
          return false;
        }
        // Add person to session:
        session.setAttribute(PERSON_SESSION_KEY, person);

        AuditLog.log("Login by user {}", person.getId());

        String userStatus = person.isGuest() ? STATUS_GUEST : STATUS_MEMBER;
        session.setAttribute(USER_STATUS_SESSION_KEY, userStatus);

      } else {
        // User is not logged in, and name-id header is empty.
        // Check whether the user is requesting the landing page, if not
        // redirect him to the landing page.
        String url = request.getRequestURI();
        String[] urlSplit = url.split("/");

        String urlPart = urlSplit.length < 1 ? "/" : urlSplit[1];

        logger.debug("Request for '{}'", request.getRequestURI());
        logger.debug("urlPart: '{}'", urlPart);

        if (LOGIN_BYPASS.contains(urlPart) || isApiCall(request.getRequestURI())) {
          logger.debug("Bypassing {}", urlPart);
          return super.preHandle(request, response, handler);
        } else if (getTeamsCookie(request).contains("skipLanding") || LANDING_BYPASS.contains(urlPart)) {
          String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
          String target = URLEncoder.encode(request.getRequestURL().toString().replace("http://","https://") + queryString, "UTF-8");
          response.sendRedirect(teamsUrl + "/Shibboleth.sso/Login?target=" + target);
          return false;
        } else {
          logger.debug("Redirect to landingpage");
          response.sendRedirect(teamsUrl + "/landingpage.shtml");
          return false;
        }
      }
    }

    return super.preHandle(request, response, handler);
  }

  protected boolean isApiCall(String requestURI) {
    return requestURI.contains(API_VOOT_URL);
  }

  private Optional<Person> constructPerson(HttpServletRequest request) {
    List<String> notProvidedSamlAttributes = new ArrayList<>();

    String id = request.getHeader("name-id");
    addNotProvidedSamlAttributes(id, notProvidedSamlAttributes, "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified" );

    String name = request.getHeader("uid");
    addNotProvidedSamlAttributes(name, notProvidedSamlAttributes, "urn:mace:dir:attribute-def:uid" );

    String email = request.getHeader("Shib-InetOrgPerson-mail");
    addNotProvidedSamlAttributes(email, notProvidedSamlAttributes, "urn:mace:dir:attribute-def:mail" );

    String schacHomeOrganization = request.getHeader("schacHomeOrganization");
    addNotProvidedSamlAttributes(schacHomeOrganization, notProvidedSamlAttributes, "urn:mace:terena.org:attribute-def:schacHomeOrganization" );

    String displayName = request.getHeader("displayName");
    addNotProvidedSamlAttributes(displayName, notProvidedSamlAttributes, "urn:mace:dir:attribute-def:displayName");

    String status = request.getHeader("is-member-of");

    if (!notProvidedSamlAttributes.isEmpty()) {
      request.getSession(true).setAttribute("notProvidedSamlAttributes", notProvidedSamlAttributes);
    }
    return notProvidedSamlAttributes.isEmpty() ? Optional.of(new Person(id, name, email, schacHomeOrganization, status, displayName)) : Optional.empty();
  }

  private void addNotProvidedSamlAttributes(String requiredSamlAttribute, List<String> notProvidedSamlAttributes, String attributeName) {
    if (!StringUtils.hasText(requiredSamlAttribute)) {
      notProvidedSamlAttributes.add(attributeName);
    }
  }

  private String getTeamsCookie(HttpServletRequest request) {
    return Optional.ofNullable(request.getCookies()).flatMap(cookies -> Arrays.stream(cookies)
      .filter(c -> c.getName().equals(TEAMS_COOKIE))
      .map(Cookie::getValue)
      .findFirst()
    ).orElse("");
  }

}
