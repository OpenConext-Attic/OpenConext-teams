package nl.surfnet.coin.teams.domain;

public class TeamServiceProvider {
  private final String spEntityId;
  private final String teamId;

  public TeamServiceProvider(String spEntityId, String teamId) {
    this.spEntityId = spEntityId;
    this.teamId = teamId;
  }

  public String getSpEntityId() {
    return spEntityId;
  }

  public String getTeamId() {
    return teamId;
  }
}
