package nl.surfnet.coin.teams.util;

import nl.surfnet.coin.api.client.domain.Email;
import nl.surfnet.coin.api.client.domain.Person;

import java.util.Set;

public class PersonUtil {
  public static boolean isGuest(final Person person) {
    if (person == null) {
        throw new IllegalArgumentException("Person is null");
    }
    final Set<String> tags = person.getTags();
    if (tags == null) return true;
    return tags.contains("guest");
  }

  public static String getFirstEmail(final Person person) {
    if (person == null) {
      throw new IllegalArgumentException("Person is null");
    }
    final Set<Email> emails = person.getEmails();
    if (emails == null) return "";
    for (Email email : emails) {
      return email.getValue();
    }
    return "";
  }
}
