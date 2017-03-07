package teams.migration;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
public class Membership implements Serializable {

    @Column
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private Instant created;

    @Id
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @Id
    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;

}
