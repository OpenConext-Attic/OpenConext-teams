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

    Set<String> notPresentInLdap = new HashSet<>();
    persons.forEach(person -> {
      if (!this.addDetails(person)) {
        notPresentInLdap.add(person.getUrn());
      }
    });

    LOG.info("migrationDao.addingLdapDetails ended in {} ms", System.currentTimeMillis() - startLdap);

    long startDatabase = System.currentTimeMillis();
    LOG.info("migrationDao.saving all persons and teams starting ");

    personRepository.save(persons);
    teamRepository.save(teams);

    LOG.info("migrationDao.saving all persons and teams ended in {} ms", System.currentTimeMillis() - startDatabase);

    LOG.info("total migration took {} ms",  System.currentTimeMillis() - start);

    return ResponseEntity.ok(notPresentInLdap);
  }

  private boolean addDetails(Person person) {
    Optional<Person> personOptional = userDetailsManager.findPersonById(person.getUrn());
    if (personOptional.isPresent()) {
      Person details = personOptional.get();
      person.setEmail(details.getEmail());
      person.setGuest(details.isGuest());
      person.setName(details.getName());
      return true;
    }
    return false;

  }
}
