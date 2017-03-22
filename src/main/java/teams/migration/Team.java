package teams.migration;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"urn","name"})
@EqualsAndHashCode(of = "urn")
public class Team {

  @Id
  @GeneratedValue
  private String id;

  @Column
  private String urn;

  @Column
  private String name;

  @Column
  private String description;

  @Column
  private boolean viewable;

  @Column
  private Instant created;

  @Formula("(select count(*) from memberships m where m.team_id = id)")
  private int membershipCount;

  @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<Membership> memberships = new HashSet<>();

  public Team(String urn, String name, String description) {
    this.urn = urn;
    this.name = name;
    this.description = description;
  }

  public Team(String urn, String name, String description, Instant created) {
    this.urn = urn;
    this.name = name;
    this.description = description;
    this.created = created;
  }

}
