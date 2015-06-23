package teams.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Person implements Serializable {

  private final String id;
  private final String name;
  private final String email;
  private final String schacHomeOrganization;
  private final String voot_membership_role;
  private final String displayName;
  private final Set<String> tags = new HashSet<>();

  public Person(String id, String name, String email, String schacHomeOrganization, String voot_membership_role, String displayName) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.schacHomeOrganization = schacHomeOrganization;
    this.voot_membership_role = voot_membership_role;
    this.displayName = displayName;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getSchacHomeOrganization() {
    return schacHomeOrganization;
  }

  public String getVoot_membership_role() {
    return voot_membership_role;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Set<String> getTags() {
    return tags;
  }

  public void addTag(String tag) {
    tags.add(tag);
  }

  public boolean isGuest() {
    return tags.contains("guest");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Person person = (Person) o;

    if (id != null ? !id.equals(person.id) : person.id != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

}
