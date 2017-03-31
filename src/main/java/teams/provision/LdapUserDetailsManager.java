package teams.provision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapEncoder;
import teams.domain.Person;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LdapUserDetailsManager implements UserDetailsManager {

  private static final Logger LOG = LoggerFactory.getLogger(LdapUserDetailsManager.class);

  private final LdapOperations ldapOperations;

  private final Pattern RFC_822 = Pattern.compile("^(.{12})(\\d{2})(.*)$");

  public LdapUserDetailsManager(LdapOperations ldapOperations) {
    this.ldapOperations = ldapOperations;
  }

  private <T> List<T> persons(String urn, AttributesMapper<T> attributesMapper) {
    AndFilter filter = new AndFilter()
      .and(new EqualsFilter("objectclass", "collabPerson"))
      .and(new EqualsFilter("collabpersonid", urn));

    String encode = filter.encode();

    LOG.info("LDAP query {}", encode);

    //we have provided the ldapOperations with a base so here we need an empty String
    List<T> results = ldapOperations.search("", encode,
      attributesMapper);

    LOG.info("LDAP query result {}", results);
    return results;
  }

  @Override
  public Optional<teams.migration.Person> findPersonById(String urn) {
    List<teams.migration.Person> persons = persons(urn, attributes -> {
      String mail = this.safeGetAttribute(attributes, "mail");
      String cn = this.safeGetAttribute(attributes, "cn");
      String isGuest = this.safeGetAttribute(attributes, "collabpersonisguest");
      String collabpersonregistered = this.safeGetAttribute(attributes, "collabpersonregistered");
      Instant created = (collabpersonregistered != null ?
        //RFC-1123 updates RFC-822 changing the year from two digits to four
        Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(rfc822ToRFC1123(collabpersonregistered))) :
        Instant.now());
      return new teams.migration.Person(urn, cn, mail, "TRUE".equals(isGuest), created);
    });
    return persons.stream().findFirst();
  }

  private String rfc822ToRFC1123(String date) {
    Matcher matcher = RFC_822.matcher(date);
    matcher.find();
    int twoDigitYear = Integer.parseInt(matcher.group(2));
    String year = String.valueOf(twoDigitYear + (twoDigitYear < 67 ? 2000 : 1900));
    return matcher.replaceFirst(String.format("$1%s$3", year));
  }

  private String safeGetAttribute(Attributes attributes, String name) throws NamingException {
    Attribute attribute = attributes.get(name);
    return attribute == null ? null : (String) attribute.get();
  }

}
