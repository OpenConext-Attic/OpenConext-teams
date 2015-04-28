package nl.surfnet.coin.teams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;

import org.apache.catalina.Container;
import org.apache.catalina.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import freemarker.template.TemplateException;
import nl.surfnet.coin.stoker.Stoker;
import nl.surfnet.coin.teams.interceptor.FeatureInterceptor;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.interceptor.MockLoginInterceptor;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import nl.surfnet.coin.teams.service.MemberAttributeService;
import nl.surfnet.coin.teams.service.VootClient;
import nl.surfnet.coin.teams.service.impl.GrouperTeamServiceWsImpl;
import nl.surfnet.coin.teams.service.impl.InMemoryMockTeamService;
import nl.surfnet.coin.teams.service.impl.JdbcServiceProviderTeamDao;
import nl.surfnet.coin.teams.service.impl.VootClientImpl;
import nl.surfnet.coin.teams.service.impl.VootClientMock;
import nl.surfnet.coin.teams.service.mail.MailService;
import nl.surfnet.coin.teams.service.mail.MailServiceImpl;
import nl.surfnet.coin.teams.util.LetterOpener;
import nl.surfnet.coin.teams.util.SpringMvcConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, FreeMarkerAutoConfiguration.class})
public class Application extends SpringBootServletInitializer {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  public static final String DEV_PROFILE_NAME = "dev";
  public static final String REAL_GROUPER_PROFILE_NAME = "realGrouper";
  public static final String GROUPZY_PROFILE_NAME = "groupzy";

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(Application.class);
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "datasource.teams")
  public DataSource teamsDataSource() {
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
                               @Value("${voot.clientId}") String clientId,
                               @Value("${voot.clientSecret}") String clientSecret,
                               @Value("${voot.scopes}") String scopes,
                               @Value("${voot.serviceUrl}") String serviceUrl) {
    if (environment.acceptsProfiles(DEV_PROFILE_NAME)) {
      LOG.debug("Using mock vootclient");
      return new VootClientMock();
    }
    return new VootClientImpl(accessTokenUri, clientId, clientSecret, scopes, serviceUrl);
  }

  @Bean
  @Autowired
  public GrouperTeamService grouperTeamService(Environment environment, MemberAttributeService memberAttributeService,
                                               @Value("${defaultStemName}") String defaultStemName,
                                               @Value("${grouperPowerUser}") String grouperPowerUser) {
    if (environment.acceptsProfiles(DEV_PROFILE_NAME) && !environment.acceptsProfiles(REAL_GROUPER_PROFILE_NAME)) {
      LOG.debug("Using mock grouper service");
      return new InMemoryMockTeamService();
    }
    LOG.debug("Grouper-integration using defaultStem {} and powerUser: {}", defaultStemName, grouperPowerUser);
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

  @Bean
  public freemarker.template.Configuration freemarkerConfiguration() throws TemplateException, IOException {
    FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean = new FreeMarkerConfigurationFactoryBean();
    freeMarkerConfigurationFactoryBean.setTemplateLoaderPath("classpath:/mailTemplates/");

    final freemarker.template.Configuration configuration = freeMarkerConfigurationFactoryBean.createConfiguration();
    return configuration;
  }

  @Bean
  @Autowired
  public WebMvcConfigurerAdapter webMvcConfigurerAdapter(
    Environment environment, MemberAttributeService memberAttributeService,
    @Value("${teamsURL}") final String teamsURL,
    @Value("${displayExternalTeams}") final Boolean displayExternalTeams,
    @Value("${displayExternalTeamMembers}") final Boolean displayExternalTeamMembers,
    @Value("${displayAddExternalGroupToTeam}") final Boolean displayAddExternalGroupToTeam
  ) {
    List<HandlerInterceptor> interceptors = new ArrayList<>();
    interceptors.add(new FeatureInterceptor(displayExternalTeams, displayExternalTeamMembers, displayAddExternalGroupToTeam));

    final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName("lang");
    interceptors.add(localeChangeInterceptor);

    if (environment.acceptsProfiles(DEV_PROFILE_NAME)) {
      LOG.debug("Using mock shibboleth");
      interceptors.add(new MockLoginInterceptor(teamsURL, memberAttributeService));
    } else {
      interceptors.add(new LoginInterceptor(teamsURL, memberAttributeService));
    }
    return new SpringMvcConfiguration(interceptors);
  }


  /**
   * Required because of https://github.com/spring-projects/spring-boot/issues/2825
   * As the issue says, probably can be removed as of Spring-Boot 1.3.0
   */
  @Bean
  public EmbeddedServletContainerCustomizer servletContainerCustomizer() {
    return new EmbeddedServletContainerCustomizer() {

      @Override
      public void customize(ConfigurableEmbeddedServletContainer container) {
        if (container instanceof TomcatEmbeddedServletContainerFactory) {
          customizeTomcat((TomcatEmbeddedServletContainerFactory) container);
        }
      }

      private void customizeTomcat(TomcatEmbeddedServletContainerFactory tomcatFactory) {
        tomcatFactory.addContextCustomizers(context -> {
          Container jsp = context.findChild("jsp");
          if (jsp instanceof Wrapper) {
            ((Wrapper) jsp).addInitParameter("development", "false");
          }
        });
      }
    };
  }

  /**
   * Can be removed as soon as https://github.com/spring-projects/spring-boot/issues/2893 is solved.
   * @param prefix
   * @param suffix
   * @return
   */
  @Bean
  @Autowired
  public InternalResourceViewResolver viewResolver(@Value("${spring.view.prefix:}") String prefix, @Value("${spring.view.suffix:}") String suffix) {
    final InternalResourceViewResolver internalResourceViewResolver = new InternalResourceViewResolver();
    internalResourceViewResolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
    internalResourceViewResolver.setPrefix(prefix);
    internalResourceViewResolver.setSuffix(suffix);
    return internalResourceViewResolver;
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
