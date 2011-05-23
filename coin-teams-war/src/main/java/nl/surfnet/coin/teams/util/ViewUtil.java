/*
 * Copyright 2011 SURFnet bv
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

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.ModelMap;

/**
 * Util class to define which view should be used (gadget or app)
 */
public final class ViewUtil {
  private static final String VIEW = "view";

  private ViewUtil() {

  }

  /**
   * Defines which view must be presented and adds it to the ModelMap
   *
   * @param request  current {@link HttpServletRequest}
   * @param modelMap {@link ModelMap} on which the view name is added
   */
  public static void addViewToModelMap(HttpServletRequest request,
                                             ModelMap modelMap) {
    modelMap.addAttribute(VIEW, getView(request));
  }

  /**
   * Defines which view must be presented
   *
   * @param request current {@link HttpServletRequest}
   * @return the name of the view
   */
  public static String getView(HttpServletRequest request) {
    String view = request.getParameter(VIEW);

    if (!"gadget".equals(view)) {
      view = "app";
    }

    return view;
  }
}
