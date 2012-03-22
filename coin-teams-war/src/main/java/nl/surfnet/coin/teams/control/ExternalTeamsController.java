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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opensocial.models.Person;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.surfnet.coin.api.client.OpenConextJsonParser;
import nl.surfnet.coin.api.client.domain.Group20;
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
  private static Logger log = LoggerFactory.getLogger(ExternalTeamsController.class);

  @Autowired
  private GroupProviderService groupProviderService;

  private ObjectMapper objectMapper = new ObjectMapper();

  private OpenConextJsonParser parser = new OpenConextJsonParser();

  @RequestMapping("/myproviders.shtml")
  public
  @ResponseBody
  List<GroupProvider> getMyExternalGroupProviders(HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    return groupProviderService.getOAuthGroupProviders(person.getId());
  }

  @RequestMapping("/mygroups.shtml")
  public
  @ResponseBody
  List<Group20> getMyExternalGroups(HttpServletRequest request) {
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);

    List<Group20> group20s = new ArrayList<Group20>();

    // get a list of my group providers that I already have an access token for
    final List<GroupProviderUserOauth> oauthList = groupProviderService.getGroupProviderUserOauths(person.getId());
    for (GroupProviderUserOauth oauth : oauthList) {

      // get the group provider
      final GroupProvider provider = groupProviderService.getGroupProviderByStringIdentifier(oauth.getProvider());

      // we assume now that it's a 3-legged oauth provider
      ThreeLeggedOauth10aGroupProviderApi api = new ThreeLeggedOauth10aGroupProviderApi(provider);
      GroupProviderThreeLeggedOAuth10aService tls = new GroupProviderThreeLeggedOAuth10aService(provider, api);
      final OAuthService oAuthService = tls.getOAuthService();

      Token accessToken = new Token(oauth.getoAuthToken(), oauth.getoAuthSecret());
      String strippedID = personToExternalId(person);
      OAuthRequest oAuthRequest = new OAuthRequest(api.getRequestTokenVerb(),
          MessageFormat.format("{0}/groups/{1}",
              provider.getAllowedOptionAsString(GroupProviderOptionParameters.URL),
              strippedID));
      oAuthService.signRequest(accessToken, oAuthRequest);
      Response oAuthResponse = oAuthRequest.send();

      if (oAuthResponse.isSuccessful()) {
        group20s.addAll(getGroup20sFromResponse(oAuthResponse));
      } else {
        log.info("Fetching external groups for user {} failed with status code {}",
            person.getId(), oAuthResponse.getCode());
        log.trace(oAuthResponse.getBody());
      }

    }
    return group20s;
  }

  private String personToExternalId(Person person) {
    String internalId = person.getId();
    String pattern = "urn:collab:(group|person):(.+):(.+)";
    return internalId.replaceAll(pattern, "$3");
  }

  private List<Group20> getGroup20sFromResponse(Response oAuthResponse) {
    List<Group20> groups = new ArrayList<Group20>();
    String body = oAuthResponse.getBody();
    InputStream in = new ByteArrayInputStream(body.getBytes());

    try {
      final JsonNode jsonNodes = objectMapper.readTree(body);
      if (jsonNodes.has("result")) {
        groups = parser.parseGroup20ResultWrapper(in).getResult().getEntry();
      } else if (jsonNodes.has("entry")) {
        groups = parser.parseGroups20(in).getEntry();
      }
    } catch (RuntimeException e) {
      log.warn("Could not parse oAuthResponse into a List of Group20's", e);
    } catch (JsonProcessingException e) {
      log.warn("Could not parse oAuthResponse as Json", e);
    } catch (IOException e) {
      log.warn("Could not parse oAuthResponse as Json", e);
    } finally {
      try {
        in.close();
      } catch (IOException e) {
        log.warn("Could not close input stream from OAuth response body", e);
      }
    }
    return groups;
  }

}
