package nl.surfnet.coin.teams;

import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import nl.surfnet.coin.stoker.Stoker;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.MemberAttributeService;
import nl.surfnet.coin.teams.service.VootClient;
import nl.surfnet.coin.teams.service.impl.GrouperTeamServiceWsImpl;
import nl.surfnet.coin.teams.service.impl.InMemoryMockTeamService;
import nl.surfnet.coin.teams.service.impl.JdbcServiceProviderTeamDao;
import nl.surfnet.coin.teams.service.impl.MemberAttributeServiceHibernateImpl;
import nl.surfnet.coin.teams.service.impl.VootClientImpl;
import nl.surfnet.coin.teams.service.impl.VootClientMock;
import nl.surfnet.coin.teams.service.mail.MailService;
import nl.surfnet.coin.teams.service.mail.MailServiceImpl;
import nl.surfnet.coin.teams.util.LetterOpener;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

  public static final String DEV_PROFILE_NAME = "dev";
  public static final String GROUPZY_PROFILE_NAME = "groupzy";

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(Application.class);
  }

  /*
  @Bean
  @Autowired
  public PlatformTransactionManager transactionManager(@Qualifier("teamSessionFactory") SessionFactory sessionFactory) {
    final HibernateTransactionManager transactionManager = new HibernateTransactionManager();
    transactionManager.setSessionFactory(sessionFactory);
    return transactionManager;
  }

  @Bean
  @Autowired
  public AnnotationSessionFactoryBean teamSessionFactory(@Qualifier("teamsDataSource") DataSource teamsDataSource) {
    final AnnotationSessionFactoryBean sessionFactoryBean = new AnnotationSessionFactoryBean();
    sessionFactoryBean.setAnnotatedClasses(DomainObject.class, JoinTeamRequest.class,
      Invitation.class, InvitationMessage.class, MemberAttribute.class);
    sessionFactoryBean.setDataSource(teamsDataSource);
    sessionFactoryBean.setNamingStrategy(new ImprovedNamingStrategy());
    sessionFactoryBean.setUseTransactionAwareDataSource(true);

    Properties hibernateProperties = new Properties();
    hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
    hibernateProperties.setProperty("hibernate.query.substitutions", "true 1, false 0");
    hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "none");
    hibernateProperties.setProperty("hibernate.cache.use_query_cache", "false");
    hibernateProperties.setProperty("hibernate.jdbc.wrap_result_sets", "true");
    hibernateProperties.setProperty("", "");
    hibernateProperties.setProperty("", "");
    sessionFactoryBean.setHibernateProperties(hibernateProperties);
    return sessionFactoryBean;
  }
  */

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "datasource.teams")
  public DataSource teamsDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean(name = "ebDataSource")
  @ConfigurationProperties(prefix = "datasource.eb")
  public DataSource ebDatasource() {
    return DataSourceBuilder.create().build();
  }

  @Bean(name = "teamsJdbcTemplate")
  @Autowired
  public JdbcTemplate teamsJdbcTemplate(@Qualifier("teamsDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  @Autowired
  public VootClient vootClient(Environment environment, @Value("${voot.accessTokenUri}") String accessTokenUri,
                               @Value("voot.clientId") String clientId,
                               @Value("voot.clientSecret") String clientSecret,
                               @Value("voot.scopes") String scopes,
                               @Value("voot.serviceUrl") String serviceUrl) {
    if (environment.acceptsProfiles(DEV_PROFILE_NAME)) {
      return new VootClientMock();
    }
    return new VootClientImpl(accessTokenUri, clientId, clientSecret, scopes, serviceUrl);
  }

  @Bean
  @Autowired
  public GrouperTeamService grouperTeamService(Environment environment, MemberAttributeService memberAttributeService,
                                               @Value("${defaultStemName}") String defaultStemName,
                                               @Value("${grouperPowerUser}") String grouperPowerUser) {
    if (environment.acceptsProfiles(DEV_PROFILE_NAME)) {
      return new InMemoryMockTeamService();
    }
    return new GrouperTeamServiceWsImpl(memberAttributeService, defaultStemName, grouperPowerUser);
  }

  @Bean
  public LocaleResolver localeResolver() {
    final SessionLocaleResolver localeResolver = new SessionLocaleResolver();
    localeResolver.setDefaultLocale(new Locale("en_EN"));
    return localeResolver;
  }

  @Bean
  @Autowired
  public MailService mailService(Environment environment, JavaMailSender mailSender) {
    if (environment.acceptsProfiles(DEV_PROFILE_NAME)) {
      return new LetterOpener();
    } else {
      final MailServiceImpl mailService = new MailServiceImpl();
      mailService.setMailSender(mailSender);
      return mailService;
    }
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Application.class, args);
  }


  @Configuration
  @Profile("groupzy")
  public static class GroupzyConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean(name = "groupzyDataSource")
    @ConfigurationProperties(prefix = "datasource.groupzy")
    public DataSource groupzyDataSource() {
      return DataSourceBuilder.create().build();
    }

    @Autowired
    @Bean
    @Lazy
    public Stoker stoker(@Value("${teams.groupzy.stoker.file}") String metaDataFileLocation,
                         @Value("${teams.groupzy.stoker.folder}") String detailDataFolder) throws Exception {
      return new Stoker(resourceLoader.getResource(metaDataFileLocation), resourceLoader.getResource(detailDataFolder));
    }

    @Autowired
    @Bean
    public JdbcServiceProviderTeamDao teamsDao(@Qualifier("groupzyDataSource") DataSource groupzyDataSource) {
      return new JdbcServiceProviderTeamDao(groupzyDataSource);
    }

  }
}
