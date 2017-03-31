package teams.migration;

import org.hibernate.engine.spi.CacheImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import teams.provision.UserDetailsManager;
import teams.repository.PersonRepository;
import teams.repository.TeamRepository;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Collection;
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
  private final TeamRepository teamRepository;
  private final PersonRepository personRepository;
  private final UserDetailsManager userDetailsManager;
  private final String secretKey;
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public MigrationService(JdbcMigrationDao migrationDao,
                          TeamRepository teamRepository,
                          PersonRepository personRepository,
                          UserDetailsManager userDetailsManager,
                          @Value("${migration.secret_key}") String secretKey,
                          @Qualifier("teamsDataSource") DataSource teamsDataSource) {
    this.migrationDao = migrationDao;
    this.teamRepository = teamRepository;
    this.personRepository = personRepository;
    this.userDetailsManager = userDetailsManager;
    this.secretKey = secretKey;
    this.jdbcTemplate = new JdbcTemplate(teamsDataSource);
  }

  @GetMapping("migrate")
  @Transactional
  public ResponseEntity migrate(@RequestParam(name = "key") String key) {
    if (!secretKey.equals(key)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    long start = System.currentTimeMillis();
    LOG.info("Starting migration");

    //idempotent
    deleteFromTable("memberships");
    deleteFromTable("persons");
    deleteFromTable("teams");

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
      LOG.info("Saving person {} {} {}", person.getId(), person.getEmail(), person.getName());
      personRepository.save(person);
      LOG.info("Saved person {} {} {}", person.getId(), person.getEmail(), person.getName());
    });

    teams.forEach(team -> {
      LOG.info("Saving team {} {} {}", team.getId(), team.getName(), team.getMemberships().stream().map(membership -> membership.getPerson()).collect(toList()));
      teamRepository.save(team);
      LOG.info("Saved team {} {} {}", team.getId(), team.getName(), team.getMemberships().stream().map(membership -> membership.getPerson()).collect(toList()));
    });

    LOG.info("migrationDao.saving all persons and teams ended in {} ms", System.currentTimeMillis() - startDatabase);

    LOG.info("total migration took {} ms", System.currentTimeMillis() - start);

    return ResponseEntity.ok(grouped.get(false));
  }

  private void addDetails(Person person) {
    Optional<Person> personOptional = userDetailsManager.findPersonById(person.getUrn());
    if (personOptional.isPresent()) {
      fillDetailsPerson(person, personOptional);
    } else {
      person.setGuest(true);
      person.setName(UNKNOWN);
      person.setEmail(UNKNOWN);
    }
  }

  private void fillDetailsPerson(Person person, Optional<Person> personOptional) {
    Person details = personOptional.get();
    String email = details.getEmail();
    person.setEmail(email == null ? UNKNOWN : email);
    person.setGuest(details.isGuest());
    String name = details.getName();
    person.setName(name == null ? UNKNOWN : name);
    person.setCreated(details.getCreated());
  }

  private void deleteFromTable(String table) {
    LOG.info("Deleting from table " + table);
    jdbcTemplate.execute("DELETE FROM " + table);
  }

}
