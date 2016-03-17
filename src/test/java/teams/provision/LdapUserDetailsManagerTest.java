package teams.provision;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapOperations;
import teams.domain.Person;

import javax.naming.directory.Attributes;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LdapUserDetailsManagerTest {

  private LdapUserDetailsManager subject;
  private LdapOperations ldapOperations;

  @Before
  public void before() throws Exception {
    this.ldapOperations = mock(LdapOperations.class);
    this.subject = new LdapUserDetailsManager(ldapOperations);
  }

  @Test
  public void testExistingPersonDoesNotExist() throws Exception {
    boolean b = existingPerson(emptyList());
    assertFalse(b);
  }

  @Test
  public void testExistingPersonExists() throws Exception {
    boolean b = existingPerson(singletonList("yep"));
    assertTrue(b);
  }

  @Test
  public void testCreatePerson() throws Exception {
    when(ldapOperations.search(anyString(),anyString(), any(AttributesMapper.class))).thenReturn(emptyList());
    Person p = new Person("urn:collab:person:example.com:amazing_grace", "amazing_grace", "amazing@grace.nl", "qwerty.qwerty", "N/A", "Amazing Grace");
    subject.createPerson(p);
  }

  @Test
  public void testCreatePersonOrganizationExists() throws Exception {
    when(ldapOperations.search(anyString(),anyString(), any(AttributesMapper.class))).thenReturn(singletonList("yep"));
    Person p = new Person("urn:collab:person:example.com:amazing_grace", "amazing_grace", "amazing@grace.nl", "qwerty.qwerty", "N/A", "Amazing Grace");
    subject.createPerson(p);
  }

  private boolean existingPerson(List<Object> result) {
    when(ldapOperations.search(anyString(),anyString(), any(AttributesMapper.class))).thenReturn(result);
    return subject.existingPerson("urn:collab:person:example.com:admin");
  }
}
