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

package nl.surfnet.coin.teams.service.impl;

import java.io.IOException;

import org.opensocial.Client;
import org.opensocial.Request;
import org.opensocial.RequestException;
import org.opensocial.auth.AuthScheme;
import org.opensocial.auth.OAuth2LeggedScheme;
import org.opensocial.models.Person;
import org.opensocial.providers.Provider;
import org.opensocial.providers.ShindigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.TeamPersonService;
import nl.surfnet.coin.teams.util.TeamEnvironment;

/**
Implementation of {@link TeamPersonService}
 */
@Component("teamPersonService")
public class TeamPersonServiceImpl implements TeamPersonService {
  private static final String REST_TEMPLATE = "people/{guid}/{selector}/{pid}";

  protected static final String SELF = "@self";

  @Autowired
  private TeamEnvironment environment;

  /**
   * {@inheritDoc}
   */
  @Override
  public Person getPerson(String userId,String loggedInUser) {
    Request request = new Request(REST_TEMPLATE, "people.get", "GET");
    request.setModelClass(Person.class);
    request.setSelector(SELF);
    request.setGuid(userId);
    try {
      return getClient(loggedInUser).send(request).getEntry();
    } catch (RequestException e) {
      throw new IllegalArgumentException(
        "Unable to retrieve the person with uid: '" + userId + "'", e);
    } catch (IOException e) {
      throw new IllegalArgumentException(
        "Unable to retrieve the person with uid: '" + userId + "'", e);
    }
  }

  /*
   * We can't store state, because the scheme and therefore the client are bound
   * to the Id of the person
   */
  private Client getClient(String loggedInUser) {
    Provider provider = new ShindigProvider(true);

    provider.setRestEndpoint(environment.getOpenSocialUrl() + "/rest/");
    provider.setRpcEndpoint(null);
    provider.setVersion("0.9");

    AuthScheme scheme = new OAuth2LeggedScheme(environment.getOauthKey(),
      environment.getOauthSecret(), loggedInUser);
    return new Client(provider, scheme);
  }

  /**
   * @param environment the environment to set
   */
  public void setEnvironment(TeamEnvironment environment) {
    this.environment = environment;
  }
}
