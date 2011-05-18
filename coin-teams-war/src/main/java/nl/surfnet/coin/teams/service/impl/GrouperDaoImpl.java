package nl.surfnet.coin.teams.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.domain.TeamResultWrapper;
import nl.surfnet.coin.teams.service.GrouperDao;

/**
 * A {@link GrouperDao} that uses Spring jdbc
 * 
 */
@Component("grouperDao")
public class GrouperDaoImpl implements GrouperDao {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @SuppressWarnings("unchecked")
  @Override
  public TeamResultWrapper findAllTeams(String stemName, int offset,
      int pageSize) {
    int rowCount = this.jdbcTemplate.queryForInt("select count(*) from grouper_groups gg, grouper_stems gs "
                + "where gg.parent_stem = gs.id and gs.name = ?", stemName);
    List<Team> teams = this.jdbcTemplate
        .query(
            "select gg.name, gg.display_name ,gg.description from grouper_groups gg, grouper_stems gs "
                + "where gg.parent_stem = gs.id and gs.name = ? order by gg.name limit ? offset ?",
            new Object[] { stemName, pageSize, offset }, new RowMapper<Team>() {

              @Override
              public Team mapRow(ResultSet rs, int rowNum) throws SQLException {
                String id = rs.getString("name");
                String name = rs.getString("display_name");
                name = name.substring(name.lastIndexOf(":")+1);
                String description= rs.getString("description");
                return new Team(id, name, description);
              }
            });
    return new TeamResultWrapper(teams, rowCount);
  }
}
