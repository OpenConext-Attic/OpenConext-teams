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

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.support.SessionStatus;

/**
 *
 */
public final class TokenUtil {

  private static final int TOKEN_LENGTH = 256;
  public static final String TOKENCHECK = "tokencheck";

  public static String generateSessionToken() {
    return RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH);
  }

  public static void checkTokens(String sessionToken, String token, SessionStatus status) {
    if (StringUtils.length(sessionToken) != TOKEN_LENGTH || !(sessionToken.equals(token))) {
      status.setComplete();
      throw new SecurityException("Token does not match");
    }
  }

}
