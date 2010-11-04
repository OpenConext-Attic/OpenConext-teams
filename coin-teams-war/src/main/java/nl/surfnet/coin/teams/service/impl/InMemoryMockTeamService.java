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
 * Mock implementation of {@link TeamService}
 * 
 */
public class InMemoryMockTeamService implements TeamService {

  private Map<String, Team> teams = new HashMap<String, Team>();

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

    Team team1 = new Team("test-team-1", "test-team-1-name", null, false);
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

    Member member1 = new Member(roles3, "member1-name", "john.doe",
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

    team1.addMembers(member1.copy(), member2.copy(), member3.copy());
    team2.addMembers(member3.copy(), member4.copy(), member5.copy());
    team3.addMembers(member5.copy(), member6.copy(), member7.copy());
    team4.addMembers(member1.copy(), member2.copy());

  }

  private Team findTeam(String teamId) {
    Team team = teams.get(teamId);
    if (team == null) {
      throw new RuntimeException("Team(id='" + teamId + "') does not exist");
    }
    return team;
  }

  @Override
  public Member findMember(String teamId, String memberId) {
    Team team = findTeam(teamId);
    Set<Member> members = team.getMembers();
    for (Member member : members) {
      if (member.getId().equals(memberId)) {
        return member;
      }
    }
    throw new RuntimeException("Member(id='" + memberId + "') does not exist");
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.TeamService#addTeam(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public String addTeam(String teamId, String displayName,
      String teamDescription) {
    Team team = new Team(teamId, displayName, teamDescription);
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
    Member member = findMember(teamId, memberId);
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
    List<Team> result = new ArrayList<Team>();
    List<Team> teamList = new ArrayList<Team>(teams.values());
    
    for (Team team : teamList) {
      if (team.isViewable()) {
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
    Collection<Team> values = findAllTeams();
    List<Team> result = new ArrayList<Team>();
    for (Team team : values) {
      if (team.getName().toLowerCase().contains(partOfTeamName.toLowerCase())) {
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
    Collection<Team> values = teams.values();
    Set<Team> result = new HashSet<Team>();
    for (Team team : values) {
      Set<Member> members = team.getMembers();
      for (Member member : members) {
        if (member.getId().equalsIgnoreCase(memberId)) {
          result.add(team);
        }
      }
    }
    return new ArrayList<Team>(result);
  }

  @Override
  public void setVisibilityGroup(String teamId, boolean viewable) {
    Team team = findTeam(teamId);
    team.setViewable(viewable);
  }

  @Override
  public boolean addMemberRole(String teamId, String memberId, Role role, boolean addAsSuperUser) {
    Member member = findMember(teamId, memberId);
    
    if (role.equals(Role.Admin) && !member.getRoles().contains(Role.Manager)) {
      member.addRole(Role.Manager);
    }
    member.addRole(role);
    
    return true;
  }

  @Override
  public boolean removeMemberRole(String teamId, String memberId, Role role, boolean removeAsSuperUser) {
    Member member = findMember(teamId, memberId);
    member.removeRole(role);
    
    return true;
  }

  @Override
  public List<Team> findTeams(String partOfTeamName, String memberId) {
    List<Team> teamsByMember = getTeamsByMember(memberId);
    List<Team> result = new ArrayList<Team>();
    for (Team team : teamsByMember) {
      if (team.getName().contains(partOfTeamName)) {
        result.add(team);
      }
    }
    return result;
  }

  @Override
  public void addMember(String teamId, String personId) {
    // just find the member (in some other team), copy and add to team
    List<Team> allTeams = findAllTeams();
    Member m = null;
    for (Team team : allTeams) {
      Set<Member> members = team.getMembers();
      for (Member member : members) {
        if (member.getId().equals(personId)) {
          m = member.copy();
        }
      }  
    }
    if (m == null) {
      throw new RuntimeException("Member('"+personId+"') not found");
    }
    Team team = findTeam(teamId);
    team.addMembers(m);
  }

  @Override
  public void updateTeam(String teamId, String displayName,
      String teamDescription) {
    Team team = findTeamById(teamId);
    team.setName(displayName);
    team.setDescription(teamDescription);
  }

  @Override
  public Set<Member> findAdmins(Team team) {
    Set<Member> result = new HashSet<Member>();
    Set<Member> members = team.getMembers();
    
    for (Member member : members) {
      if (member.getRoles().contains(Role.Admin)) {
        result.add(member);
      }
    }
    
    return result;
  }

  @Override
  public Member findMember(Team team, String memberId) {
    Set<Member> members = team.getMembers();
    for (Member member : members) {
      if (member.getId().equals(memberId)) {
        return member;
      }
    }
    throw new RuntimeException("Member(id='" + memberId + "') does not exist");
  }

}
