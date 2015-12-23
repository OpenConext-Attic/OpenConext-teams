package teams;

import javax.sql.DataSource;

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

import nl.surfnet.coin.stoker.Stoker;
import teams.service.impl.JdbcServiceProviderTeamDao;

@Configuration
@Profile("groupzy")
public class GroupzyConfig {

  @Autowired
  private ResourceLoader resourceLoader;

  @Bean(name = "groupzyDataSource")
  @ConfigurationProperties(prefix = "datasource.groupzy")
  public DataSource groupzyDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  @Lazy
  public Stoker stoker(@Value("${teams.groupzy.stoker.file}") String metaDataFileLocation,
      @Value("${teams.groupzy.stoker.folder}") String detailDataFolder) throws Exception {
    return new Stoker(resourceLoader.getResource(metaDataFileLocation), resourceLoader.getResource(detailDataFolder));
  }

  @Bean
  public JdbcServiceProviderTeamDao teamsDao(@Qualifier("groupzyDataSource") DataSource groupzyDataSource) {
    return new JdbcServiceProviderTeamDao(groupzyDataSource);
  }
}
