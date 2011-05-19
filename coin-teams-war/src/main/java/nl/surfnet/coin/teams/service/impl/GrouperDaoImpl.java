package nl.surfnet.coin.teams.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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
    int rowCount = this.jdbcTemplate.queryForInt(
        "select count(distinct gg.name) from grouper_groups gg, grouper_stems gs "
            + "where gg.parent_stem = gs.id and gs.name = ?", stemName);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description from grouper_groups gg, grouper_stems gs "
            + "where gg.parent_stem = gs.id and gs.name = ? order by gg.name limit ? offset ?",
        new Object[] { stemName, pageSize, offset });
    return new TeamResultWrapper(teams, rowCount);
  }

  @Override
  public TeamResultWrapper findTeams(String stemName, String partOfGroupname,
      int offset, int pageSize) {
    partOfGroupname = wildCard(partOfGroupname);
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) from grouper_groups gg, grouper_stems gs "
                + "where gg.parent_stem = gs.id and gs.name = ? and upper(gg.name) like ?",
            stemName, partOfGroupname);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description from grouper_groups gg, grouper_stems gs "
            + "where gg.parent_stem = gs.id and gs.name = ? and upper(gg.name) like ? order by gg.name limit ? offset ?",
        new Object[] { stemName, partOfGroupname, pageSize, offset });
    return new TeamResultWrapper(teams, rowCount);
  }

  @Override
  public TeamResultWrapper findAllTeamsByMember(String stemName,
      String personId, int offset, int pageSize) {
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) from grouper_groups gg, grouper_stems gs, grouper_members gm, grouper_memberships gms where"
                + " gg.parent_stem = gs.id and gm.subject_id = ? and gms.member_id = gm.id "
                + "and gms.owner_group_id = gg.id and gs.name = ?",
            personId, stemName);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description from grouper_groups gg, grouper_stems gs, grouper_members gm, grouper_memberships gms where"
            + " gg.parent_stem = gs.id and gm.subject_id = ? and gms.member_id = gm.id "
            + "and gms.owner_group_id = gg.id and gs.name = ? order by gg.name limit ? offset ?",
        new Object[] { personId, stemName, pageSize, offset });
    return new TeamResultWrapper(teams, rowCount);
  }

  @Override
  public TeamResultWrapper findTeamsByMember(String stemName, String personId,
      String partOfGroupname, int offset, int pageSize) {
    partOfGroupname = wildCard(partOfGroupname);
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) from grouper_groups gg, grouper_stems gs, grouper_members gm, grouper_memberships gms where"
                + " gg.parent_stem = gs.id and gm.subject_id = ? and gms.member_id = gm.id "
                + "and gms.owner_group_id = gg.id and gs.name = ? and upper(gg.name) like ? ",
            personId, stemName, partOfGroupname);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description from grouper_groups gg, grouper_stems gs, grouper_members gm, grouper_memberships gms where"
            + " gg.parent_stem = gs.id and gm.subject_id = ? and gms.member_id = gm.id "
            + "and gms.owner_group_id = gg.id and gs.name = ? and upper(gg.name) like ? order by gg.name limit ? offset ?",
        new Object[] { personId, stemName, partOfGroupname, pageSize, offset });
    return new TeamResultWrapper(teams, rowCount);
  }

  private List<Team> performQuery(String sql, Object[] args) {
    try {
      return this.jdbcTemplate.query(sql, args, getRowMapper());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<Team>();
    }
  }

  private String wildCard(String partOfGroupname) {
    Assert.hasText(partOfGroupname);
    partOfGroupname = ("%" + partOfGroupname + "%").toUpperCase();
    return partOfGroupname;
  }

  private RowMapper<Team> getRowMapper() {
    return new RowMapper<Team>() {
      @Override
      public Team mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString("name");
        String name = rs.getString("display_name");
        name = name.substring(name.lastIndexOf(':') + 1);
        String description = rs.getString("description");
        return new Team(id, name, description, true);
      }
    };
  }

}
