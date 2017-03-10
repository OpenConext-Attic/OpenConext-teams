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

package teams.service.interceptor;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import teams.domain.MemberAttribute;
import teams.domain.Person;
import teams.interceptor.LoginInterceptor;
import teams.provision.MockUserDetailsManager;
import teams.repository.PersonRepository;
import teams.service.MemberAttributeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static teams.domain.MemberAttribute.ATTRIBUTE_GUEST;
import static teams.interceptor.LoginInterceptor.*;

/**
 * Test for {@link LoginInterceptor}
 */
public class LoginInterceptorTest {

  private String id = "urn:collab:person:surfnet.nl:hansz";
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private LoginInterceptor interceptor;
  private PersonRepository personRepository;

  @Before
  public void before() throws Exception {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    personRepository =
      mock(PersonRepository.class);
    when(personRepository.findByUrn(anyString())).thenReturn(Optional.empty());
    interceptor = new LoginInterceptor("foo", personRepository);
  }

  @Test
  public void testPreHandleHappyFlow() throws Exception {
    request.addHeader("name-id", id);
    request.addHeader("is-member-of", "urn:collab:org:surf.nl");
    request.addHeader("uid", "John Doe");
    request.addHeader("Shib-InetOrgPerson-mail", "john@example.com");
    request.addHeader("schacHomeOrganization", "example.com");
    request.addHeader("displayName", "John Doe");

    boolean loggedIn = interceptor.preHandle(request, response, null);
    assertTrue(loggedIn);
    Person person = (Person) request.getSession().getAttribute("person");
    assertNotNull(person);
    assertFalse(person.isGuest());
  }

  @Test
  public void testPreHandleRequiredSamlAttributeMissing() throws Exception {
    request.addHeader("name-id", id);
    boolean loggedIn = interceptor.preHandle(request, response, null);
    assertFalse(loggedIn);

    @SuppressWarnings("unchecked")
    List<String> notProvidedSamlAttributes = (List<String>) request.getSession().getAttribute("notProvidedSamlAttributes");

    assertEquals(asList(
      "urn:mace:dir:attribute-def:uid",
      "urn:mace:dir:attribute-def:mail",
      "urn:mace:terena.org:attribute-def:schacHomeOrganization",
      "urn:mace:dir:attribute-def:displayName"),
      notProvidedSamlAttributes);
  }


}
