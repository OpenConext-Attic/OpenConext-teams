package teams.migration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class GrouperDaoConfig {

  @Bean(name = "grouperDataSource")
  @ConfigurationProperties(prefix = "datasource.grouper")
  public DataSource grouperDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  public JdbcMigrationDao teamsDao(@Qualifier("grouperDataSource") DataSource grouperDataSource) {
    return new JdbcMigrationDao(new JdbcTemplate(grouperDataSource));
  }
}
