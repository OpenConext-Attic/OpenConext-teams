package teams.voot;

import java.util.Objects;

public class Member {

  public final String id ;
  public final String name ;
  public final String email ;

  public Member(String id, String name, String email) {
    this.id = id;
    this.name = name;
    this.email = email;
  }

  @Override
  public String toString() {
    return "Member{" +
      "id='" + id + '\'' +
      ", name='" + name + '\'' +
      ", email='" + email + '\'' +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Member member = (Member) o;
    return Objects.equals(id, member.id) &&
      Objects.equals(name, member.name) &&
      Objects.equals(email, member.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, email);
  }
}
