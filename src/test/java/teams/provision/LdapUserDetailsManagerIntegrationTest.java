package teams.provision;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import teams.migration.Person;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

@Ignore
public class LdapUserDetailsManagerIntegrationTest {

  private LdapUserDetailsManager subject;

  @Before
  public void before() throws Exception {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl("ldap://ldap.test2.surfconext.nl:389");
    contextSource.setBase("dc=surfconext,dc=nl");
    contextSource.setUserDn("cn=engine,dc=surfconext,dc=nl");
    contextSource.setPassword("secret");
    contextSource.afterPropertiesSet();
    this.subject = new LdapUserDetailsManager(new LdapTemplate(contextSource));
  }

  @Test
  public void testFindPerson() throws Exception {
    Optional<Person> personOptional = this.subject.findPersonById("urn:collab:person:surfnet.nl:kinkhorst");
    personOptional = this.subject.findPersonById("urn:collab:person:surfnet.nl:nope");
    personOptional = this.subject.findPersonById("urn:collab:person:example.com:admin");

    assertTrue(personOptional.get().isGuest());
  }

}
