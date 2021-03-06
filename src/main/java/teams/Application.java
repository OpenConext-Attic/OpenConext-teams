package teams;

import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.TraceWebFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import teams.interceptor.FeatureInterceptor;
import teams.interceptor.LoginInterceptor;
import teams.interceptor.MockLoginInterceptor;
import teams.interceptor.VootApiSecurityFilter;
import teams.provision.LdapUserDetailsManager;
import teams.provision.UserDetailsManager;
import teams.repository.PersonRepository;
import teams.service.MemberAttributeService;
import teams.service.VootClient;
import teams.service.impl.VootClientImpl;
import teams.service.impl.VootClientMock;
import teams.service.mail.MailService;
import teams.service.mail.MailServiceImpl;
import teams.util.LetterOpener;
import teams.util.SpringMvcConfiguration;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration(exclude = {
  SecurityAutoConfiguration.class,
  ManagementWebSecurityAutoConfiguration.class,
  FreeMarkerAutoConfiguration.class,
  TraceWebFilterAutoConfiguration.class,
  MetricFilterAutoConfiguration.class})
public class Application extends SpringBootServletInitializer {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  public static final String DEV_PROFILE_NAME = "dev";
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
  public JdbcTemplate teamsJdbcTemplate(@Qualifier("teamsDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public UserDetailsManager userDetailsManager(@Value("${ldap.url}") String url,
                                               @Value("${ldap.base}") String base,
                                               @Value("${ldap.userDn}") String userDn,
                                               @Value("${ldap.password}") String password) {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl(url);
    contextSource.setBase(base);
    contextSource.setUserDn(userDn);
    contextSource.setPassword(password);
    contextSource.afterPropertiesSet();

    return new LdapUserDetailsManager(new LdapTemplate(contextSource));
  }

  @Bean
  public VootClient vootClient(Environment environment,
                               @Value("${voot.accessTokenUri}") String accessTokenUri,
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
  public LocaleResolver localeResolver() {
    SessionLocaleResolver localeResolver = new SessionLocaleResolver();
    localeResolver.setDefaultLocale(StringUtils.parseLocaleString("EN"));
    return localeResolver;
  }

  @Bean
  public MailService mailService(Environment environment, JavaMailSender mailSender) {
    if (environment.acceptsProfiles(DEV_PROFILE_NAME)) {
      return new LetterOpener();
    } else {
      MailServiceImpl mailService = new MailServiceImpl();
      mailService.setMailSender(mailSender);
      return mailService;
    }
  }

  @Bean
  public freemarker.template.Configuration freemarkerConfiguration() throws TemplateException, IOException {
    FreeMarkerConfigurationFactoryBean fmConfigurationFactory = new FreeMarkerConfigurationFactoryBean();
    fmConfigurationFactory.setTemplateLoaderPaths("classpath:/mailTemplates/");

    freemarker.template.Configuration configuration = fmConfigurationFactory.createConfiguration();
    configuration.setIncompatibleImprovements(freemarker.template.Configuration.VERSION_2_3_23);

    return configuration;
  }

  @Autowired
  @Bean
  public WebMvcConfigurerAdapter webMvcConfigurerAdapter(
    Environment environment,
    PersonRepository personRepository,
    @Value("${teamsURL}") String teamsURL,
    @Value("${displayExternalTeams}") Boolean displayExternalTeams,
    @Value("${displayExternalTeamMembers}") Boolean displayExternalTeamMembers,
    @Value("${displayAddExternalGroupToTeam}") Boolean displayAddExternalGroupToTeam,
    @Value("${application.version}") String applicationVersion,
    @Value("${voot.api.user}") String vootApiUser,
    @Value("${voot.api.password}") String vootApiPassword,
    ResourceLoader resourceLoader) throws Exception {

    List<HandlerInterceptor> interceptors = new ArrayList<>();

    String commitId = gitCommitId(resourceLoader, environment);
    interceptors.add(new FeatureInterceptor(displayExternalTeams, displayExternalTeamMembers, displayAddExternalGroupToTeam, commitId, applicationVersion));

    LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName("lang");
    interceptors.add(localeChangeInterceptor);

    if (environment.acceptsProfiles(DEV_PROFILE_NAME)) {
      LOG.debug("Using mock shibboleth");
      interceptors.add(new MockLoginInterceptor(teamsURL, personRepository));
    } else {
      interceptors.add(new LoginInterceptor(teamsURL, personRepository));
    }
    interceptors.add(new VootApiSecurityFilter(vootApiUser, vootApiPassword));
    return new SpringMvcConfiguration(interceptors);
  }

  private String gitCommitId(ResourceLoader resourceLoader, Environment environment) throws IOException {
    Properties gitProperties = new Properties();
    Resource resource = resourceLoader.getResource("classpath:git.properties");
    if (!resource.exists() && environment.acceptsProfiles(DEV_PROFILE_NAME)) {
      return "unknown";
    }
    gitProperties.load(resource.getInputStream());
    return gitProperties.getProperty("git.commit.id");
  }

  /**
   * See https://github.com/spring-projects/spring-boot/issues/2893. This is a JSP limitation as the issue is closed as won't-fix
   */
  @Bean
  public InternalResourceViewResolver viewResolver(@Value("${spring.mvc.view.prefix:}") String prefix, @Value("${spring.mvc.view.suffix:}") String suffix) {
    final InternalResourceViewResolver internalResourceViewResolver = new InternalResourceViewResolver();
    internalResourceViewResolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
    internalResourceViewResolver.setPrefix(prefix);
    internalResourceViewResolver.setSuffix(suffix);
    return internalResourceViewResolver;
  }

}
