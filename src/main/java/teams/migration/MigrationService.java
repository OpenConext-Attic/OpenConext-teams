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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@RestController
public class MigrationService {

  private static final Logger LOG = LoggerFactory.getLogger(MigrationService.class);
  public static final String UNKNOWN = "UNKNOWN_ATTRIBUTE";

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
    long start = System.currentTimeMillis();
    LOG.info("Starting migration");

    //idempotent
    membershipRepository.deleteAll();
    teamRepository.deleteAll();
    personRepository.deleteAll();

    long startDao = System.currentTimeMillis();
    LOG.info("migrationDao.findAllTeamsAndMemberships starting ");

    //all non-persistent teams fully populated with memberships and persons
    Collection<Team> teams = migrationDao.findAllTeamsAndMemberships();

    Set<Person> persons = teams.stream().map(team -> team.getMemberships().stream().map(membership -> membership.getPerson()))
      .flatMap(Function.identity())
      .collect(toSet());

    LOG.info("migrationDao.findAllTeamsAndMemberships found {} teams with total {} members", teams.size(), persons.size());
    LOG.info("migrationDao.findAllTeamsAndMemberships ended in {} ms", System.currentTimeMillis() - startDao);

    //now fetch all the details from the LDAP and enrich the person references
    long startLdap = System.currentTimeMillis();
    LOG.info("migrationDao.addingLdapDetails starting ");

    persons.forEach(this::addDetails);

    Map<Boolean, List<Person>> grouped = persons.stream().collect(Collectors.groupingBy(person -> person.getEmail() != null && person.getName() != null));
    List<Person> personsPresentInLdap = grouped.get(true);

    LOG.info("migrationDao.addingLdapDetails ended in {} ms", System.currentTimeMillis() - startLdap);

    long startDatabase = System.currentTimeMillis();
    LOG.info("migrationDao.saving all persons and teams starting ");

    personsPresentInLdap.forEach(person -> {
      LOG.info("Saving person {} {}", person.getEmail(), person.getName());
      personRepository.save(person);
    });
    teams.forEach(team -> {
      LOG.info("Saving team {} {}", team.getName(), team.getMemberships().stream().map(membership -> membership.getPerson()).collect(toList()));
      teamRepository.save(team);
    });

    LOG.info("migrationDao.saving all persons and teams ended in {} ms", System.currentTimeMillis() - startDatabase);

    LOG.info("total migration took {} ms",  System.currentTimeMillis() - start);

    return ResponseEntity.ok(grouped.get(false));
  }

  private void addDetails(Person person) {
    Optional<Person> personOptional = userDetailsManager.findPersonById(person.getUrn());
    if (personOptional.isPresent()) {
      fillDetailsPerson(person, personOptional);
    } else {
      personOptional = userDetailsManager.findPersonById(person.getUrn().replaceAll("@", "_"));
      if (personOptional.isPresent()) {
        fillDetailsPerson(person, personOptional);
      } else {
        person.setGuest(true);
        person.setName(UNKNOWN);
        person.setEmail(UNKNOWN);
      }
    }
  }

  private void fillDetailsPerson(Person person, Optional<Person> personOptional) {
    Person details = personOptional.get();
    String email = details.getEmail();
    person.setEmail(email == null ? UNKNOWN : email);
    person.setGuest(details.isGuest());
    String name = details.getName();
    person.setName(name == null ? UNKNOWN : name);
  }
}
