package nl.surfnet.coin.teams.service;

import nl.surfnet.coin.teams.domain.TeamResultWrapper;

/**
 * Grouper Dao for accessing the grouper tables directly
 * 
 */
public interface GrouperDao {

  /**
   * Return all teams using a specific stem, without the teams being private
   * except if the personId is a member of the private team
   * 
   * @param stemName
   *          the name of the Stem
   * @param personId
   *          the logged in person
   * @param offset
   *          the row number of the start
   * @param pageSize
   *          the maximum result size
   * @return teams including the number of total records
   */
  TeamResultWrapper findAllTeams(String stemName, String personId, int offset,
      int pageSize);

  /**
   * Return all teams using a specific stem with a name like, , without the
   * teams being private except if the personId is a member of the private team
   * 
   * @param stemName
   *          the name of the Stem
   * @param personId
   *          the logged in person
   * @param partOfGroupname
   *          part of group name
   * @param offset
   *          the row number of the start
   * @param pageSize
   *          the maximum result size
   * @return teams including the number of total records
   */
  TeamResultWrapper findTeams(String stemName, String personId,
      String partOfGroupname, int offset, int pageSize);

}
