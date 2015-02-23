package nl.surfnet.coin.teams.service.impl;

import nl.surfnet.coin.teams.domain.TeamServiceProvider;
import nl.surfnet.coin.teams.service.TeamsDao;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class JdbcServiceProviderTeamDao implements TeamsDao {

  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;

  public JdbcServiceProviderTeamDao(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
  }

  @Override
  public Collection<TeamServiceProvider> forTeam(String teamId) {
    return jdbcTemplate.query("select sp_entity_id, team_id from service_provider_group where team_id = ?", new String[]{teamId}, new RowMapper<TeamServiceProvider>() {
      @Override
      public TeamServiceProvider mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new TeamServiceProvider(rs.getString("sp_entity_id"), rs.getString("team_id"));
      }
    });
  }

  @Override
  public void persist(final String teamId, final Collection<String> spEntityIds) {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        jdbcTemplate.update("delete from service_provider_group where team_id = ?", teamId);
        for (String spEntityId : spEntityIds) {
          Date now = new DateTime().toDate();
          jdbcTemplate.update("insert into service_provider_group (team_id, sp_entity_id, created_at, updated_at) values (?, ?, ?, ?)", teamId, spEntityId, now, now);
        }

      }
    });
  }
}
