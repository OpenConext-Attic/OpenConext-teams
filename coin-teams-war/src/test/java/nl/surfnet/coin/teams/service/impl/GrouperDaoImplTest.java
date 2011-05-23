/*
 * Copyright 2011 SURFnet bv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.coin.teams.service.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import nl.surfnet.coin.teams.domain.TeamResultWrapper;
import nl.surfnet.coin.teams.service.GrouperDao;

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
