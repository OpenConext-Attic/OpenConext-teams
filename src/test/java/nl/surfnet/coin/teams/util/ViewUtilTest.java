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

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.ui.ModelMap;

/**
 * Test for {@link ViewUtil}
 */
public class ViewUtilTest {
  @Test
  public void testAddViewToModelMap() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);

    ModelMap modelMap = new ModelMap();

    when(request.getParameter("view")).thenReturn(null);
    ViewUtil.addViewToModelMap(request, modelMap);
    assertEquals("app", modelMap.get("view"));

    when(request.getParameter("view")).thenReturn("gadget");
    ViewUtil.addViewToModelMap(request, modelMap);
    assertEquals("gadget", modelMap.get("view"));

    when(request.getParameter("view")).thenReturn("app");
    ViewUtil.addViewToModelMap(request, modelMap);
    assertEquals("app", modelMap.get("view"));
  }

  @Test
  public void testGetView() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getParameter("view")).thenReturn(null);
    assertEquals("app", ViewUtil.getView(request));

    when(request.getParameter("view")).thenReturn("gadget");
    assertEquals("gadget", ViewUtil.getView(request));

    when(request.getParameter("view")).thenReturn("app");
    assertEquals("app", ViewUtil.getView(request));

  }
}
