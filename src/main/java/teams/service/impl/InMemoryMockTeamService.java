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
package teams.service.impl;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;

import teams.domain.Member;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Stem;
import teams.domain.Team;
import teams.domain.TeamResultWrapper;
import teams.service.GrouperTeamService;
import teams.util.DuplicateTeamException;

/**
 * Mock implementation of {@link teams.service.GrouperTeamService}
 */
public class InMemoryMockTeamService implements GrouperTeamService {

  private Map<String, Team> teams = new HashMap<>();

  public InMemoryMockTeamService() {
    initData();
  }

  private void initData() {
    Team team1 = new Team("test-team-1", "test-team-1-name", null, false);
    Team team2 = new Team("test-team-2", "test-team-2-name", "description-2", false);
    Team team3 = new Team("test-team-3", "test-team-3-name", "description-3", true);
    Team team4 = new Team("test-team-4", "test-team-4-name", "description-4", true);

    teams.put(team1.getId(), team1);
    teams.put(team2.getId(), team2);
    teams.put(team3.getId(), team3);
    teams.put(team4.getId(), team4);

    Set<Role> roles1 = ImmutableSet.of(Role.Member);
    Set<Role> roles2 = ImmutableSet.of(Role.Manager, Role.Member);
    Set<Role> roles3 = ImmutableSet.of(Role.Admin, Role.Manager, Role.Member);

    Member member1 = new Member(roles3, "member1-name", "member1", "member1@surfnet.nl");
    Member member2 = new Member(roles2, "member2-name", "member-2", "member2@surfnet.nl");
    Member member3 = new Member(roles1, "member3-name", "member-3", "member3@surfnet.nl");
    Member member4 = new Member(roles1, "member4-name", "member-4", "member4@surfnet.nl");
    member4.setGuest(true);
    Member member5 = new Member(roles1, "member5-name", "member-5", "member5@surfnet.nl");
    Member member6 = new Member(roles1, "member6-name", "member-6", "member6@surfnet.nl");
    Member member7 = new Member(roles1, "member7-name", "member-7", "member7@surfnet.nl");

    team1.addMembers(member1.copy(), member2.copy(), member3.copy());
    team2.addMembers(member3.copy(), member4.copy(), member5.copy());
    team3.addMembers(member5.copy(), member6.copy(), member7.copy());
    team4.addMembers(member1.copy(), member2.copy(), member4.copy());

    List<Member> dummyMembers = new ArrayList<>();
    for (int memberId = 10; memberId < 110; memberId++) {
      Member dummyMember = new Member(roles1, "member" + memberId + "-name", "member-" + memberId, "member" + memberId + "@surfnet.nl");
      dummyMembers.add(dummyMember);
    }

    for (int teamId = 5; teamId < 50; teamId++) {
      Team newTeam = new Team("test-team-" + teamId, "test-team-" + teamId + "-name", "description-" + teamId, true);
      newTeam.addMembers(dummyMembers.toArray(new Member[dummyMembers.size()]));
      teams.put(newTeam.getId(), newTeam);
    }
  }

  @Override
  public String addTeam(String teamId, String displayName, String teamDescription, String stemName) throws DuplicateTeamException {
    String fqTeamId = stemName + ":" + teamId;
    Team team = new Team(fqTeamId, displayName, teamDescription);
    if (teams.containsKey(team.getId())) {
      throw new DuplicateTeamException("There is already a team with id '" + teamId + "'");
    }
    teams.put(team.getId(), team);
    return team.getId();
  }

  @Override
  public void deleteMember(Team team, String memberId) {
    Member member = findMember(team, memberId);
    team.removeMembers(member);
  }

  @Override
  public void deleteTeam(String teamId) {
    teams.remove(teamId);
  }

  @Override
  public List<Team> findPublicTeams(String personId, String partOfGroupname) {
    return teams.values().stream()
        .filter(team -> team.isViewable() && team.getName().contains(partOfGroupname))
        .collect(toList());

  }

  @Override
  public TeamResultWrapper findAllTeamsByMember(String personId, int offset, int pageSize) {
    return findTeams(offset, pageSize, team -> team.getMembers().stream().anyMatch(member -> personId.equals(member.getId())));
  }

  @Override
  public TeamResultWrapper findTeamsByMember(String personId, String partOfGroupname, int offset, int pageSize) {
    return findTeams(offset, pageSize, team -> {
      if (!(team.isViewable() && team.getName().contains(partOfGroupname))) {
        return false;
      }
      return team.getMembers().stream().anyMatch(member -> personId.equals(member.getId()));
    });
  }

  private TeamResultWrapper findTeams(int offset, int pageSize, Predicate<Team> predicate) {
    List<Team> matches = teams.values().stream().filter(predicate).collect(toList());
    List<Team> limitedMatches = matches.stream().skip(offset).limit(pageSize).collect(toList());

    return new TeamResultWrapper(limitedMatches, matches.size(), offset, pageSize);
  }

  @Override
  public List<Stem> findStemsByMember(String personId) {
    return new ArrayList<>();
  }

  @Override
  public Team findTeamById(String teamId) {
    return Optional.ofNullable(teams.get(teamId))
        .orElseThrow(() -> new RuntimeException("Team(id='" + teamId + "') does not exist"));
  }

  @Override
  public void setVisibilityGroup(String teamId, boolean viewable) {
    Team team = findTeamById(teamId);
    team.setViewable(viewable);
  }

  @Override
  public boolean addMemberRole(Team team, String memberId, Role role, String actAsUserId) {
    Member member = findMember(team, memberId);

    if (role.equals(Role.Admin) && !member.getRoles().contains(Role.Manager)) {
      member.addRole(Role.Manager);
    }

    return member.addRole(role);
  }

  @Override
  public boolean removeMemberRole(Team team, String memberId, Role role, String actAsUserId) {
    Member member = findMember(team, memberId);
    return member.removeRole(role);
  }

  @Override
  public void addMember(Team team, Person person) {
    // just find the member (in some other team), copy and add to team
    List<Team> allTeams = findPublicTeams("", "");
    Member m = null;
    for (Team t : allTeams) {
      if (m != null) {
        break;
      }
      List<Member> members = t.getMembers();
      for (Member member : members) {
        if (member.getId().equals(person.getId())) {
          m = member.copy();
          break;
        }
      }
    }
    if (m == null) {
      m = new Member(new HashSet<>(), person);
    }
    m.setGuest(person.isGuest());
    team.addMembers(m);
  }

  @Override
  public void updateTeam(String teamId, String displayName, String teamDescription, String actAsSubject) {
    Team team = findTeamById(teamId);
    team.setName(displayName);
    team.setDescription(teamDescription);
  }

  @Override
  public Stem findStem(String stemId) {
    return null;
  }
}
