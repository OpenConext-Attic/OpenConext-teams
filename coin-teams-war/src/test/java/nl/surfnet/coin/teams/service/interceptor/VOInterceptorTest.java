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

import nl.surfnet.coin.teams.control.AbstractControllerTest;
import nl.surfnet.coin.teams.interceptor.VOInterceptor;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class VOInterceptorTest extends AbstractControllerTest {

  private VOInterceptor interceptor = new VOInterceptor();
  private final static String VONAME = "testvo.nl";
  private final static String WRONGVO = "wrongvo.nl";

  @Test
  public void testPreHandle() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(null, "/teams/vo/" + VONAME + "/");
    request.addHeader(VOInterceptor.VO_NAME_HEADER, VONAME);
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, null);
    assertTrue(result);
  }

  @Test
  public void testPreHandleHome() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(null, "/teams/vo/" + VONAME + "/home.shtml");
    request.addHeader(VOInterceptor.VO_NAME_HEADER, VONAME);
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, null);
    assertTrue(result);
  }

  @Test
  public void testPreHandleWrongVO() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(null, "/teams/vo/" + WRONGVO + "/home.shtml");
    request.addHeader(VOInterceptor.VO_NAME_HEADER, VONAME);
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, null);
    assertFalse(result);
  }

  @Test
  public void testPreHandleLoginToVO() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(null, "/teams/vo/" + VONAME + "/home.shtml");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, null);
    assertFalse(result);
  }
}
