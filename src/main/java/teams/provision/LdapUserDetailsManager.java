package teams.provision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import teams.domain.Person;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.util.List;

public class LdapUserDetailsManager implements UserDetailsManager {

  private static final Logger LOG = LoggerFactory.getLogger(LdapUserDetailsManager.class);

  private final LdapOperations ldapOperations;

  public LdapUserDetailsManager(LdapOperations ldapOperations) {
    this.ldapOperations = ldapOperations;
  }

  @Override
  public boolean existingPerson(String urn) {
    AndFilter filter = new AndFilter()
      .and(new EqualsFilter("objectclass", "collabPerson"))
      .and(new EqualsFilter("collabpersonid", urn));

    String encode = filter.encode();

    LOG.debug("LDAP query {}", encode);

    //we have provided the ldapOPerations with a base so here we need an empty String
    List<String> persons = ldapOperations.search("", encode,
      (AttributesMapper<String>) attributes -> (String) attributes.get("collabpersonid").get());

    LOG.debug("LDAP query result {}", persons);
    return !persons.isEmpty();
  }

  @Override
  public void createPerson(Person person) {
    Attributes userAttributes = new BasicAttributes();
    userAttributes.put("collabpersonid", person.getId());
    userAttributes.put("cn", person.getDisplayName());
    userAttributes.put("mail", person.getEmail());

    BasicAttribute classAttribute = new BasicAttribute("objectclass");
    classAttribute.add("collabPerson");
    classAttribute.add("person");
    classAttribute.add("top");

    userAttributes.put(classAttribute);

    String dn = String.format("uid=%s,o=%s", person.getName(), person.getSchacHomeOrganization());

    LOG.debug("LDAP bind {} for {}", userAttributes, dn);

    ldapOperations.bind(dn, null, userAttributes);
  }

}
