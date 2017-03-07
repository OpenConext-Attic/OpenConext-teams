package teams.migration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamSummary {

    private Long id;

    private String name;

    private int membershipCount;

    private String description;
}
