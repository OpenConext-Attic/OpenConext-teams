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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;

import teams.domain.Person;
import teams.provision.MockUserDetailsManager;
import teams.provision.UserDetailsManager;
import teams.repository.PersonRepository;
import teams.service.MemberAttributeService;

/**
 * Like the LoginInterceptor but gets the user id from the environment instead
 * of Shibboleth.
 */
public class MockLoginInterceptor extends LoginInterceptor {

  private static final String MOCK_USER_ATTR = "mockUser";
  private static final boolean MOCK_USER_STATUS_IS_GUEST = false;

  public MockLoginInterceptor(String teamsURL, PersonRepository personRepository) {
    super(teamsURL, personRepository);
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // no login required for landingpage, css and js
    if (request.getRequestURI().contains("landingpage.shtml") ||
      request.getRequestURI().contains(".js") ||
      request.getRequestURI().contains(".css") ||
      request.getRequestURI().contains(".png") ||
      request.getRequestURI().contains("migrate")) {
      return true;
    }

    HttpSession session = request.getSession();
    MockLoginInterceptor.SetHeader wrapper = new MockLoginInterceptor.SetHeader(request);

    Person person = (Person) session.getAttribute(PERSON_SESSION_KEY);

    String userNameParameter = request.getParameter(MOCK_USER_ATTR);
    if (null == person && StringUtils.isBlank(userNameParameter)) {
      sendLoginHtml(response);
      return false;
    } else if (null == person) {
      //handle mock user
      wrapper.setHeader("name-id", userNameParameter);
      wrapper.setHeader("uid", userNameParameter);
      wrapper.setHeader("Shib-InetOrgPerson-mail", UUID.randomUUID().toString() + "@example.org");
      wrapper.setHeader("schacHomeOrganization", "example.com");
      wrapper.setHeader("displayName", "John Doe");
      wrapper.setHeader("is-member-of", "urn:collab:org:surf.nl");

    } else {
      wrapper.setHeader("name-id", person.getId());
    }
    return super.preHandle(wrapper, response, handler);

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

  private static class SetHeader extends HttpServletRequestWrapper {

    private final HashMap<String, String> headers;

    public SetHeader(HttpServletRequest request) {
      super(request);
      this.headers = new HashMap<>();
    }

    public void setHeader(String name, String value) {
      this.headers.put(name, value);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
      List<String> names = Collections.list(super.getHeaderNames());
      names.addAll(headers.keySet());
      return Collections.enumeration(names);
    }

    @Override
    public String getHeader(String name) {
      if (headers.containsKey(name)) {
        return headers.get(name);
      }
      return super.getHeader(name);
    }
  }

}
