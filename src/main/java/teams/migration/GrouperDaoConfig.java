package teams.migration;

import nl.surfnet.coin.stoker.Stoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import teams.service.impl.JdbcServiceProviderTeamDao;

import javax.sql.DataSource;

@Configuration
public class GrouperDaoConfig {

  @Bean(name = "grouperDataSource")
  @ConfigurationProperties(prefix = "datasource.grouper")
  public DataSource groupzyDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  public JdbcMigrationDao teamsDao(@Qualifier("grouperDataSource") DataSource grouperDataSource) {
    return new JdbcMigrationDao(new JdbcTemplate(grouperDataSource));
  }
}
