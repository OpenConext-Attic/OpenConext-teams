/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.service;

import java.util.List;

import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;

/**
 * Main interface for dealing with Teams
 * 
 * @TODO add JavaDoc
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

  List<Team> findTeams(String partOfTeamName);

  List<Team> findAllTeams();
  
  Team findTeamById(String teamId);

  String addTeam(String teamId, String displayName, String teamDescription, boolean viewable);

  void updateTeam(String teamId, String displayName, String teamDescription);

  void deleteTeam(String teamId);

  void addMember(String teamId, String personId);

  void deleteMember(String teamId, String personId);

  void setVisibilityGroup(String teamId, boolean viewable);

  void updateMember(String teamId, String memberId, Role role);

}
