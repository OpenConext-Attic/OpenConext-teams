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

package teams.interceptor;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;

import teams.domain.Person;
import teams.provision.MockUserDetailsManager;
import teams.provision.UserDetailsManager;
import teams.service.MemberAttributeService;

/**
 * Like the LoginInterceptor but gets the user id from the environment instead
 * of Shibboleth.
 */
public class MockLoginInterceptor extends LoginInterceptor {
  private static final String MOCK_USER_ATTR = "mockUser";
  private static final String MOCK_USER_STATUS = "member";//"guest";

  public MockLoginInterceptor(String teamsUrl, MemberAttributeService memberAttributeService) {
    super(teamsUrl, memberAttributeService, new MockUserDetailsManager(), true);
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // no login required for landingpage, css and js
    if (request.getRequestURI().contains("landingpage.shtml") ||
      request.getRequestURI().contains(".js") ||
      request.getRequestURI().contains(".css") ||
      request.getRequestURI().contains(".png")) {
      return true;
    }

    HttpSession session = request.getSession();
    if (null == session.getAttribute(PERSON_SESSION_KEY) &&
      StringUtils.isBlank(request.getParameter(MOCK_USER_ATTR))) {
      sendLoginHtml(response);
      return false;
    } else if (null == session.getAttribute(PERSON_SESSION_KEY)) {
      //handle mock user
      String userId = request.getParameter(MOCK_USER_ATTR);
      Person person = new Person(userId, userId, userId + "@mockorg.org", "mockorg.org", "urn:collab:org:surf.nl", userId);
      session.setAttribute(PERSON_SESSION_KEY, person);

      //handle guest status
      session.setAttribute(USER_STATUS_SESSION_KEY, MOCK_USER_STATUS);
    }
    return true;
  }

  private void sendLoginHtml(HttpServletResponse response) {
    try (InputStream loginPage = new ClassPathResource("mockLogin.html").getInputStream()) {
      response.setContentType("text/html");
      IOUtils.copy(loginPage, response.getOutputStream());
      response.flushBuffer();
    } catch (IOException e) {
      throw new RuntimeException("Unable to serve the mockLogin.html file", e);
    }
  }
}
