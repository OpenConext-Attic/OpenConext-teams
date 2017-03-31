package teams.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JdbcMigrationDao {

  private final JdbcTemplate jdbcTemplate;

  public JdbcMigrationDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Collection<Team> findAllTeamsAndMemberships() {
    final Map<String, Team> teams = new HashMap<>();
    final Map<String, Person> persons = new HashMap<>();
    jdbcTemplate.query("select gm.subject_id as subject_id, gg.name as group_name, gg.description as group_description, " +
        "gg.display_extension as group_display_extension, gg.create_time as group_create_time, gf.name as fieldname , gms.create_time as membership_created " +
        "from grouper_memberships gms, grouper_groups gg, grouper_stems gs, grouper_members gm, grouper_fields gf " +
        "where gms.owner_group_id = gg.id and gms.member_id = gm.id and gms.field_id = gf.id " +
        "and gg.parent_stem = gs.id and gs.name != 'etc' and gm.subject_id != 'GrouperSystem'",

      rs -> {
        String urn = rs.getString("group_name");

        Team team = teams.getOrDefault(urn, new Team(
          urn,
          rs.getString("group_display_extension"),
          rs.getString("group_description"),
          Instant.ofEpochMilli(rs.getLong("group_create_time"))));

        String subjectId = rs.getString("subject_id").replaceAll("@", "_");

        if (subjectId.equalsIgnoreCase("GrouperAll")) {
          team.setViewable(true);
          return;
        }

        Optional<Membership> membershipOptional = team.getMemberships().stream()
          .filter(membership -> membership.getPerson().getUrn().equals(subjectId))
          .findFirst();

        Role role = getRole(rs.getString("fieldname"));

        if (membershipOptional.isPresent()) {
          Membership membership = membershipOptional.get();
          if (role.isMoreImportant(membership.getRole())) {
            membership.setRole(role);
          }
        } else {
          Person person;
          if (persons.containsKey(subjectId)) {
            person = persons.get(subjectId);
          } else {
            person = new Person(subjectId);
            persons.put(subjectId, person);
          }
          Instant created = Instant.ofEpochMilli(rs.getLong("membership_created"));
          Membership membership = new Membership(role, team, person, created);
          team.getMemberships().add(membership);
        }

        teams.put(team.getUrn(), team);

      });

    return teams.values();

  }

  private Role getRole(String privilegeName) {
    if (privilegeName.equalsIgnoreCase("admins")) {
      return Role.ADMIN;
    } else if (privilegeName.equalsIgnoreCase("updaters")) {
      return Role.MANAGER;
    }
    return Role.MEMBER;
  }

}
