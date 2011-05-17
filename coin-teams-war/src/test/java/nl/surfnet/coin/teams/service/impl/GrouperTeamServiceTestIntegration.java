package nl.surfnet.coin.teams.service.impl;

import static org.junit.Assert.*;

import java.util.List;

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.util.TeamEnvironment;

import org.junit.Test;

/**
 * 
 * Integration test for Grouper. Note: this is not run as part of the build
 *
 */
public class GrouperTeamServiceTestIntegration {

  @Test
  public void testFindAllTeams() {
    GrouperTeamService teamService = new GrouperTeamService();
    TeamEnvironment environment = new TeamEnvironment();
    environment.setDefaultStemName("nl:surfnet:diensten");
    environment.setGrouperPowerUser("GrouperSystem");
    teamService.setEnvironment(environment );
    for (int i = 0; i < 100; i++) {
      long time = System.currentTimeMillis();
      List<Team> findAllTeams = teamService.findAllTeams(null);
      System.out.println(System.currentTimeMillis() - time);
    }
//    assertEquals(100, findAllTeams.size());
  }
  
  

}
