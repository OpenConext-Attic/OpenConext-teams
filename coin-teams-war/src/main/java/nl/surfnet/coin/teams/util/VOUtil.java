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

package nl.surfnet.coin.teams.util;

import nl.surfnet.coin.teams.interceptor.VOInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Component("voUtil")
public class VOUtil {

  @Autowired
  private TeamEnvironment environment;

  /**
   * Returns the stem name for this request.
   * If a user is logged in to a VO stem name is returned
   *
   * @param request {@link javax.servlet.http.HttpServletRequest}
   * @return the stem name on the session or
   *         {@literal null} if there is no stem
   */
  public String getStemName(final HttpServletRequest request) {
    String voName = VOInterceptor.getUserVo();

    // Check whether a VO is requested, if not always return the default stem
    if (isVoRequested(request) && StringUtils.hasText(voName)) {
      String voPrefix = environment.getVoStemPrefix();
      voPrefix = voPrefix.endsWith(":") ? voPrefix : voPrefix + ":";
      return voPrefix + voName;
    }
    return environment.getDefaultStemName();
  }

  /**
   * Check whether a VO is requested in the request
   *
   * @param request {@link HttpServletRequest}
   * @return {@link Boolean} true if a VO is requested, false if no VO is requested
   */
  public boolean isVoRequested(final HttpServletRequest request) {
    String[] url = request.getRequestURI().split("/");

    for (String s : url) {
      if ("vo".equalsIgnoreCase(s)) {
        return true;
      }
    }
    return false;
  }
}
