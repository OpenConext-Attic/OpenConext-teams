package teams.migration;

import lombok.Getter;

@Getter
public class MyTeamSummary extends TeamSummary implements Comparable<MyTeamSummary> {

    private Role role;

    public MyTeamSummary(Long id, String name, int membershipCount, String description, Role role) {
        super(id, name, membershipCount, description);
        this.role = role;
    }

    @Override
    public int compareTo(MyTeamSummary other) {
        return this.getName().compareTo(other.getName());
    }
}
