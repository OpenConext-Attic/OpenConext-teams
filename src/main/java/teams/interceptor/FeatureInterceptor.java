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

package teams.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Interceptor to enable/disable (new) features
 */
public class FeatureInterceptor extends HandlerInterceptorAdapter {

  private final boolean displayExternalTeams;
  private final boolean displayExternalTeamMembers;
  private final boolean displayAddExternalGroupToTeam;
  private final String commitId;
  private final String applicationVersion;

  public FeatureInterceptor(boolean displayExternalTeams, boolean displayExternalTeamMembers, boolean displayAddExternalGroupToTeam, String commitId, String applicationVersion) {
    this.displayExternalTeams = displayExternalTeams;
    this.displayExternalTeamMembers = displayExternalTeamMembers;
    this.displayAddExternalGroupToTeam = displayAddExternalGroupToTeam;
    this.commitId = commitId;
    this.applicationVersion = applicationVersion;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response,
                         Object handler, ModelAndView modelAndView) throws Exception {
    if (modelAndView != null) {
      ModelMap map = modelAndView.getModelMap();
      map.addAttribute("commitId", commitId);
      map.addAttribute("applicationVersion", applicationVersion);
      map.addAttribute("displayExternalTeams", displayExternalTeams);
      map.addAttribute("displayExternalTeamMembers", displayExternalTeamMembers);
      map.addAttribute("displayAddExternalGroupToTeam", displayAddExternalGroupToTeam);
    }
    super.postHandle(request, response, handler, modelAndView);
  }

}
