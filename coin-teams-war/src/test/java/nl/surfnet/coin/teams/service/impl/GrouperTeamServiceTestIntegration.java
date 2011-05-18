package nl.surfnet.coin.teams.service.impl;

import org.junit.Ignore;

import nl.surfnet.coin.teams.domain.TeamResultWrapper;
import nl.surfnet.coin.teams.util.TeamEnvironment;

/**
 * 
 * Integration test for Grouper. Note: this is not run as part of the build
 *
 */
public class GrouperTeamServiceTestIntegration {

  @Ignore
  public void testFindAllTeams() {
    GrouperTeamService teamService = new GrouperTeamService();
    TeamEnvironment environment = new TeamEnvironment();
    environment.setDefaultStemName("nl:surfnet:diensten");
    environment.setGrouperPowerUser("GrouperSystem");
    teamService.setEnvironment(environment );
    for (int i = 0; i < 100; i++) {
      long time = System.currentTimeMillis();
      TeamResultWrapper allTeamsLimited = teamService.findAllTeams("nl:surfnet:diensten", 0, 10);
      System.out.println(System.currentTimeMillis() - time);
    }
  }
  
  

}
