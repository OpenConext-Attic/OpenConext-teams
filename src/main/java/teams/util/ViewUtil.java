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

import static com.google.common.net.UrlEscapers.urlFormParameterEscaper;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.ModelMap;

/**
 * Util class to escape view parameters
 */
public final class ViewUtil {

  private ViewUtil() {
  }

  public static String escapeViewParameters(String viewTemplate, Object... args) {
    Object[] escapedArgs = Arrays.stream(args)
        .map(arg -> arg instanceof String ? urlFormParameterEscaper().escape((String) arg) : arg)
        .toArray(Object[]::new);

    return String.format(viewTemplate, escapedArgs);
  }
}
