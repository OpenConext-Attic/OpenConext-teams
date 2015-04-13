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

package nl.surfnet.coin.teams.control;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

@Controller
public class JSController {

  private static final String JS_BASEPATH = "js/coin-teams/";
  private static final List<String> SCRIPTS = ImmutableList.of("jquery/plugins.js", "init.js", "core.js", "sandbox.js",
    "modules/home.js", "modules/teamoverview.js", "modules/addteam.js", "modules/editteam.js", "modules/jointeam.js",
    "modules/addmember.js", "modules/acceptinvitation.js", "modules/detailteam.js", "modules/addallowedserviceproviders.js"
  );

  private String jsToSend;

  public JSController() {

    StringBuffer stringBuffer = new StringBuffer();
    SCRIPTS.forEach(script -> {
      URL url = Resources.getResource(JS_BASEPATH + script);
      String text = null;
      try {
        text = Resources.toString(url, Charsets.UTF_8);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      stringBuffer.append(text);
    });
    jsToSend = stringBuffer.toString();
  }


  @RequestMapping(value = "/js/coin-teams.js")
  public void js(HttpServletResponse response) throws IOException {
    response.setContentType("application/javascript");
    response.getWriter().println(jsToSend);

  }

}
