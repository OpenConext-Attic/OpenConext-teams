package nl.surfnet.coin.teams.service.impl;

import org.junit.Ignore;
import org.junit.Test;

import nl.surfnet.coin.teams.domain.TeamResultWrapper;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.util.TeamEnvironment;

/**
 * 
 * Integration test for Grouper. Note: this is not run as part of the build
 *
 */
public class GrouperTeamServiceTestIntegration {

  @Test
  public void testFindAllTeams() {
    LoginInterceptor.setLoggedInUser("urn:collab:person:test.surfguest.nl:oharsta");
    
    GrouperTeamService teamService = new GrouperTeamService();
    TeamEnvironment environment = new TeamEnvironment();
    environment.setDefaultStemName("nl:surfnet:diensten");
    environment.setGrouperPowerUser("GrouperSystem");
    teamService.setEnvironment(environment );
    teamService.findAllTeamsOld(null);
  }
  
  

}
