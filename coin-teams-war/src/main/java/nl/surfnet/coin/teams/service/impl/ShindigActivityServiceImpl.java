/**
 * 
 */
package nl.surfnet.coin.teams.service.impl;

import java.io.IOException;

import nl.surfnet.coin.teams.service.ShindigActivityService;
import nl.surfnet.coin.teams.util.TeamEnvironment;

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

  /* (non-Javadoc)
   * @see nl.surfnet.coin.teams.service.ShindigActivityService#addActivity(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void addActivity(String person, String title, String body) throws RequestException, IOException {
    
    Provider provider = new ShindigProvider();
    
    provider.setRestEndpoint(environment.getRestEndpoint());
    provider.setRpcEndpoint(environment.getRpcEndpoint());
    
    AuthScheme scheme = new OAuth2LeggedScheme(environment.getConsumerKey(), environment.getConsumerSecret(), person);

    Client client = new Client(provider, scheme);
    
    Activity activity = new Activity();
    activity.setTitle(title);
    activity.setBody(body);
    
    Request request = ActivitiesService.createActivity(activity);
    Response response = client.send(request);
    
  }

}
