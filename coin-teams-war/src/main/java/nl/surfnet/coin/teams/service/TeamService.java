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

import java.util.Set;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.domain.TeamResultWrapper;
import nl.surfnet.coin.teams.util.DuplicateTeamException;



/**
 * Main interface for dealing with Teams
 * 
 */
public interface TeamService extends GrouperDao {

  /**
   * Find {@link Team} by id
   * 
   * @param teamId
   *          unique identifier for a {@link Team}
   * @return Team with all members
   */
  Team findTeamById(String teamId);

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

  /**
   * Add a {@link Team}. Note that the teamId is altered if not compliant to the
   * rules for correct id's.
   *
   * @param teamId
   *          the teamId
   * @param displayName
   *          the displayName
   * @param teamDescription
   *          description of the team
   * @param stemName
   *          name of the stem
   * @return The id of the team
   * @throws DuplicateTeamException when a team with with the given teamId already exists.
   */
  String addTeam(String teamId, String displayName, String teamDescription, String stemName)
          throws DuplicateTeamException;

  /**
   * Update a {@link Team}
   * @param teamId the id of a {@link Team}
   * @param displayName the new displayName
   * @param teamDescription the new description of the {@link Team}
   */
  void updateTeam(String teamId, String displayName, String teamDescription);

  /**
   * Delete a {@link Team}
   * @param teamId the unique identifier
   */
  void deleteTeam(String teamId);

  /**
   * Delete a Member from a {@link Team}
   * @param teamId the unique identifier for a {@link Team}
   * @param personId the unique identifier for a {@link Member}
   */
  void deleteMember(String teamId, String personId);

  /**
   * Update the {@link Team} to be (not) visible
   * @param teamId unique identifier for a {@link Team}
   * @param viewable boolean
   */
  void setVisibilityGroup(String teamId, boolean viewable);

  /**
   * Add {@link Role} to a {@link Team}
   * @param teamId the unique identifier of a {@link Team}
   * @param memberId the unique identifier of a {@link Team} 
   * @param role the {@link Role} to be added
   * @param addAsSuperUser boolean whether to assign the privileges as the super user
   * 
   * @return boolean true if the {@link Role} has been 
   * successfully added false if the {@link Role} has not been added
   */
  boolean addMemberRole(String teamId, String memberId, Role role, boolean addAsSuperUser);

  /**
   * Remove {@link Role} to a {@link Team}
   * @param teamId the unique identifier of a {@link Team}
   * @param memberId the unique identifier of a {@link Team} 
   * @param role the {@link Role} to be removed
   * @param removeAsSuperUser as the grouper superuser?
   * 
   * @return boolean true if the {@link Role} has been 
   * successfully added false if the {@link Role} has not been added
   */
  boolean removeMemberRole(String teamId, String memberId, Role role, boolean removeAsSuperUser);
  

  void addMember(String teamId, String personId);
  
  Member findMember(String teamId, String memberId);

  Set<Member> findAdmins(Team team);
  
}
