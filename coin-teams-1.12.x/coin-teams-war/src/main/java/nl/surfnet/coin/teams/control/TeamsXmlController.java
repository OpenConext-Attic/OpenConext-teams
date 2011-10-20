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
package nl.surfnet.coin.teams.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import nl.surfnet.coin.teams.util.TeamEnvironment;

/**
 * @author steinwelberg
 *
 */
@Controller
public class TeamsXmlController {
  
  @Autowired
  private TeamEnvironment teamEnvironment;

  @RequestMapping("/teams.xml")
  public String start(ModelMap modelMap, HttpServletRequest request) {

    modelMap.addAttribute("teamsURL", teamEnvironment.getTeamsURL());
    modelMap.addAttribute("groupNameContext", teamEnvironment.getGroupNameContext());

    return "teams";
  }
  
}
