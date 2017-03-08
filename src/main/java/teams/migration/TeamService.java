package teams.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import teams.domain.Member;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Stem;
import teams.domain.Team;
import teams.domain.TeamResultWrapper;
import teams.repository.PersonRepository;
import teams.repository.TeamRepository;
import teams.service.GrouperTeamService;
import teams.util.DuplicateTeamException;

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

  private Pattern forbiddenChars = Pattern.compile(Pattern.quote("<>/\\*:,% "));

  private TeamRepository teamRepository;
  private PersonRepository personRepository;
  private String defaultStemName;

  @Autowired
  public TeamService(TeamRepository teamRepository, PersonRepository personRepository,
                     @Value("${defaultStemName}") String defaultStemName) {
    this.teamRepository = teamRepository;
    this.personRepository = personRepository;
    this.defaultStemName = defaultStemName;
  }


  @Override
  public teams.domain.Team findTeamById(String teamId) {
    teams.migration.Team team = findTeamByUrn(teamId);
    team.getMemberships().forEach(Membership::getPerson);
    return new Team(
      team.getUrn(),
      team.getName(),
      team.getDescription(),
      team.getMemberships()
        .stream()
        .map(membership -> new Member(
          roleConversion(membership.getRole()),
          membership.getPerson().getName(),
          membership.getPerson().getUrn(),
          membership.getPerson().getEmail()))
        .collect(Collectors.toList()),
      team.isViewable());
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
    teams.migration.Team teamByUrn = findTeamByUrn(team.getId());
    teams.migration.Person personByUrn = findPersonByUrn(memberId);
    teamByUrn.getMemberships().add(new Membership(
      teams.migration.Role.valueOf(role.name().toUpperCase()), teamByUrn, findPersonByUrn(memberId)
    ));
    //teamRepository.save()
    return true;
  }

  @Override
  public boolean removeMemberRole(Team team, String memberId, Role role, String actAsUserId) {
    return false;
  }

  @Override
  public void addMember(Team team, Person person) {

  }

  @Override
  public Member findMember(Team team, String memberId) {
    return null;
  }

  @Override
  public Set<Member> findAdmins(Team team) {
    return null;
  }

  @Override
  public Stem findStem(String stemId) {
    return null;
  }

  @Override
  public List<Team> findPublicTeams(String personId, String partOfGroupname) {
    return null;
  }

  @Override
  public TeamResultWrapper findAllTeamsByMember(String personId, int offset, int pageSize) {
    return null;
  }

  @Override
  public TeamResultWrapper findTeamsByMember(String personId, String partOfGroupname, int offset, int pageSize) {
    return null;
  }

  @Override
  public List<Stem> findStemsByMember(String personId) {
    return null;
  }

  private teams.migration.Team findTeamByUrn(String teamId) {
    Optional<teams.migration.Team> teamOptional = teamRepository.findByUrn(teamId);
    return teamOptional.orElseThrow(teamDoesNotExist(teamId));
  }

  private teams.migration.Person findPersonByUrn(String personId) {
    Optional<teams.migration.Person> personOptional = personRepository.findByUrn(personId);
    return personOptional.orElseThrow(teamDoesNotExist(personId));
  }

  private Supplier<IllegalArgumentException> teamDoesNotExist(String teamId) {
    return doesNotExist("Team", teamId);
  }

  private Supplier<IllegalArgumentException> personDoesNotExist(String personId) {
    return doesNotExist("Person", personId);  }

  private Supplier<IllegalArgumentException> doesNotExist(String entity, String id) {
    return () -> new IllegalArgumentException(String.format("%s %s does not exist",entity,id));
  }

  private Set<Role> roleConversion(teams.migration.Role role) {
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
