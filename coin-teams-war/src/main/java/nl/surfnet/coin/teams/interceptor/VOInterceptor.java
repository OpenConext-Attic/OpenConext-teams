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

package nl.surfnet.coin.teams.interceptor;

import nl.surfnet.coin.teams.util.TeamEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class VOInterceptor extends HandlerInterceptorAdapter {

  private static final Logger logger = LoggerFactory.getLogger(VOInterceptor.class);
  private static final ThreadLocal<String> userVo = new ThreadLocal<String>();

  @Autowired
  private TeamEnvironment teamEnvironment;

  /**
   * Handle Virtual Organisations
   * <p/>
   * Do a couple of checks:
   * - Is the user requesting the VO he is logged in to?
   * - Is the user logged in to a VO?
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    String url = getRequestedPart(request);
    String[] urlSplit = url.split("/");

    // Check whether we have a vo request
    if (urlSplit.length >= 5 && "vo".equals(urlSplit[2])) {

      // Unprotect the items in bypass
      String urlPart = urlSplit[4];

      if (createVOBypass().contains(urlPart)) {
        return super.preHandle(request, response, handler);
      } else {
        String loggedInVoName = getLoggedInVOName(request);
        String requestedVoName = getRequestedVOName(request);

        // Check whether the VO that the user requests is equal to the VO he is logged in to
        if (loggedInVoName != null && StringUtils.hasText(loggedInVoName)) {
          if (requestedVoName != null && StringUtils.hasText(requestedVoName)
                  && !loggedInVoName.equals(requestedVoName)) {

            // The user requested another VO than he is logged in for, present an error page
            logger.error("Wrong VO: Requested '{}', but is logged in to '{}'", requestedVoName, loggedInVoName);
            response.sendRedirect("wrongvo.shtml");
            return false;
          }
        } else if (loggedInVoName == null || !StringUtils.hasText(loggedInVoName)
                && requestedVoName != null && StringUtils.hasText(requestedVoName)) {
          logger.error("Not logged in to VO: Requested VO '{}'", requestedVoName);
          response.sendRedirect("logintovo.shtml");
          return false;
        }
      }
      userVo.set(getLoggedInVOName(request));
    }
    return super.preHandle(request, response, handler);
  }

  /**
   * Return the in the ThreadLocal stored value for the User's VO
   *
   * @return {@link ThreadLocal}
   */
  public static String getUserVo() {
    return userVo.get();
  }

  /**
   * @param teamEnvironment the teamEnvironment to set
   */
  public void setTeamEnvironment(TeamEnvironment teamEnvironment) {
    this.teamEnvironment = teamEnvironment;
  }

  /**
   * @return the teamEnvironment
   */
  protected TeamEnvironment getTeamEnvironment() {
    return teamEnvironment;
  }

  String getLoggedInVOName(HttpServletRequest request) {
    return request.getHeader("vo-name");
  }

  private String getRequestedVOName(HttpServletRequest request) {
    String url = getRequestedPart(request);
    String[] urlParts = url.split("/");
    return urlParts.length >= 4 ? urlParts[3] : null;
  }

  private static List<String> createVOBypass() {
    List<String> bypass = new ArrayList<String>();
    bypass.add("wrongvo.shtml");
    bypass.add("logintovo.shtml");
    return bypass;
  }

  /*
   * Return the part of the path of the request
   */
  private String getRequestedPart(HttpServletRequest request) {
    return request.getRequestURI();
  }
}
