/*
 * Copyright 2011 SURFnet bv
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
