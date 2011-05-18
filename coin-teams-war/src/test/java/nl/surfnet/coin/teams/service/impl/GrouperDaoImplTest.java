package nl.surfnet.coin.teams.service.impl;

import static org.junit.Assert.*;

import nl.surfnet.coin.teams.domain.TeamResultWrapper;
import nl.surfnet.coin.teams.service.GrouperDao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:coin-teams-context.xml",
    "classpath:coin-teams-properties-hsqldb-context.xml",
    "classpath:coin-shared-context.xml"})
public class GrouperDaoImplTest {

  @Autowired
  private GrouperDao grouperDao;
  
  @Test
  public void testFindAllTeams() {
    TeamResultWrapper findAllTeams = grouperDao.findAllTeams("nl:surfnet:diensten", 2, 3);
    assertEquals(findAllTeams.getTeams().size(), 3);
    assertEquals(findAllTeams.getTeams().get(0).getName(), "Team 3");
    assertEquals(findAllTeams.getTotalCount(),5);
  }

}
