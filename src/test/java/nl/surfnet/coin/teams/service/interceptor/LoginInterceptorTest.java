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

package nl.surfnet.coin.teams.service.interceptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.MemberAttributeService;

/**
 * Test for {@link LoginInterceptor}
 */
public class LoginInterceptorTest {

  private String id = "urn:collab:person:surfnet.nl:hansz";
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private LoginInterceptor interceptor;

  @Before
  public void before() throws Exception {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    MemberAttributeService memberAttributeService =
      mock(MemberAttributeService.class);
    when(memberAttributeService.findAttributesForMemberId(
      id)).thenReturn(new ArrayList<>());
    interceptor = new LoginInterceptor("foo", memberAttributeService);
  }

  @Test
  public void testPreHandleHappyFlow() throws Exception {
    request.addHeader("name-id", id);
    request.addHeader("coin-user-status", "member");
    request.addHeader("uid", "John Doe");
    request.addHeader("Shib-InetOrgPerson-mail", "john@example.com");

    boolean loggedIn = interceptor.preHandle(request, response, null);
    assertTrue(loggedIn);
    Assert.assertNotNull(request.getSession().getAttribute("person"));
  }

  @Test
  public void testPreHandleRequiredSamlAttributeMissing() throws Exception {
    request.addHeader("name-id", id);
    boolean loggedIn = interceptor.preHandle(request, response, null);
    assertFalse(loggedIn);

    @SuppressWarnings("unchecked")
    List<String> notProvidedSamlAttributes = (List<String>) request.getSession().getAttribute("notProvidedSamlAttributes");

    assertEquals(Arrays.asList("urn:mace:dir:attribute-def:uid", "urn:mace:dir:attribute-def:mail"), notProvidedSamlAttributes);
  }
}
