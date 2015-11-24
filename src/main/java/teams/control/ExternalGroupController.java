/*
 * Copyright 2012 SURFnet bv, The Netherlands
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

package teams.control;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import teams.domain.ExternalGroup;
import teams.interceptor.LoginInterceptor;
import teams.util.ViewUtil;

/**
 * Controller for external teams
 */
@Controller
@RequestMapping("/externalgroups/*")
public class ExternalGroupController {

  @RequestMapping("/groupdetail.shtml")
  public String groupDetail(@RequestParam String groupId, HttpServletRequest request, ModelMap modelMap) {
    @SuppressWarnings("unchecked")
    List<ExternalGroup> groups = (List<ExternalGroup>) request.getSession().getAttribute(LoginInterceptor.EXTERNAL_GROUPS_SESSION_KEY);

    groups.stream()
      .filter(eg -> eg.getIdentifier().equals(groupId))
      .findFirst()
      .ifPresent(eg -> {
        modelMap.addAttribute("groupProvider", eg.getGroupProvider());
        modelMap.addAttribute("externalGroup", eg);
      });

    ViewUtil.addViewToModelMap(request, modelMap);

    return "external-groupdetail";
  }
}
