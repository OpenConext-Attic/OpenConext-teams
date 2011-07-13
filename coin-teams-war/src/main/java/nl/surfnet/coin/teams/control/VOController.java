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
package nl.surfnet.coin.teams.control;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Controller
public class VOController {

  @RequestMapping("/vo/{voName}")
  public RedirectView start(@PathVariable String voName, ModelMap modelMap, HttpServletRequest request) {
    return new RedirectView(request.getContextPath() + "/vo/" + voName + "/home.shtml");
  }

  @RequestMapping("/vo/{voName}/wrongvo.shtml")
  public String wrongVO(@PathVariable String voName, ModelMap modelMap, HttpServletRequest request) {
    modelMap.addAttribute("requestedVo", voName);
    return "wrongvo";
  }

  @RequestMapping("/vo/{voName}/logintovo.shtml")
  public String loginToVo(@PathVariable String voName, ModelMap modelMap, HttpServletRequest request) {
    modelMap.addAttribute("requestedVo", voName);
    return "logintovo";
  }
}
