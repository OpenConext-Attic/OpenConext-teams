/*
 * Copyright 2011 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.coin.teams.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import nl.surfnet.coin.teams.domain.Role;
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
  public TeamResultWrapper findAllTeams(String stemName, String personId,
      int offset, int pageSize) {
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) "
                + "from grouper_groups gg, grouper_stems gs, grouper_members gm, "
                + "grouper_memberships gms, grouper_rpt_group_field_v ggf  "
                + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
                + "and gs.name = ? and ggf.group_name = gg.name "
                + "and ((ggf.field_type = 'access' and ggf.field_name = 'viewers') or gm.subject_id = ?) ",
            stemName, personId);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description, grgv.membership_count "
            + "from grouper_groups gg, grouper_stems gs, grouper_members gm, "
            + "grouper_memberships gms, grouper_rpt_group_field_v ggf, grouper_rpt_groups_v grgv   "
            + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
            + "and gs.name = ? and ggf.group_name = gg.name "
            + "and grgv.group_name = gg.name "
            + "and ((ggf.field_type = 'access' and ggf.field_name = 'viewers') or gm.subject_id = ?) "
            + "order by gg.name limit ? offset ?", new Object[] { stemName,
            personId, pageSize, offset });
    return new TeamResultWrapper(teams, rowCount, offset, pageSize);
  }

  @Override
  public TeamResultWrapper findTeams(String stemName, String personId,
      String partOfGroupname, int offset, int pageSize) {
    partOfGroupname = wildCard(partOfGroupname);
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) "
                + "from grouper_groups gg, grouper_stems gs, grouper_members gm,"
                + "grouper_memberships gms, grouper_rpt_group_field_v ggf  "
                + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
                + "and gs.name = ? and ggf.group_name = gg.name "
                + "and ((ggf.field_type = 'access' and ggf.field_name = 'viewers') or gm.subject_id = ?) "
                + "and upper(gg.name) like ?", stemName, personId,
            partOfGroupname);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description,grgv.membership_count "
            + "from grouper_groups gg, grouper_stems gs, grouper_members gm,"
            + "grouper_memberships gms, grouper_rpt_group_field_v ggf , grouper_rpt_groups_v grgv   "
            + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
            + "and gs.name = ? and ggf.group_name = gg.name "
            + " and grgv.group_name = gg.name "
            + "and ((ggf.field_type = 'access' and ggf.field_name = 'viewers') or gm.subject_id = ?) "
            + "and upper(gg.name) like ? order by gg.name limit ? offset ?",
        new Object[] { stemName, personId, partOfGroupname, pageSize, offset });
    return new TeamResultWrapper(teams, rowCount, offset, pageSize);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.GrouperDao#findAllTeamsByMember(java.lang
   * .String, java.lang.String, int, int)
   */
  @Override
  public TeamResultWrapper findAllTeamsByMember(String stemName,
      String personId, int offset, int pageSize) {
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) from grouper_groups gg, grouper_stems gs, grouper_members gm, "
                + "grouper_memberships gms "
                + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
                + "and gs.name = ?  " + "and gm.subject_id = ? ", stemName,
            personId);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description, grgv.membership_count "
            + "from grouper_groups gg, grouper_stems gs, grouper_members gm, "
            + "grouper_memberships gms, grouper_rpt_groups_v grgv   "
            + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
            + "and grgv.group_name = gg.name "
            + "and gs.name = ? and gm.subject_id = ? "
            + "order by gg.name limit ? offset ?", new Object[] { stemName,
            personId, pageSize, offset });
    Map<String, Role> roles = retrieveRoles(
        " select gf.name as fieldname, gg.name as groupname from grouper_memberships gms, grouper_groups gg, grouper_fields gf, "
            + " grouper_stems gs, grouper_members gm where "
            + " gms.field_id = gf.id and  gms.owner_group_id = gg.id and "
            + " gms.member_id = gm.id and gs.name = ? "
            + " and gm.subject_id = ?  "
            + " and gg.parent_stem = gs.id "
            + " and (gf.name = 'admins' or gf.name = 'updaters') order by gg.name ",
        new Object[] { stemName, personId });
    assignViewerRolesToTeam(teams, roles);
    return new TeamResultWrapper(teams, rowCount, offset, pageSize);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.GrouperDao#findTeamsByMember(java.lang.String
   * , java.lang.String, java.lang.String, int, int)
   */
  @Override
  public TeamResultWrapper findTeamsByMember(String stemName, String personId,
      String partOfGroupname, int offset, int pageSize) {
    partOfGroupname = wildCard(partOfGroupname);
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) "
                + "from grouper_groups gg, grouper_stems gs, grouper_members gm, "
                + "grouper_memberships gms "
                + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
                + "and gs.name = ?  "
                + "and gm.subject_id = ?  and upper(gg.name) like ?", stemName,
            personId, partOfGroupname);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description, grgv.membership_count "
            + "from grouper_groups gg, grouper_stems gs, grouper_members gm, "
            + "grouper_memberships gms, grouper_rpt_groups_v grgv   "
            + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
            + "and grgv.group_name = gg.name "
            + "and gs.name = ? and gm.subject_id = ? "
            + "and upper(gg.name) like ? order by gg.name limit ? offset ?",
        new Object[] { stemName, personId, partOfGroupname, pageSize, offset });
    Map<String, Role> roles = retrieveRoles(
        " select gf.name as fieldname, gg.name as groupname from grouper_memberships gms, grouper_groups gg, grouper_fields gf, "
            + " grouper_stems gs, grouper_members gm where "
            + " gms.field_id = gf.id and  gms.owner_group_id = gg.id and "
            + " gms.member_id = gm.id "
            + " and gs.name = ? and  gm.subject_id = ?  "
            + " and gg.parent_stem = gs.id "
            + " and (gf.name = 'admins' or gf.name = 'updaters') and "
            + "upper(gg.name) like ? order by gg.name ", new Object[] {
            stemName, personId, partOfGroupname });
    assignViewerRolesToTeam(teams, roles);
    return new TeamResultWrapper(teams, rowCount, offset, pageSize);
  }

  private List<Team> performQuery(String sql, Object[] args) {
    try {
      return this.jdbcTemplate.query(sql, args, new RowMapper<Team>() {
        @Override
        public Team mapRow(ResultSet rs, int rowNum) throws SQLException {
          String id = rs.getString("name");
          String name = rs.getString("display_name");
          name = name.substring(name.lastIndexOf(':') + 1);
          String description = rs.getString("description");
          int count = rs.getInt("membership_count");
          Team team = new Team(id, name, description, true);
          team.setNumberOfMembers(count);
          return team;
        }
      });
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<Team>();
    }
  }

  private String wildCard(String partOfGroupname) {
    Assert.hasText(partOfGroupname);
    partOfGroupname = ("%" + partOfGroupname + "%").toUpperCase();
    return partOfGroupname;
  }

  private void assignViewerRolesToTeam(List<Team> teams, Map<String, Role> roles) {
    for (Team team : teams) {
      Role role = roles.get(team.getId());
      role = (role == null ? Role.Member : role);
      team.setViewerRole(role);
    }

  }

  private Map<String, Role> retrieveRoles(String sql, Object[] args) {
    try {
      RolesRowCallbackHandler handler = new RolesRowCallbackHandler();
      this.jdbcTemplate.query(sql, args, handler);
      return handler.roles;
    } catch (EmptyResultDataAccessException e) {
      return new HashMap<String, Role>();
    }

  }

  private class RolesRowCallbackHandler implements RowCallbackHandler {

    private Map<String, Role> roles;

    public RolesRowCallbackHandler() {
      super();
      this.roles = new HashMap<String, Role>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.jdbc.core.RowCallbackHandler#processRow(java.sql.
     * ResultSet)
     */
    @Override
    public void processRow(ResultSet rs) throws SQLException {
      String groupName = rs.getString("groupname");
      String permission = rs.getString("fieldname");
      /*
       * If the permission equals 'admins' then we have an Role.Admin, else we
       * have a role Role.Manager, but we must not overwrite a previous
       * Role.Admin
       */
      Role role = roles.get(groupName);
      if (!Role.Admin.equals(role)) {
        roles.put(groupName, permission.equals("admins") ? Role.Admin
            : Role.Manager);
      }
    }
  }

}
