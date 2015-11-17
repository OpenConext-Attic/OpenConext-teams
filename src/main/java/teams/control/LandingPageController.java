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

/**
 *
 */
package teams.control;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import teams.interceptor.LoginInterceptor;
import teams.util.ViewUtil;

@Controller
public class LandingPageController {

  @Value("${teamsURL}")
  private String teamsUrl;

  @RequestMapping(value = "/landingpage.shtml", method = RequestMethod.GET)
  public String start(ModelMap modelMap, HttpServletRequest request) {
    ViewUtil.addViewToModelMap(request, modelMap);

    modelMap.addAttribute("teamsUrl", teamsUrl);

    return "landingpage";
  }

  @RequestMapping(value = "/landingpage.shtml", method = RequestMethod.POST)
  public void storeCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(LoginInterceptor.TEAMS_COOKIE, "skipLanding=true");
    cookie.setMaxAge(Integer.MAX_VALUE);
    response.addCookie(cookie);
  }
}
