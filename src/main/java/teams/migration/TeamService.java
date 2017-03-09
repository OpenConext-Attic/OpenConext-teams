package teams.migration;

import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import teams.domain.Member;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Stem;
import teams.domain.Team;
import teams.domain.TeamResultWrapper;
import teams.repository.MembershipRepository;
import teams.repository.PersonRepository;
import teams.repository.TeamRepository;
import teams.service.GrouperTeamService;
import teams.util.DuplicateTeamException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Component
public class TeamService implements GrouperTeamService {

  private Set<Role> adminRoles = new HashSet<>(asList(Role.Admin, Role.Manager, Role.Member));
  private Set<Role> managerRoles = new HashSet<>(asList(Role.Manager, Role.Member));
  private Set<Role> memberRoles = new HashSet<>(asList(Role.Member));

  private Pattern forbiddenChars = Pattern.compile(String.format("[%s]", Pattern.quote("<>/\\*:,% ")));

  private TeamRepository teamRepository;
  private PersonRepository personRepository;
  private MembershipRepository membershipRepository;
  private String defaultStemName;

  @Autowired
  public TeamService(TeamRepository teamRepository, PersonRepository personRepository,
                     MembershipRepository membershipRepository, @Value("${defaultStemName}") String defaultStemName) {
    this.teamRepository = teamRepository;
    this.personRepository = personRepository;
    this.membershipRepository = membershipRepository;
    this.defaultStemName = defaultStemName;
  }


  @Override
  public teams.domain.Team findTeamById(String teamId) {
    teams.migration.Team team = findTeamByUrn(teamId);
    return convertTeam(team, true, Optional.empty());
  }

  @Override
  public String addTeam(String teamId, String displayName, String teamDescription, String stemName) throws DuplicateTeamException {
    String teamUrn = defaultStemName + ":" + forbiddenChars.matcher(teamId).replaceAll("");
    teams.migration.Team team = new teams.migration.Team(teamUrn, displayName, teamDescription);
    return teamRepository.save(team).getUrn();
  }

  @Override
  public void updateTeam(String teamId, String displayName, String teamDescription, String actAsSubject) {
    teams.migration.Team team = findTeamByUrn(teamId);
    team.setName(displayName);
    team.setDescription(teamDescription);
    teamRepository.save(team);
  }

  @Override
  public void deleteTeam(String teamId) {
    teams.migration.Team team = findTeamByUrn(teamId);
    teamRepository.delete(team);
  }

  @Override
  public void deleteMember(Team team, String personId) {
    teams.migration.Person person = findPersonByUrn(personId);
    personRepository.delete(person);
  }

  @Override
  public void setVisibilityGroup(String teamId, boolean viewable) {
    teams.migration.Team team = findTeamByUrn(teamId);
    team.setViewable(viewable);
    teamRepository.save(team);
  }

  @Override
  public boolean addMemberRole(Team team, String memberId, Role role, String actAsUserId) {
    //This is a promotion. Prerequisite - legacy - is that the person is already a member
    Membership membership = findMembershipByTeamUrnAndPersonUrn(team.getId(), memberId);
    membership.setRole(convertRole(role));
    membershipRepository.save(membership);
    return true;
  }

  @Override
  public boolean removeMemberRole(Team team, String memberId, Role role, String actAsUserId) {
    //This is a degradation. Prerequisite - legacy - is that the person is already a member
    Membership membership = findMembershipByTeamUrnAndPersonUrn(team.getId(), memberId);
    teams.migration.Role roleToBeRemoved = convertRole(role);
    teams.migration.Role newRole = roleToBeRemoved == teams.migration.Role.ADMIN ? teams.migration.Role.MANAGER : teams.migration.Role.MEMBER;
    membership.setRole(newRole);
    membershipRepository.save(membership);
    return true;
  }

  @Override
  public void addMember(Team team, Person person) {
    teams.migration.Team membershipTeam = findTeamByUrn(team.getId());
    teams.migration.Person membershipPerson = findPersonByUrn(person.getId());
    Membership membership = new Membership(teams.migration.Role.MEMBER, membershipTeam, membershipPerson);
    membershipRepository.save(membership);
  }

  @Override
  public Member findMember(Team team, String memberId) {
    Membership membership = findMembershipByTeamUrnAndPersonUrn(team.getId(), memberId);
    return convertMembershipToMember(membership);
  }

  @Override
  public Set<Member> findAdmins(Team team) {
    return findTeamByUrn(team.getId()).getMemberships()
      .stream()
      .filter(membership -> membership.getRole().equals(teams.migration.Role.ADMIN))
      .map(this::convertMembershipToMember)
      .collect(Collectors.toSet());
  }

  @Override
  public Stem findStem(String stemId) {
    return new Stem(stemId, null, null);
  }

  @Override
  public List<Team> findPublicTeams(String personId, String partOfGroupname) {
    return teamRepository.findByNameContainingIgnoreCaseOrderByNameAsc(partOfGroupname).stream()
      .filter(team -> team.isViewable() || team.getMemberships().stream().anyMatch(membership -> membership.getUrnPerson().equals(personId)))
      .map(team -> this.convertTeam(team, false, Optional.empty()))
      .collect(Collectors.toList());
  }

  @Override
  public TeamResultWrapper findAllTeamsByMember(String personId, int offset, int pageSize) {
    Page<teams.migration.Team> page = teamRepository.findByMembershipsUrnPersonOrderByNameAsc(personId, new PageRequest(offset, pageSize));
    List<Team> teams = page.getContent().stream().map(team -> this.convertTeam(team, false, Optional.of(personId)))
      .collect(Collectors.toList());
    return new TeamResultWrapper(teams, page.getTotalElements(), offset, pageSize);
  }

  @Override
  public TeamResultWrapper findTeamsByMember(String personId, String partOfGroupname, int offset, int pageSize) {
    Page<teams.migration.Team> page = teamRepository.findByNameContainingIgnoreCaseAndMembershipsUrnPersonOrderByNameAsc(partOfGroupname, personId, new PageRequest(offset, pageSize));
    List<Team> teams = page.getContent().stream().map(team -> this.convertTeam(team, false, Optional.of(personId)))
      .collect(Collectors.toList());
    return new TeamResultWrapper(teams, page.getTotalElements(), offset, pageSize);
  }

  @Override
  public List<Stem> findStemsByMember(String personId) {
    return Collections.singletonList(new Stem(defaultStemName, null, null));
  }

  private Member convertMembershipToMember(Membership membership) {
    teams.migration.Person person = membership.getPerson();
    return new Member(
      convertRoles(membership.getRole()),
      person.getName(),
      person.getUrn(),
      person.getEmail());
  }

  private Team convertTeam(teams.migration.Team team, boolean includeMembership, Optional<String> personUrnOptional) {
    Team result = new Team(
      team.getUrn(),
      team.getName(),
      team.getDescription(),
      includeMembership ?
        team.getMemberships().stream().map(this::convertMembershipToMember).collect(Collectors.toList()) :
        Collections.emptyList(),
      team.isViewable(),
      team.getMembershipCount());
    if (personUrnOptional.isPresent()) {
      String personUrn = personUrnOptional.get();
      teams.migration.Role role = team.getMemberships().stream().filter(membership -> membership.getUrnPerson().equals(personUrn))
        .findFirst()
        .map(Membership::getRole)
        .orElseThrow(() -> new IllegalArgumentException(String.format("Team %s does not contain member %s", team, personUrn)));
      result.setViewerRole(convertRole(role));
    }
    return result;
  }

  private teams.migration.Team findTeamByUrn(String teamId) {
    Optional<teams.migration.Team> teamOptional = teamRepository.findByUrn(teamId);
    return teamOptional.orElseThrow(doesNotExist("Team", teamId));
  }

  private teams.migration.Person findPersonByUrn(String personId) {
    Optional<teams.migration.Person> personOptional = personRepository.findByUrn(personId);
    return personOptional.orElseThrow(doesNotExist("Person", personId));
  }

  private Membership findMembershipByTeamUrnAndPersonUrn(String teamUrn, String personUrn) {
    Optional<Membership> membershipOptional = membershipRepository.findByTeamUrnAndPersonUrn(teamUrn, personUrn);
    return membershipOptional.orElseThrow(doesNotExist("Membership", teamUrn + " - " + personUrn));
  }

  private Supplier<IllegalArgumentException> doesNotExist(String entity, String id) {
    return () -> new IllegalArgumentException(String.format("%s %s does not exist", entity, id));
  }

  private teams.migration.Role convertRole(Role role) {
    return teams.migration.Role.valueOf(role.name().toUpperCase());
  }

  private Role convertRole(teams.migration.Role role) {
    return Role.valueOf(WordUtils.capitalize(role.name().toLowerCase()));
  }

  private Set<Role> convertRoles(teams.migration.Role role) {
    switch (role) {
      case ADMIN:
        return adminRoles;
      case MANAGER:
        return managerRoles;
      case MEMBER:
        return memberRoles;
      default:
        throw new IllegalArgumentException("Non existent role " + role);
    }
  }

}
