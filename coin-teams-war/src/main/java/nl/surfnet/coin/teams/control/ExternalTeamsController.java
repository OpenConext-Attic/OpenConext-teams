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

package nl.surfnet.coin.teams.control;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opensocial.models.Person;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.surfnet.coin.api.client.OpenConextJsonParser;
import nl.surfnet.coin.api.client.domain.Group20;
import nl.surfnet.coin.api.client.domain.Group20Entry;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.GroupProviderUserOauth;
import nl.surfnet.coin.teams.domain.ThreeLeggedOauth10aGroupProviderApi;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.GroupProviderService;
import nl.surfnet.coin.teams.service.impl.GroupProviderThreeLeggedOAuth10aService;
import nl.surfnet.coin.teams.util.GroupProviderOptionParameters;

/**
 * Controller for external teams
 */
@Controller
@RequestMapping("/externalgroups/*")
public class ExternalTeamsController {

  @Autowired
  private GroupProviderService groupProviderService;

  private OpenConextJsonParser parser = new OpenConextJsonParser();

  @RequestMapping("/myproviders.shtml")
  public
  @ResponseBody
  List<GroupProvider> getMyExternalGroupProviders(HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    return groupProviderService.getGroupProviders(person.getId());
  }

  @RequestMapping("/mygroups.shtml")
  public
  @ResponseBody
  List<Group20> getMyExternalGroups(HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);

    List<Group20> group20s = new ArrayList<Group20>();

    final List<GroupProviderUserOauth> oauthList = groupProviderService.getGroupProviderUserOauths(person.getId());
    for (GroupProviderUserOauth oauth : oauthList) {
      final GroupProvider provider = groupProviderService.getGroupProviderByStringIdentifier(oauth.getProvider());

      ThreeLeggedOauth10aGroupProviderApi api = new ThreeLeggedOauth10aGroupProviderApi(provider);
      GroupProviderThreeLeggedOAuth10aService tls = new GroupProviderThreeLeggedOAuth10aService(provider, api);
      final OAuthService oAuthService = tls.getOAuthService();

      Token accessToken = new Token(oauth.getoAuthToken(), oauth.getoAuthSecret());
      final String strippedID = personToExternalId(person);
      OAuthRequest oAuthRequest = new OAuthRequest(api.getRequestTokenVerb(),
          MessageFormat.format("{0}/groups/{1}",
              provider.getAllowedOptionAsString(GroupProviderOptionParameters.URL),
              strippedID));
      oAuthService.signRequest(accessToken, oAuthRequest);
      Response oAuthResponse = oAuthRequest.send();
      if (oAuthResponse.isSuccessful()) {
        InputStream in = oAuthResponse.getStream();
        final Group20Entry group20Entry = parser.parseGroups20(in);
        group20s.addAll(group20Entry.getEntry());
      } else {
        System.out.println(oAuthResponse.getCode());
        System.out.println(oAuthResponse.getBody());
      }
    }
    return group20s;
  }

  private String personToExternalId(Person person) {
    String internalId = person.getId();
    String pattern = "urn:collab:(group|person):(.+):(.+)";
    return internalId.replaceAll(pattern, "$3");
  }

}
