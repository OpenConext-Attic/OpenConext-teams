/*
 * Copyright 2012 SURFnet bv, The Netherlands
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

package teams.service;

import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import teams.domain.Member;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Stem;
import teams.domain.Team;
import teams.domain.TeamResultWrapper;
import teams.util.DuplicateTeamException;

/**
 * Main interface for dealing with Teams
 */
public interface GrouperTeamService {

  /**
   * Find {@link Team} by id
   *
   * @param teamId
   *          unique identifier for a {@link Team}
   * @return Team with all members
   */
  Team findTeamById(String teamId);

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
   * @throws DuplicateTeamException
   *           when a team with with the given teamId already exists.
   */
  String addTeam(String teamId, String displayName, String teamDescription, String stemName) throws DuplicateTeamException;

  /**
   * Update a {@link Team}
   *
   * @param teamId          the id of a {@link teams.domain.Team}
   * @param displayName     the new displayName
   * @param teamDescription the new description of the {@link teams.domain.Team}
   * @param actAsSubject    the unique identifier that performs the request in Grouper. Can be the power user
   *                        or the memberId
   */
  void updateTeam(String teamId, String displayName, String teamDescription, String actAsSubject);

  /**
   * Delete a {@link Team}
   *
   * @param teamId
   *          the unique identifier
   */
  void deleteTeam(String teamId);

  /**
   * Delete a Member from a {@link Team}
   *
   * @param teamId
   *          the unique identifier for a {@link Team}
   * @param personId
   *          the unique identifier for a {@link Member}
   */
  void deleteMember(String teamId, String personId);

  /**
   * Update the {@link Team} to be (not) visible
   *
   * @param teamId
   *          unique identifier for a {@link Team}
   * @param viewable
   *          boolean
   */
  void setVisibilityGroup(String teamId, boolean viewable);

  /**
   * Add {@link Role} to a {@link Team}
   *
   * @param teamId
   *          the unique identifier of a {@link teams.domain.Team}
   * @param memberId
   *          the unique identifier of a {@link teams.domain.Member}
   * @param role
   *          the {@link teams.domain.Role} to be added
   * @param actAsUserId
   *          the unique identifier that performs the request in Grouper. Can be the power user
   *          or the memberId
   * @return boolean true if the {@link Role} has been successfully added false
   *         if the {@link Role} has not been added
   */
  boolean addMemberRole(String teamId, String memberId, Role role, String actAsUserId);

  /**
   * Remove {@link Role} to a {@link Team}
   *
   * @param teamId
   *          the unique identifier of a {@link teams.domain.Team}
   * @param memberId
   *          the unique identifier of a {@link teams.domain.Member}
   * @param role
   *          the {@link teams.domain.Role} to be removed
   * @param actAsUserId
   *          the unique identifier that performs the request in Grouper. Can be the power user
   *          of the memberId
   * @return boolean true if the {@link Role} has been successfully added false
   *         if the {@link Role} has not been added
   */
  boolean removeMemberRole(String teamId, String memberId, Role role, String actAsUserId);

  /**
   * Adds a person to the team
   *
   * @param teamId
   *          the unique identifier of the
   *          {@link teams.domain.Team}
   * @param person
   *          {@link teams.domain.Person} to add as Member to the Team
   */
  void addMember(String teamId, Person person);

  /**
   * Tries to find a Member in a Team
   *
   * @param teamId
   *          the unique identifier of the {@link Team}
   * @param memberId
   *          the unique identifier of a {@link Member}
   * @return {@link Member} or {@literal null} if the Team does not contain a
   *         Member by the memberId
   */
  default Member findMember(String teamId, String memberId) {
    Optional<Member> member = findTeamById(teamId).getMembers().stream()
        .filter(m -> m.getId().equals(memberId))
        .findFirst();

    return member.orElseThrow(() -> new RuntimeException("Member(id='" + memberId + "') is not a member of the team"));
  }

  /**
   * Returns a Set of {@link Member}'s that have the admin role for this team
   *
   * @param team
   *          {@link Team}
   * @return Set of {@link Member}'s with admin role, can be empty
   */
  default Set<Member> findAdmins(Team team) {
    return team.getMembers().stream()
        .filter(m -> m.getRoles().contains(Role.Admin))
        .collect(toSet());
  }

  Stem findStem(String stemId);

  /**
   * Return all teams using a specific stem with a name like, , without the
   * teams being private except if the personId is a member of the private team
   *
   * @param personId
   *          the logged in person
   * @param partOfGroupname
   *          part of group name
   * @return teams including the number of total records
   */
  List<Team> findPublicTeams(String personId, String partOfGroupname);

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
  TeamResultWrapper findAllTeamsByMember(String personId, int offset, int pageSize);

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
  TeamResultWrapper findTeamsByMember(String personId, String partOfGroupname, int offset, int pageSize);

  /**
   * Return all stems for a person
   *
   * @param personId {@link String} the identifier of the logged in person
   * @return {@link java.util.List} the list with stems that this user is a member of.
   */
  List<Stem> findStemsByMember(String personId);
}
