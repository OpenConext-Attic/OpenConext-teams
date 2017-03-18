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
    contextSource.setUrl("ldap://ldap.test3.surfconext.nl:389");
    contextSource.setBase("dc=surfconext,dc=nl");
    contextSource.setUserDn("cn=admin,dc=surfconext,dc=nl");
    contextSource.setPassword("Y7xQeCBtMKR1B1fiYpXT");
    contextSource.afterPropertiesSet();
    this.subject = new LdapUserDetailsManager(new LdapTemplate(contextSource));
  }

  @Test
  public void testFindPerson() throws Exception {
    Optional<Person> personOptional = this.subject.findPersonById("urn:collab:person:hu.nl:frederique.leopold@hu.nl");
    Optional<Person> personOptional2 = this.subject.findPersonById("urn:collab:person:hu.nl:frederique.leopold_hu.nl");

    assertTrue(personOptional.get().isGuest());
  }

}
