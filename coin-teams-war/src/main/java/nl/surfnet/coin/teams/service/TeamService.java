/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.service;

import java.util.List;
import java.util.Set;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.util.DuplicateTeamException;



/**
 * Main interface for dealing with Teams
 * 
 */
public interface TeamService {

  /**
   * Get Teams by Person
   * 
   * @param memberId
   *          the id of the Member
   * @return {@link List} of {@link Team} instances
   */
  List<Team> getTeamsByMember(String memberId);

  /**
   * Find Teams by part of the team name
   * 
   * @param partOfTeamName
   *          query is based on %partOfTeamName%
   * @return {@link List} of {@link Team} instances
   */
  List<Team> findTeams(String partOfTeamName);
  
  /**
   * Return all teams
   * 
   * @return {@link List} of {@link Team} instances
   */
  List<Team> findAllTeams();

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
   * @return The id of the team
   * @throws DuplicateTeamException when a team with with the given teamId already exists.
   */
  String addTeam(String teamId, String displayName, String teamDescription) throws DuplicateTeamException;

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
   * @param memberId the unique identifier for a {@link Member}
   */
  void deleteMember(String teamId, String personId);

  /**
   * Update the {@link Team} to be (not) visible
   * @param teamId
   * @param viewable
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
  
  List<Team> findTeams(String partOfTeamName, String memberId);

  void addMember(String teamId, String personId);
  
  Member findMember(String teamId, String memberId);
  
  Member findMember(Team team, String memberId);
  
  Set<Member> findAdmins(Team team);
  
}
