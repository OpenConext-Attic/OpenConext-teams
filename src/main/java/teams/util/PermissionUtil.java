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
package teams.util;

import static teams.interceptor.LoginInterceptor.USER_STATUS_SESSION_KEY;

import javax.servlet.http.HttpServletRequest;

public final class PermissionUtil {

  private PermissionUtil() {
  }

  public static boolean isGuest(HttpServletRequest request) {
    String userStatus = (String) request.getSession().getAttribute(USER_STATUS_SESSION_KEY);
    return userStatus == null || "guest".equals(userStatus);
  }

}
