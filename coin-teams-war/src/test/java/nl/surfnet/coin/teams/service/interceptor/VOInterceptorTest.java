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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.surfnet.coin.teams.control.AbstractControllerTest;
import nl.surfnet.coin.teams.interceptor.VOInterceptor;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import nl.surfnet.coin.teams.util.VOUtil;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class VOInterceptorTest extends AbstractControllerTest {

  private VOInterceptor interceptor ;
  private final static String VONAME = "testvo.nl";
  private final static String WRONGVO = "wrongvo.nl";
  private final static String VOHEADERVALUE = VONAME;
  private VOUtil voUtil;

  
  @Before
  public void before() {
    interceptor = new VOInterceptor();
    voUtil = new VOUtil();
    TeamEnvironment environment = new TeamEnvironment();
    environment.setVoStemPrefix("nl");
    voUtil.setEnvironment(environment);
    interceptor.setVoUtil(voUtil);
  }
  
  @Test
  public void testPreHandleHappyFlow() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(null,
        "/teams/vo/" + VONAME + "/");
    request.addHeader(VOInterceptor.VO_NAME_HEADER, VOHEADERVALUE);
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, null);
    assertTrue(result);
    String userVo = VOInterceptor.getUserVo();
    assertNull(userVo);
  }

  @Test
  public void testPreHandleHomeWithCorrectStemName() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(null,
        "/teams/vo/" + VONAME + "/home.shtml");
    request.addHeader(VOInterceptor.VO_NAME_HEADER, VOHEADERVALUE);
    MockHttpServletResponse response = new MockHttpServletResponse();

    TeamService teamService = mock(TeamService.class);
    when(teamService.doesStemExists(voUtil.getStemName(VONAME))).thenReturn(Boolean.TRUE);
    interceptor.setTeamService(teamService);
    
    boolean result = interceptor.preHandle(request, response, null);
    
    assertTrue(result);
    
    String userVo = VOInterceptor.getUserVo();
    assertEquals(VONAME, userVo);
  }

 
  @Test
  public void testPreHandleWrongVO() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(null,
        "/teams/vo/" + WRONGVO + "/home.shtml");
    request.addHeader(VOInterceptor.VO_NAME_HEADER, VOHEADERVALUE);
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, null);
    assertFalse(result);
  }

  @Test
  public void testPreHandleLoginToVO() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(null,
        "/teams/vo/" + VONAME + "/home.shtml");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, null);
    assertFalse(result);
  }
}
