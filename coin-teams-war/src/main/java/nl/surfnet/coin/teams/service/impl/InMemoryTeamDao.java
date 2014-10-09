package nl.surfnet.coin.teams.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import nl.surfnet.coin.teams.domain.TeamServiceProvider;
import nl.surfnet.coin.teams.service.TeamsDao;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTeamDao implements TeamsDao {
  private final ConcurrentHashMap<String, Collection<TeamServiceProvider>> store = new ConcurrentHashMap<>();

  @Override
  public synchronized Collection<TeamServiceProvider> forTeam(String teamId) {
    Collection<TeamServiceProvider> serviceProviders = store.get(teamId);
    if (serviceProviders == null) {
      return Collections.emptyList();
    }
    return serviceProviders;
  }

  @Override
  public synchronized void persist(final String teamId, Collection<String> spEntityIds) {
    store.remove(teamId);
    Collection<TeamServiceProvider> teamServiceProviders = Collections2.transform(spEntityIds, new Function<String, TeamServiceProvider>() {
      @Override
      public TeamServiceProvider apply(String input) {
        return new TeamServiceProvider(input, teamId);
      }
    });
    store.put(teamId, teamServiceProviders);
  }
}
