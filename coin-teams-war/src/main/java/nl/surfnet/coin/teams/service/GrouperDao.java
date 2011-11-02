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

package nl.surfnet.coin.teams.service;

import nl.surfnet.coin.teams.domain.Stem;
import nl.surfnet.coin.teams.domain.TeamResultWrapper;

import java.util.List;

/**
 * Grouper Dao for accessing the grouper tables directly
 * 
 */
public interface GrouperDao {

  /**
   * Return all teams using a specific stem, without the teams being private
   * except if the personId is a member of the private team
   * 
   *
   * @param personId
   *          the logged in person
   * @param offset
   *          the row number of the start
   * @param pageSize
   *          the maximum result size
   * @return teams including the number of total records
   */
  TeamResultWrapper findAllTeams(String personId, int offset,
                                 int pageSize);

  /**
   * Return all teams using a specific stem with a name like, , without the
   * teams being private except if the personId is a member of the private team
   * 
   *
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
  TeamResultWrapper findTeams(String personId,
                              String partOfGroupname, int offset, int pageSize);

  /**
   * Return all teams using a specific stem where the personId is a member
   * 
   *
   * @param personId
   *          the logged in person
   * @param offset
   *          the row number of the start
   * @param pageSize
   *          the maximum result size
   * @return teams including the number of total records
   */
  TeamResultWrapper findAllTeamsByMember(String personId, int offset,
                                         int pageSize);

  /**
   * Return all teams using a specific stem with a name like where the personId is a member
   * 
   *
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
  TeamResultWrapper findTeamsByMember(String personId,
                                      String partOfGroupname, int offset, int pageSize);

  /**
   * Return all stems for a person
   *
   * @param personId {@link String} the identifier of the logged in person
   * @return {@link List} the list with stems that this user is a member of.
   */
  List<Stem> findStemsByMember(String personId);
}
