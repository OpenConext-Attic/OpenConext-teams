package nl.surfnet.coin.teams.service;

import nl.surfnet.coin.teams.domain.TeamResultWrapper;

/**
 * Grouper Dao for accessing the grouper tables directly
 * 
 */
public interface GrouperDao {

  /**
   * Return all teams using a specific stem
   * 
   * @param stemName
   *          the name of the Stem
   * @param offset
   *          the row number of the start
   * @param pageSize
   *          the maximum result size
   * @return teams including the number of total records
   */
  TeamResultWrapper findAllTeams(String stemName, int offset, int pageSize);

}
