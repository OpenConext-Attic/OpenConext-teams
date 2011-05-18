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

  /**
   * Return all teams using a specific stem with a name like
   * 
   * @param stemName
   *          the name of the Stem
   * @param partOfGroupname
   *          part of group name
   * @param offset
   *          the row number of the start
   * @param pageSize
   *          the maximum result size
   * @return teams including the number of total records
   */
  TeamResultWrapper findTeams(String stemName, String partOfGroupname,
      int offset, int pageSize);

  /**
   * Return all the teams for a person
   * 
   * @param stemName
   *          the name of the Stem
   * @param personId
   *          the id of the person
   * @param offset
   *          the row number of the start
   * @param pageSize
   *          the maximum result size
   * @return teams including the number of total records
   */
  TeamResultWrapper findAllTeamsByMember(String stemName, String personId,
      int offset, int pageSize);

  /**
   * 
   * @param stemName
   *          the name of the Stem
   * @param personId
   *          the id of the person
   * @param partOfGroupname
   *          part of group name
   * @param offset
   *          the row number of the start
   * @param pageSize
   *          the maximum result size
   * @return teams including the number of total records
   */
  TeamResultWrapper findTeamsByMember(String stemName, String personId,
      String partOfGroupname, int offset, int pageSize);
}
