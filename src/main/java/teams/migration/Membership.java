package teams.migration;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.Instant;

@Entity(name = "memberships")
@Getter
@Setter
@EqualsAndHashCode(of = {"person", "team"})
@ToString
@NoArgsConstructor
public class Membership {

  @Id
  @GeneratedValue
  private String id;

  @Column
  @Enumerated(EnumType.STRING)
  private Role role;

  @Column
  private Instant created;

  @ManyToOne
  @JoinColumn(name = "person_id")
  private Person person;

  @ManyToOne
  @JoinColumn(name = "team_id")
  @JsonIgnore
  private Team team;

  public Membership(Role role, Team team, Person person) {
    this.role = role;
    this.team = team;
    this.person = person;
  }
}
