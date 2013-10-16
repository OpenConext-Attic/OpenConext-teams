/*
 * Copyright 2012 SURFnet bv, The Netherlands
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

public class GrouperDaoImpl {}
/*

TODO: remove this class entirely

extends AbstractGrouperDaoImpl implements GrouperDao {

  private static final Logger LOG = LoggerFactory.getLogger(GrouperDaoImpl.class);

  @Resource(name = "grouperJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  @Override
  public TeamResultWrapper findAllTeams(String personId,
      int offset, int pageSize) {
    int rowCount = this.jdbcTemplate
        .queryForInt(SQL_FIND_ALL_TEAMS_ROWCOUNT, personId);
    LOG.debug("Finding visible teams for person {}", personId);
    List<Team> teams = performQuery(
        SQL_FIND_ALL_TEAMS, new Object[] { personId, pageSize, offset });
    TeamResultWrapper teamResultWrapper = new TeamResultWrapper(teams, rowCount, offset, pageSize);
    LOG.debug("Result from grouper dao: {}", teamResultWrapper);
    return teamResultWrapper;
  }

  @Override
  public TeamResultWrapper findTeams(String personId, String partOfGroupname, int offset, int pageSize) {
    partOfGroupname = wildCard(partOfGroupname);
    int rowCount = this.jdbcTemplate.queryForInt(SQL_FIND_TEAMS_LIKE_GROUPNAME_ROWCOUNT, personId, partOfGroupname);
    LOG.debug("Finding visible teams for person {} matching groupname-part '{}' (offset {}, pageSize {})", personId, partOfGroupname, offset, pageSize);
    List<Team> teams = performQuery(SQL_FIND_TEAMS_LIKE_GROUPNAME,
        new Object[] { personId, partOfGroupname, pageSize, offset });
    TeamResultWrapper teamResultWrapper = new TeamResultWrapper(teams, rowCount, offset, pageSize);
    LOG.debug("Result from grouper dao: {}", teamResultWrapper);
    return teamResultWrapper;
  }

  @Override
  public TeamResultWrapper findAllTeamsByMember(String personId, int offset, int pageSize) {
    LOG.debug("Finding teams having member {} (offset {}, pageSize {})", personId, offset, pageSize);
    int rowCount = this.jdbcTemplate.queryForInt(SQL_FIND_ALL_TEAMS_BY_MEMBER_ROWCOUNT, personId);
    List<Team> teams = performQuery(SQL_FIND_ALL_TEAMS_BY_MEMBER, new Object[] { personId, pageSize, offset });
    addRolesToTeams(personId, teams);
    addMemberCountToTeams(personId, teams);
    TeamResultWrapper teamResultWrapper = new TeamResultWrapper(teams, rowCount, offset, pageSize);
    LOG.debug("Result from grouper dao: {}", teamResultWrapper);
    return teamResultWrapper;
  }

  @Override
  public TeamResultWrapper findTeamsByMember(String personId, String partOfGroupname, int offset, int pageSize) {
    LOG.debug("Finding teams having member {}, matching groupname-part '{}' (offset {}, pageSize {})", personId, partOfGroupname, offset, pageSize);
    partOfGroupname = wildCard(partOfGroupname);
    int rowCount = this.jdbcTemplate
        .queryForInt(SQL_FIND_TEAMS_BY_MEMBER_ROWCOUNT, personId, partOfGroupname);
    List<Team> teams = performQuery(SQL_FIND_TEAMS_BY_MEMBER,
        new Object[] { personId, partOfGroupname, pageSize, offset });
    addRolesToTeams(personId, teams);
    addMemberCountToTeams(personId, teams);
    TeamResultWrapper teamResultWrapper = new TeamResultWrapper(teams, rowCount, offset, pageSize);
    LOG.debug("Result from grouper dao: {}", teamResultWrapper);
    return teamResultWrapper;
  }


  @Override
  public List<Stem> findStemsByMember(String personId) {
    LOG.debug("Finding stems by member {}", personId);

    String sql = SQL_FIND_STEMS_BY_MEMBER;

    Object[] args = new Object[] { personId, };
    try {
      List<Stem> stems = this.jdbcTemplate.query(sql, args, new RowMapper<Stem>() {
        @Override
        public Stem mapRow(ResultSet rs, int rowNum) throws SQLException {
          String id = rs.getString("name");
          String name = rs.getString("display_name");
          String description = rs.getString("description");
          return new Stem(id, name, description);
        }
      });
      LOG.debug("Result from grouper dao: {}", stems);
      return stems;
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

  private void addRolesToTeams(String personId,
      List<Team> teams) {
    try {
      RolesRowCallbackHandler handler = new RolesRowCallbackHandler();
      String sql = SQL_ROLES_BY_TEAMS;
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

   * Difficulty here is to get a count not limited by a subject id, but limit
   * the number of groups queried to the groups belonging to a certain subject

  private void addMemberCountToTeams(String personId,
      List<Team> teams) {
    MemberCountRowCallbackHandler handler = new MemberCountRowCallbackHandler();
    this.jdbcTemplate.query(SQL_ADD_MEMBER_COUNT_TO_TEAMS, new Object[] { personId }, handler);
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

    @Override
    public void processRow(ResultSet rs) throws SQLException {
      String groupName = rs.getString("groupname");
      String permission = rs.getString("fieldname");
      /*
       * If the permission equals 'admins' then we have an Role.Admin, else we
       * have a role Role.Manager, but we must not overwrite a previous
       * Role.Admin

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

    @Override
    public void processRow(ResultSet rs) throws SQLException {
      String groupName = rs.getString("groupname");
      int count = rs.getInt("membercount");
      memberCounts.put(groupName, count);
    }
  }

}
*/
