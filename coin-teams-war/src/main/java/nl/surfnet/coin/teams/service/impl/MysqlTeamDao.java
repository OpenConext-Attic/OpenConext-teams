package nl.surfnet.coin.teams.service.impl;

import nl.surfnet.coin.teams.domain.TeamServiceProvider;
import nl.surfnet.coin.teams.service.TeamsDao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

public class MysqlTeamDao implements TeamsDao {
  @Override
  public Collection<TeamServiceProvider> forTeam(String teamId) {
    return new ArrayList<>();
  }

  @Override
  public void persist(String teamId, Collection<String> spEntityIds) {

  }
}
