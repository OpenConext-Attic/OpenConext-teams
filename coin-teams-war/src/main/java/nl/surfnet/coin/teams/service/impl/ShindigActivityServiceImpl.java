/**
 * 
 */
package nl.surfnet.coin.teams.service.impl;

import java.io.IOException;

import nl.surfnet.coin.teams.service.ShindigActivityService;
import nl.surfnet.coin.teams.util.TeamEnvironment;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opensocial.Client;
import org.opensocial.Request;
import org.opensocial.RequestException;
import org.opensocial.Response;
import org.opensocial.auth.AuthScheme;
import org.opensocial.auth.OAuth2LeggedScheme;
import org.opensocial.models.Activity;
import org.opensocial.providers.Provider;
import org.opensocial.providers.ShindigProvider;
import org.opensocial.services.ActivitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
      Response response = client.send(request);
    } catch (RequestException e) {
      logger.error("RequestException while adding activity for team ('" + teamId + "') and person ('" + personId + "')" , e);
    } catch (IOException e) {
      logger.error("IOException while adding activity for team ('" + teamId + "') and person ('" + personId + "')" , e);
    }
  }

}