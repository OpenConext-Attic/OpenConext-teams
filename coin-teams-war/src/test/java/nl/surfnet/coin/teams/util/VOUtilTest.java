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

package nl.surfnet.coin.teams.util;

import nl.surfnet.coin.teams.control.AbstractControllerTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class VOUtilTest extends AbstractControllerTest {

  private final static String VONAME = "testvo.nl";
  private final static String DEFAULTSTEM = "nl:surfnet:diensten";
  private final static String VOSTEMPREFIX = "nl:surfnet:vo";

  /**
   * Mock the getVOName.
   *
   * @throws Exception
   */
  //@Test
  //TODO fix test. Mock Static method VOInterceptor.getUserVo Otherwise the outcome of this test is not predictable!
  public void testGetVoName() throws Exception {

    VOUtil voUtil = new VOUtil();

    TeamEnvironment environment = mock(TeamEnvironment.class);
    when(environment.getVoStemPrefix()).thenReturn(VOSTEMPREFIX);
    when(environment.getDefaultStemName()).thenReturn(DEFAULTSTEM);
    autoWireMock(voUtil, environment, TeamEnvironment.class);

    String stemName = voUtil.getStemName(getRequest());
    assertEquals("nl:surfnet:diensten", stemName);
  }
}
