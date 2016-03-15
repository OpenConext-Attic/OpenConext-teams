package teams.provision;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import static org.junit.Assert.*;

@Ignore
public class LdapUserDetailsManagerTest {

  private LdapUserDetailsManager subject ;

  @Before
  public void before() throws Exception {
    LdapContextSource ctx = new LdapContextSource();
    ctx.setUrl("ldap://ldap.test.surfconext.nl:389");
    ctx.setBase("dc=surfconext,dc=nl");
    ctx.setUserDn("cn=engine,dc=surfconext,dc=nl");
    ctx.setPassword("secret");
    ctx.afterPropertiesSet();

    LdapTemplate template = new LdapTemplate(ctx);
    template.afterPropertiesSet();

    subject = new LdapUserDetailsManager(template);
  }

  @Test
  public void testExistingPerson() throws Exception {
  }

  @Test
  public void testCreatePerson() throws Exception {

  }

  @Test
  public void testExistingOrganisation() throws Exception {
    boolean b = subject.existingOrganisation("example.com");
    assertTrue(b);

    b = subject.existingOrganisation("does.not.exists.really");
    assertFalse(b);
  }

//  @Test
//  public void testCreateOrganisation() throws Exception {
//    subject.createOrganisation("does.not.exists");
//
//
//    boolean b = subject.existingOrganisation("does.not.exists");
//    assertTrue(b);
//  }
}
