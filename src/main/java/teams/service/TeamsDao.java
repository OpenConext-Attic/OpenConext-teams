package teams.service;

import teams.domain.TeamServiceProvider;

import java.util.Collection;

public interface TeamsDao {

  public Collection<TeamServiceProvider> forTeam(String teamId);

  public void persist(String teamId, Collection<String> spEntityIds);
}
