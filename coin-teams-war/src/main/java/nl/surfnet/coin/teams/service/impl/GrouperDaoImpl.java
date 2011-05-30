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
  public TeamResultWrapper findAllTeams(String stemName, String personId,
      int offset, int pageSize) {
    int rowCount = this.jdbcTemplate
        .queryForInt(
            "select count(distinct gg.name) from grouper_groups gg, grouper_stems gs, grouper_members gm, "
                + "grouper_memberships gms, grouper_rpt_group_field_v ggf  "
                + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
                + "and gs.name = ? and ggf.group_name = gg.name "
                + "and ((ggf.field_type = 'access' and ggf.field_name = 'viewers') or gm.subject_id = ?) ",
            stemName, personId);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description from grouper_groups gg, grouper_stems gs, grouper_members gm, "
            + "grouper_memberships gms, grouper_rpt_group_field_v ggf   "
            + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
            + "and gs.name = ? and ggf.group_name = gg.name "
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
            "select count(distinct gg.name) from grouper_groups gg, grouper_stems gs, grouper_members gm,"
                + "grouper_memberships gms, grouper_rpt_group_field_v ggf  "
                + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
                + "and gs.name = ? and ggf.group_name = gg.name "
                + "and ((ggf.field_type = 'access' and ggf.field_name = 'viewers') or gm.subject_id = ?) "
                + "and upper(gg.name) like ?", stemName, personId,
            partOfGroupname);
    List<Team> teams = performQuery(
        "select distinct gg.name, gg.display_name ,gg.description from grouper_groups gg, grouper_stems gs, grouper_members gm,"
            + "grouper_memberships gms, grouper_rpt_group_field_v ggf  "
            + "where gg.parent_stem = gs.id and gms.member_id = gm.id and gms.owner_group_id = gg.id "
            + "and gs.name = ? and ggf.group_name = gg.name "
            + "and ((ggf.field_type = 'access' and ggf.field_name = 'viewers') or gm.subject_id = ?) "
            + "and upper(gg.name) like ? order by gg.name limit ? offset ?",
        new Object[] { stemName, personId, partOfGroupname, pageSize, offset });
    return new TeamResultWrapper(teams, rowCount, offset, pageSize);
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
