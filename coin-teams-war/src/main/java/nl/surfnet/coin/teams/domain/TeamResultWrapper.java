package nl.surfnet.coin.teams.domain;

import java.util.List;

/**
 * Result for a teams query 
 *
 */
public class TeamResultWrapper {
 
  List<Team> teams;
  int totalCount;
  
  public TeamResultWrapper(List<Team> teams, int totalCount) {
    super();
    this.teams = teams;
    this.totalCount = totalCount;
  }

  /**
   * @return the teams
   */
  public List<Team> getTeams() {
    return teams;
  }

  /**
   * @return the totalCount
   */
  public int getTotalCount() {
    return totalCount;
  }
  
  
}
