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

  private static final String STEM = "nl:surfnet:diensten";
  private static final String PERSON_ID = "urn:collab:person:test.surfguest.nl:personId";
  
  @Autowired
  private GrouperDao grouperDao;
  
  @Test
  public void testFindAllTeams() {
    TeamResultWrapper wrapper = grouperDao.findAllTeams(STEM,PERSON_ID, 2, 3);
    assertEquals(2,wrapper.getTeams().size());
    assertEquals(wrapper.getTeams().get(0).getName(), "Team 3");
    assertEquals(4,wrapper.getTotalCount());
  }
  
  @Test
  public void testFindTeams() {
    TeamResultWrapper wrapper = grouperDao.findTeams(STEM,PERSON_ID, "team", 3, 100);
    assertEquals(1,wrapper.getTeams().size());
    assertEquals(wrapper.getTeams().get(0).getName(), "Team 4");
    assertEquals(4, wrapper.getTotalCount());
    
    wrapper = grouperDao.findTeams(STEM, PERSON_ID,"2",  0, 100);
    assertEquals(wrapper.getTeams().size(), 1);
    assertEquals(wrapper.getTeams().get(0).getName(), "Team 2");
    assertEquals(wrapper.getTotalCount(),1);
  }

 

}
