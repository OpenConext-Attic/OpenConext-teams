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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import teams.domain.ExternalGroup;
import teams.domain.Person;
import teams.service.VootClient;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static teams.interceptor.LoginInterceptor.EXTERNAL_GROUPS_SESSION_KEY;
import static teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;

@Controller
@RequestMapping("/externalgroups/*")
public class ExternalGroupController {

  private static final Logger LOG = LoggerFactory.getLogger(ExternalGroupController.class);

  @Autowired
  private VootClient vootClient;

  @RequestMapping("/groupdetail.shtml")
  public String groupDetail(@RequestParam String groupId, HttpServletRequest request, ModelMap modelMap) {
    @SuppressWarnings("unchecked")
    List<ExternalGroup> externalGroups = Optional.ofNullable((List<ExternalGroup>) request.getSession().getAttribute(EXTERNAL_GROUPS_SESSION_KEY)).orElseGet(() -> {
      Person person = (Person) request.getSession().getAttribute(PERSON_SESSION_KEY);
      LOG.info("Could not find externalGroups in session, calling voot for {}", person);
      return vootClient.groups(person.getId());
    });

    externalGroups.stream()
      .filter(eg -> eg.getIdentifier().equals(groupId))
      .findFirst()
      .ifPresent(eg -> {
        modelMap.addAttribute("groupProvider", eg.getGroupProvider());
        modelMap.addAttribute("externalGroup", eg);
      });

    return "external-groupdetail";
  }
}
