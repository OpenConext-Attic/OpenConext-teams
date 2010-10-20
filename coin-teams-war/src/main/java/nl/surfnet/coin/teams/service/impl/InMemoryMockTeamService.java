/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;

import org.springframework.stereotype.Component;

/**
 * @author oharsta
 * 
 */
@Component("teamService")
public class InMemoryMockTeamService implements TeamService {

  private Map<String, Team> teams = new HashMap<String, Team>();
  private Map<String, Member> members = new HashMap<String, Member>();

  /**
   * Constructor
   */
  public InMemoryMockTeamService() {
    initData();
  }

  /*
   * Add some dummy data
   */
  private void initData() {
    Team team1 = new Team("test-team-1", "test-team-1-name", "description-1", true);
    Team team2 = new Team("test-team-2", "test-team-2-name", "description-2", false);
    Team team3 = new Team("test-team-3", "test-team-3-name", "description-3", true);
    Team team4 = new Team("test-team-4", "test-team-4-name", "description-4", true);

    teams.put(team1.getId(), team1);
    teams.put(team2.getId(), team2);
    teams.put(team3.getId(), team3);
    teams.put(team4.getId(), team4);
    
    Set<Role> roles1 = new HashSet<Role>();
    roles1.add(Role.Member);
    
    Set<Role> roles2 = new HashSet<Role>();
    roles2.add(Role.Manager);
    roles2.add(Role.Member);
    
    Set<Role> roles3 = new HashSet<Role>();
    roles3.add(Role.Admin);
    roles3.add(Role.Manager);
    roles3.add(Role.Member);

    Member member1 = new Member(roles3, "member1-name", "member-1",
        "member1@surfnet.nl");
    
    Member member2 = new Member(roles2, "member2-name", "member-2",
        "member2@surfnet.nl");
    Member member3 = new Member(roles1, "member3-name", "member-3",
        "member3@surfnet.nl");
    Member member4 = new Member(roles1, "member4-name", "member-4",
        "member4@surfnet.nl");
    Member member5 = new Member(roles1, "member5-name", "member-5",
        "member5@surfnet.nl");
    Member member6 = new Member(roles1, "member6-name", "member-6",
        "member6@surfnet.nl");
    Member member7 = new Member(roles1, "member7-name", "member-7",
        "member7@surfnet.nl");

    members.put(member1.getId(), member1);
    members.put(member2.getId(), member2);
    members.put(member3.getId(), member3);
    members.put(member4.getId(), member4);
    members.put(member5.getId(), member5);
    members.put(member6.getId(), member6);
    members.put(member7.getId(), member7);

    team1.addMembers(member1, member2, member3);
    team2.addMembers(member3, member4, member5);
    team3.addMembers(member5, member6, member7);
    team4.addMembers(member1, member2);

  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#addMember(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void addMember(String teamId, String memberId) {
    Member member = findMember(memberId);
    Team team = findTeam(teamId);
    team.addMembers(member);
  }

  private Team findTeam(String teamId) {
    Team team = teams.get(teamId);
    if (team == null) {
      throw new RuntimeException("Team(id='" + teamId + "') does not exist");
    }
    return team;
  }

  private Member findMember(String memberId) {
    Member member = members.get(memberId);
    if (member == null) {
      throw new RuntimeException("Member(id='" + memberId + "') does not exist");
    }
    return member;
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#addTeam(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public String addTeam(String teamId, String displayName, String teamDescription, boolean viewable) {
    Team team = new Team(teamId, displayName, teamDescription, viewable);
    teams.put(team.getId(), team);
    return team.getId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.TeamService#deleteMember(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void deleteMember(String teamId, String memberId) {
    Team team = findTeam(teamId);
    Member member = findMember(memberId);
    team.removeMembers(member);
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#deleteTeam(java.lang.String)
   */
  @Override
  public void deleteTeam(String teamId) {
    Team team = findTeam(teamId);
    teams.remove(team);
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#findAllTeams()
   */
  @Override
  public List<Team> findAllTeams() {
    return new ArrayList<Team>(teams.values());
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#findAllTeams(java.lang.Boolean)
   */
  @Override
  public List<Team> findAllTeams(boolean viewable) {
    List<Team> result = new ArrayList<Team>();
    List<Team> teamList = new ArrayList<Team>(teams.values());
    
    for (Team team : teamList) {
      if (team.isViewable() == viewable) {
        result.add(team);
      }
    }
    
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.TeamService#findTeamById(java.lang.String)
   */
  @Override
  public Team findTeamById(String teamId) {
    return findTeam(teamId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#findTeams(java.lang.String)
   */
  @Override
  public List<Team> findTeams(String partOfTeamName) {
    Collection<Team> values = teams.values();
    List<Team> result = new ArrayList<Team>();
    for (Team team : values) {
      if (team.getName().contains(partOfTeamName)) {
        result.add(team);
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.TeamService#getTeamsByPerson(java.lang.String
   * )
   */
  @Override
  public List<Team> getTeamsByMember(String memberId) {
    Member member = findMember(memberId);
    Collection<Team> values = teams.values();
    List<Team> result = new ArrayList<Team>();
    for (Team team : values) {
      if (team.getMembers().contains(member)) {
        result.add(team);
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.TeamService#updateMember(java.lang.String,
   * java.lang.String, nl.surfnet.coin.teams.domain.Role)
   */
  @Override
  public void updateMember(String teamId, String memberId, Role role) {
    Member member = findMember(memberId);
    member.addRole(role);
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#updateTeam(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public void updateTeam(String teamId, String displayName,
      String teamDescription) {
    Team team = findTeam(teamId);
    team.setName(displayName);
    team.setDescription(teamDescription);
  }

}
