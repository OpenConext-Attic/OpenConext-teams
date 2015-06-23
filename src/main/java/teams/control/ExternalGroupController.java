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

import teams.domain.ExternalGroup;
import teams.domain.Person;
import teams.interceptor.LoginInterceptor;
import teams.service.VootClient;
import teams.util.ViewUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Controller for external teams
 */
@Controller
@RequestMapping("/externalgroups/*")
public class ExternalGroupController {

  private static final int PAGESIZE = 10;

  @Autowired
  private VootClient vootClient;

  @RequestMapping("/groupdetail.shtml")
  public String groupDetail(@RequestParam String groupId, HttpServletRequest request, ModelMap modelMap) {
    Person person = (Person) request.getSession().getAttribute(LoginInterceptor.PERSON_SESSION_KEY);
    List<ExternalGroup> groups = (List<ExternalGroup>) request.getSession().getAttribute(LoginInterceptor.EXTERNAL_GROUPS_SESSION_KEY);

    for (ExternalGroup externalGroup: groups) {
      if (externalGroup.getIdentifier().equals(groupId)) {
        modelMap.addAttribute("groupProvider", externalGroup.getGroupProvider());
        modelMap.addAttribute("externalGroup", externalGroup);
        break;
      }
    }

    ViewUtil.addViewToModelMap(request, modelMap);

    return "external-groupdetail";
  }
}
