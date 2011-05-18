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
    TeamResultWrapper wrapper = grouperDao.findAllTeams(STEM, 2, 3);
    assertEquals(wrapper.getTeams().size(), 3);
    assertEquals(wrapper.getTeams().get(0).getName(), "Team 3");
    assertEquals(wrapper.getTotalCount(),5);
  }
  
  @Test
  public void testFindTeams() {
    TeamResultWrapper wrapper = grouperDao.findTeams(STEM, "team", 3, 100);
    assertEquals(wrapper.getTeams().size(), 2);
    assertEquals(wrapper.getTeams().get(0).getName(), "Team 4");
    assertEquals(wrapper.getTotalCount(),5);
    
    wrapper = grouperDao.findTeams(STEM, "2",  0, 100);
    assertEquals(wrapper.getTeams().size(), 1);
    assertEquals(wrapper.getTeams().get(0).getName(), "Team 2");
    assertEquals(wrapper.getTotalCount(),1);
  }

  @Test
  public void testFindTeamByMember() {
    TeamResultWrapper wrapper = grouperDao.findTeamsByMember(STEM,PERSON_ID, "4", 0, 5);
    assertEquals(wrapper.getTeams().size(),1);
    assertEquals(wrapper.getTeams().get(0).getName(), "Team 4");
    assertEquals(wrapper.getTotalCount(),1);
   }

  @Test
  public void testFindAllTeamByMember() {
    TeamResultWrapper wrapper = grouperDao.findAllTeamsByMember(STEM,PERSON_ID, 0, 5);
    assertEquals(wrapper.getTeams().size(), 4);
    assertEquals(wrapper.getTeams().get(0).getName(), "Team 1");
    assertEquals(wrapper.getTotalCount(),4);
   }

}
