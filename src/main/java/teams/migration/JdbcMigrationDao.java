package teams.migration;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JdbcMigrationDao {

  private final JdbcTemplate jdbcTemplate;
  private final Map<String, Team> teams = new HashMap<>();
  private final Map<String, Person> persons = new HashMap<>();

  public JdbcMigrationDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Collection<Team> findAllTeamsAndMemberships() {
    jdbcTemplate.query("select gm.subject_id as subject_id, gg.name as group_name, gg.description as group_description, " +
      "gg.display_extension as group_display_extension, gf.name as fieldname " +
      "from grouper_memberships gms, grouper_groups gg, grouper_stems gs, grouper_members gm, grouper_fields gf " +
      "where gms.owner_group_id = gg.id and gms.member_id = gm.id and gms.field_id = gf.id " +
      "and gg.parent_stem = gs.id and gs.name != 'etc' and gm.subject_id != 'GrouperSystem'", this::processRow);

    return teams.values();

  }

  private void processRow(ResultSet rs) throws SQLException {
    String urn = rs.getString("group_name");

    Team team = teams.getOrDefault(urn, new Team(
      urn,
      rs.getString("group_display_extension"),
      rs.getString("group_description")));

    String subjectId = rs.getString("subject_id");

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
      Membership membership = new Membership();
      membership.setRole(role);
      Person person;
      if (persons.containsKey(subjectId)) {
        person = persons.get(subjectId);
      } else {
        person = new Person(subjectId);
        persons.put(subjectId, person);
      }
      membership.setPerson(person);
      membership.setTeam(team);
      team.getMemberships().add(membership);
    }

    teams.put(team.getUrn(), team);
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
