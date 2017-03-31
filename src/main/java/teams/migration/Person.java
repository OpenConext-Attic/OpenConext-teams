package teams.migration;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "persons")
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"id", "urn", "name"})
@EqualsAndHashCode(of = "urn")
public class Person {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String urn;

  @Column
  private String name;

  @Column
  private String email;

  @Column
  private Instant created;

  @Column
  private boolean guest;

  public Person(String urn) {
    this.urn = urn;
  }

  public Person(String urn, String name, String email, boolean isGuest, Instant created) {
    this.urn = urn;
    this.name = name;
    this.email = email;
    this.guest = isGuest;
    this.created = created;
  }

}
