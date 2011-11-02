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

import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Stem;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.domain.TeamResultWrapper;
import nl.surfnet.coin.teams.service.GrouperDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link GrouperDao} that uses Spring jdbc
 * 
 */
@Component("grouperDao")
public class GrouperDaoImpl implements GrouperDao {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Override
  public TeamResultWrapper findAllTeams(String personId,
      int offset, int pageSize) {
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) "
                + "from grouper_groups gg, grouper_stems gs, grouper_members gm, "
                + "grouper_memberships gms, "
                + " grouper_fields gf, grouper_group_set ggs  "
                + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
                + " and gs.name != 'etc' "
                + "and ggs.field_id = gf.id "
                + " and gg.id = ggs.owner_group_id "
                + "and gms.owner_id = ggs.member_id "
                + " and gms.field_id = ggs.member_field_id "
                + "and ((gf.type = 'access' and gf.name = 'viewers') or gm.subject_id = ?) ", personId);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description, gs.name as stem_name, gs.display_name as stem_display_name, gs.description as stem_description "
            + "from grouper_groups gg, grouper_stems gs, grouper_members gm, "
            + "grouper_memberships gms, "
            + " grouper_fields gf, grouper_group_set ggs  "
            + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
            + " and gs.name != 'etc' "
            + " and ggs.field_id = gf.id "
            + " and gg.id = ggs.owner_group_id "
            + "and gms.owner_id = ggs.member_id "
            + " and gms.field_id = ggs.member_field_id "
            + "and ((gf.type = 'access' and gf.name = 'viewers') or gm.subject_id = ?) "
            + "order by gg.name limit ? offset ?", new Object[] { personId, pageSize, offset });
    return new TeamResultWrapper(teams, rowCount, offset, pageSize);
  }

  @Override
  public TeamResultWrapper findTeams(String personId,
      String partOfGroupname, int offset, int pageSize) {
    partOfGroupname = wildCard(partOfGroupname);
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) "
                + "from grouper_groups gg, grouper_stems gs, grouper_members gm,"
                + "grouper_memberships gms, "
                + " grouper_fields gf, grouper_group_set ggs  "
                + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
                + " and gs.name != 'etc' "
                + " and ggs.field_id = gf.id "
                + " and gg.id = ggs.owner_group_id "
                + "and gms.owner_id = ggs.member_id "
                + " and gms.field_id = ggs.member_field_id "
                + "and ((gf.type = 'access' and gf.name = 'viewers') or gm.subject_id = ?) "
                + "and upper(gg.name) like ?", personId,
            partOfGroupname);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description, gs.name as stem_name, gs.display_name as stem_display_name, gs.description as stem_description "
            + "from grouper_groups gg, grouper_stems gs, grouper_members gm,"
            + "grouper_memberships gms, "
            + " grouper_fields gf, grouper_group_set ggs  "
            + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
            + " and gs.name != 'etc' "
            + " and ggs.field_id = gf.id "
            + " and gg.id = ggs.owner_group_id "
            + "and gms.owner_id = ggs.member_id "
            + " and gms.field_id = ggs.member_field_id "
            + "and ((gf.type = 'access' and gf.name = 'viewers') or gm.subject_id = ?) "
            + "and upper(gg.name) like ? order by gg.name limit ? offset ?",
        new Object[] { personId, partOfGroupname, pageSize, offset });
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
  public TeamResultWrapper findAllTeamsByMember(String personId, int offset, int pageSize) {
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) from grouper_groups gg, grouper_stems gs, grouper_members gm, "
                + "grouper_memberships gms "
                + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
                + "and gm.subject_id = ? and gs.name != 'etc'", personId);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description, gs.name as stem_name, gs.display_name as stem_display_name, gs.description as stem_description "
            + "from grouper_groups gg, grouper_stems gs, grouper_members gm, "
            + "grouper_memberships gms  "
            + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
            + "and gm.subject_id = ? "
            + "and gs.name != 'etc' "
            + "order by gg.name limit ? offset ?", new Object[] { personId, pageSize, offset });
    addRolesToTeams(personId, teams);
    addMemberCountToTeams(personId, teams);
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
  public TeamResultWrapper findTeamsByMember(String personId,
      String partOfGroupname, int offset, int pageSize) {
    partOfGroupname = wildCard(partOfGroupname);
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) "
                + "from grouper_groups gg, grouper_stems gs, grouper_members gm, "
                + "grouper_memberships gms "
                + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
                + "and gm.subject_id = ?  and upper(gg.name) like ?", personId, partOfGroupname);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description, gs.name as stem_name, gs.display_name as stem_display_name, gs.description as stem_description "
            + "from grouper_groups gg, grouper_stems gs, grouper_members gm, "
            + "grouper_memberships gms  "
            + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
            + "and gm.subject_id = ? "
            + "and gs.name != 'etc' "
            + "and upper(gg.name) like ? order by gg.name limit ? offset ?",
        new Object[] { personId, partOfGroupname, pageSize, offset });
    addRolesToTeams(personId, teams);
    addMemberCountToTeams(personId, teams);
    return new TeamResultWrapper(teams, rowCount, offset, pageSize);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Stem> findStemsByMember(String personId) {
    String sql = "select distinct gs.name, gs.display_name, gs.description "
            + "from grouper_groups gg, grouper_stems gs, grouper_members gm, "
            + "grouper_memberships gms  "
            + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
            + "and gm.subject_id = ? "
            + "and gs.name != 'etc' ";

    Object[] args = new Object[] { personId, };
    try {
      return this.jdbcTemplate.query(sql, args, new RowMapper<Stem>() {
        @Override
        public Stem mapRow(ResultSet rs, int rowNum) throws SQLException {
          String id = rs.getString("name");
          String name = rs.getString("display_name");
          String description = rs.getString("description");
          return new Stem(id, name, description);
        }
      });
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<Stem>();
    }
  }

  private List<Team> performQuery(String sql, Object[] args) {
    try {
      return this.jdbcTemplate.query(sql, args, new RowMapper<Team>() {
        @Override
        public Team mapRow(ResultSet rs, int rowNum) throws SQLException {
          String stemId = rs.getString("stem_name");
          String stemName = rs.getString("stem_display_name");
          String stemDescription = rs.getString("stem_description");
          Stem stem = new Stem(stemId, stemName, stemDescription);

          String id = rs.getString("name");
          String name = rs.getString("display_name");
          name = name.substring(name.lastIndexOf(':') + 1);
          String description = rs.getString("description");
          return new Team(id, name, description, stem, true);
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

  private void addRolesToTeams(String personId,
      List<Team> teams) {
    try {
      RolesRowCallbackHandler handler = new RolesRowCallbackHandler();
      String sql = " select gf.name as fieldname, gg.name as groupname from grouper_memberships gms, "
          + "grouper_groups gg, grouper_fields gf, "
          + " grouper_stems gs, grouper_members gm where "
          + " gms.field_id = gf.id and  gms.owner_group_id = gg.id and "
          + " gms.member_id = gm.id "
          + " and gm.subject_id = ?  "
          + " and gg.parent_stem = gs.id "
          + " and gs.name != 'etc' "
          + " and (gf.name = 'admins' or gf.name = 'updaters') order by gg.name ";
      this.jdbcTemplate
          .query(sql, new Object[] { personId }, handler);
      Map<String, Role> roles = handler.roles;
      for (Team team : teams) {
        Role role = roles.get(team.getId());
        role = (role == null ? Role.Member : role);
        team.setViewerRole(role);
      }
    } catch (EmptyResultDataAccessException e) {
      // this we can ignore
    }

  }

  /*
   * Difficulty here is to get a count not limited by a subject id, but limit
   * the number of groups queried to the groups belonging to a certain subject
   */
  private void addMemberCountToTeams(String personId,
      List<Team> teams) {
    String sql = "select gg.name  as groupname, count(distinct gms.member_id) as membercount from "
        + " grouper_groups gg, grouper_stems gs, grouper_members gm, "
        + " grouper_memberships gms "
        + " where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
        + " and gm.subject_type = 'person' "
        + " and gs.name != 'etc' "
        + " and gg.id in (select distinct(ggo.id) from grouper_groups ggo, grouper_members gmo, grouper_memberships gmso  "
        + " where gmso.member_id = gmo.id and gmso.owner_group_id = ggo.id and gmo.subject_id = ?)   "
        + " group by gg.name  ";
    MemberCountRowCallbackHandler handler = new MemberCountRowCallbackHandler();
    this.jdbcTemplate.query(sql, new Object[] { personId }, handler);
    Map<String, Integer> memberCounts = handler.memberCounts;
    for (Team team : teams) {
      team.setNumberOfMembers(memberCounts.get(team.getId()));
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

  private class MemberCountRowCallbackHandler implements RowCallbackHandler {
    private Map<String, Integer> memberCounts;

    public MemberCountRowCallbackHandler() {
      super();
      this.memberCounts = new HashMap<String, Integer>();
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
      int count = rs.getInt("membercount");
      memberCounts.put(groupName, count);
    }
  }

}
