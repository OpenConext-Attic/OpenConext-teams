package teams.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import teams.provision.UserDetailsManager;
import teams.repository.MembershipRepository;
import teams.repository.PersonRepository;
import teams.repository.TeamRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

@RestController
public class MigrationService {

  private static final Logger LOG = LoggerFactory.getLogger(MigrationService.class);

  private final JdbcMigrationDao migrationDao;
  private final MembershipRepository membershipRepository;
  private final TeamRepository teamRepository;
  private final PersonRepository personRepository;
  private final UserDetailsManager userDetailsManager;
  private final String secretKey;

  @Autowired
  public MigrationService(JdbcMigrationDao migrationDao,
                          TeamRepository teamRepository,
                          PersonRepository personRepository,
                          MembershipRepository membershipRepository,
                          UserDetailsManager userDetailsManager,
                          @Value("${migration.secret_key}") String secretKey) {
    this.migrationDao = migrationDao;
    this.teamRepository = teamRepository;
    this.personRepository = personRepository;
    this.membershipRepository = membershipRepository;
    this.userDetailsManager = userDetailsManager;
    this.secretKey = secretKey;
  }

  @GetMapping("migrate")
  public ResponseEntity migrate(@RequestParam(name = "key") String key) {
    if (!secretKey.equals(key)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    //idempotent
    membershipRepository.deleteAll();
    teamRepository.deleteAll();
    personRepository.deleteAll();

    //all non-persistent teams fully populated with memberships and persons
    Collection<Team> teams = migrationDao.findAllTeamsAndMemberships();

    Set<Person> persons = teams.stream().map(team -> team.getMemberships().stream().map(membership -> membership.getPerson()))
      .flatMap(Function.identity())
      .collect(toSet());

    //now fetch all the details from the LDAP and enrich the person references
    persons.forEach(this::addDetails);

    personRepository.save(persons);
    teamRepository.save(teams);

    return ResponseEntity.ok().build();
  }

  private void addDetails(Person person) {
    Optional<Person> personOptional = userDetailsManager.findPersonById(person.getUrn());
    Person details = personOptional.orElseThrow(() -> new IllegalArgumentException(person.getUrn() + " not found in the LDAP"));
    person.setEmail(details.getEmail());
    person.setGuest(details.isGuest());
    person.setName(details.getName());
  }
}
