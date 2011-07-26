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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;
import nl.surfnet.coin.teams.util.VOUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class VOInterceptor extends HandlerInterceptorAdapter {

  /**
   * Part of the url indicating vo's
   */
  private static final String VO_IDENTIFIER = "vo:";
  private static final Logger logger = LoggerFactory
      .getLogger(VOInterceptor.class);
  private static final ThreadLocal<String> USER_VO = new ThreadLocal<String>();
  public static final String VO_NAME_HEADER = "coin-vo-name";
  /**
   * All the error redirects that we need to let through
   */
  private final static Set<String> BYPASS_LOCATIONS = new HashSet<String>(
      Arrays.asList("wrongvo.shtml", "logintovo.shtml"));

  @Autowired
  private TeamEnvironment teamEnvironment;

  @Autowired
  private TeamService teamService;
  
  @Autowired
  private VOUtil voUtil;

  /**
   * Handle Virtual Organisations
   * <p/>
   * Do a couple of checks: - Is the user requesting the VO he is logged in to?
   * - Is the user logged in to a VO?
   */
  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response, Object handler) throws Exception {

    String url = getRequestedPart(request);
    String[] urlSplit = url.split("/");

    /*
     * Check whether we have a vo request (note that a request with 4 url parts
     * will be redirected to home.shtml and therefore will be picked up the
     * second time
     */
    if (urlSplit.length >= 5 && "vo".equals(urlSplit[2])) {
      // Unprotect the items in bypass
      String urlPart = urlSplit[4];

      if (BYPASS_LOCATIONS.contains(urlPart)) {
        return super.preHandle(request, response, handler);
      } else {
        String loggedInVoName = getLoggedInVOName(request);
        String requestedVoName = getRequestedVOName(request);

        boolean isLoggedInVo = StringUtils.hasText(loggedInVoName);
        boolean isVoRequested = StringUtils.hasText(requestedVoName);

        if (isVoRequested && isLoggedInVo
            && !loggedInVoName.equals(requestedVoName)) {
          /*
           * The user requested another VO than he is logged in for, present an
           * error page
           */
          logger.error("Wrong VO: Requested '{}', but is logged in to '{}'",
              requestedVoName, loggedInVoName);
          response.sendRedirect("wrongvo.shtml");
          return false;
        }
        if (isVoRequested && !isLoggedInVo) {
          logger.error("Not logged in to VO: Requested VO '{}'",
              requestedVoName);
          response.sendRedirect("logintovo.shtml");
          return false;
        }
        if (isLoggedInVo && isVoRequested
            && loggedInVoName.equals(requestedVoName)) {
          String stemName = voUtil.getStemName(requestedVoName);
          if (teamService.doesStemExists(stemName)) {
            USER_VO.set(getLoggedInVOName(request));
          }
          else {
            logger.error("Non existing stem name: '{}'",
                stemName);
            response.sendRedirect("logintovo.shtml");
            return false;
          }
        }
      }
    }
    return super.preHandle(request, response, handler);
  }

  /**
   * Return the in the ThreadLocal stored value for the User's VO
   * 
   * @return {@link ThreadLocal}
   */
  public static String getUserVo() {
    return USER_VO.get();
  }

  /**
   * @param teamEnvironment
   *          the teamEnvironment to set
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
    // this will be something like
    // https://engine.dev.surfconext.nl/authentication/idp/metadata/vo:managementvo
    return request.getHeader(VO_NAME_HEADER);
  }

  private String getRequestedVOName(HttpServletRequest request) {
    String url = getRequestedPart(request);
    String[] urlParts = url.split("/");
    return urlParts.length >= 4 ? urlParts[3] : null;
  }

  /*
   * Return the part of the path of the request
   */
  private String getRequestedPart(HttpServletRequest request) {
    return request.getRequestURI();
  }

  /**
   * @param teamService the teamService to set
   */
  public void setTeamService(TeamService teamService) {
    this.teamService = teamService;
  }

  /**
   * @param voUtil the voUtil to set
   */
  public void setVoUtil(VOUtil voUtil) {
    this.voUtil = voUtil;
  }
}
