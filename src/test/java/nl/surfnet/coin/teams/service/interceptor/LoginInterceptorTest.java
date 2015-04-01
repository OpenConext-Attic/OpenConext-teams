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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import nl.surfnet.coin.teams.domain.MemberAttribute;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.MemberAttributeService;

/**
 * Test for {@link LoginInterceptor}
 */
public class LoginInterceptorTest {

  @Test
  public void testPreHandle() throws Exception {
    String id = "urn:collab:person:surfnet.nl:hansz";



    MemberAttributeService memberAttributeService =
      mock(MemberAttributeService.class);
    when(memberAttributeService.findAttributesForMemberId(
      id)).thenReturn(new ArrayList<MemberAttribute>());
    LoginInterceptor interceptor = new LoginInterceptor("foo", memberAttributeService);


    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("name-id", id);
    request.addHeader("coin-user-status", "member");
    MockHttpServletResponse response = new MockHttpServletResponse();
    boolean loggedIn = interceptor.preHandle(request, response, null);
    assertTrue(loggedIn);
    Assert.assertNotNull(request.getSession().getAttribute("person"));
  }

}
