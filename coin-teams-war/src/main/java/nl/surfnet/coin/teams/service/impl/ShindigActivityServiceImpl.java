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

/**
 * 
 */
package nl.surfnet.coin.teams.service.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.opensocial.Client;
import org.opensocial.Request;
import org.opensocial.RequestException;
import org.opensocial.auth.AuthScheme;
import org.opensocial.auth.OAuth2LeggedScheme;
import org.opensocial.models.Activity;
import org.opensocial.providers.Provider;
import org.opensocial.providers.ShindigProvider;
import org.opensocial.services.ActivitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.surfnet.coin.teams.service.ShindigActivityService;
import nl.surfnet.coin.teams.util.TeamEnvironment;

/**
 * @author steinwelberg
 *
 */
@Service("shindigActivityService")
public class ShindigActivityServiceImpl implements ShindigActivityService {
  
  @Autowired 
  private TeamEnvironment environment;
  
  private Logger logger = Logger.getLogger(ShindigActivityServiceImpl.class);
  

  /* (non-Javadoc)
   * @see nl.surfnet.coin.teams.service.ShindigActivityService#addActivity(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void addActivity(String personId, String teamId, String title, String body) {
    
    logger.info("Adding activity to the portal for team ('" + teamId + "')");
    
    Provider provider = new ShindigProvider();
    
    provider.setRestEndpoint(environment.getRestEndpoint());
    provider.setRpcEndpoint(environment.getRpcEndpoint());
    
    AuthScheme scheme = new OAuth2LeggedScheme(environment.getOauthKey(), environment.getOauthSecret(), personId);

    Client client = new Client(provider, scheme);
    
    Activity activity = new Activity();
    activity.setTitle(title);
    activity.setBody(body);
    
    Request request = ActivitiesService.createActivity(activity);
    // Add the teamId as the groupId to the activity.
    request.setGroupId(teamId);
    request.setAppId(environment.getAppId());
    
    try {
      client.send(request);
    } catch (RequestException e) {
      logger.error("RequestException while adding activity for team ('" + teamId + "') and person ('" + personId + "')" , e);
    } catch (IOException e) {
      logger.error("IOException while adding activity for team ('" + teamId + "') and person ('" + personId + "')" , e);
    }
  }

}
