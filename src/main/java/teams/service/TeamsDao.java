package teams.service;

import teams.domain.TeamServiceProvider;

import java.util.Collection;

public interface TeamsDao {

   Collection<TeamServiceProvider> forTeam(String teamId);

   void persist(String teamId, Collection<String> spEntityIds);
}
