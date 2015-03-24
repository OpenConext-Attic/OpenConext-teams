/*
 * Copyright 2011 SURFnet bv, The Netherlands
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

import nl.surfnet.coin.api.client.domain.Person;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.JoinTeamRequestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *Test for {@link JoinTeamRequestServiceHibernateImpl}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:coin-teams-context.xml",
    "classpath:coin-teams-properties-context.xml",
    "classpath:coin-shared-context.xml"})
@TransactionConfiguration(transactionManager = "teamTransactionManager", defaultRollback = true)
@Transactional
@ActiveProfiles("openconext")
public class JoinTeamRequestServiceHibernateImplTest {

  @Autowired
  private JoinTeamRequestService joinTeamRequestService;

  @Test
  public void testEmptyDb() throws Exception {
    Person mockPerson = mock(Person.class);
    when(mockPerson.getId()).thenReturn("person-1");
    Team mockTeam = mock(Team.class);
    when(mockTeam.getId()).thenReturn("team-1");

    assertEquals("No pending requests", 0, joinTeamRequestService.findPendingRequests(mockTeam).size());
    assertNull(joinTeamRequestService.findPendingRequest(mockPerson, mockTeam));
  }

  @Test
  public void testFindPendingRequests() throws Exception {
    Person mockPerson = mock(Person.class);
    when(mockPerson.getId()).thenReturn("person-1");
    Person mockPerson2 = mock(Person.class);
    when(mockPerson2.getId()).thenReturn("person-2");
    Team mockTeam = mock(Team.class);
    when(mockTeam.getId()).thenReturn("team-1");


    JoinTeamRequest joinTeamRequest = new JoinTeamRequest();
    joinTeamRequest.setPersonId(mockPerson.getId());
    joinTeamRequest.setGroupId(mockTeam.getId());
    joinTeamRequest.setTimestamp(123456789L);

    JoinTeamRequest joinTeamRequest2 = new JoinTeamRequest();
    joinTeamRequest2.setPersonId(mockPerson2.getId());
    joinTeamRequest2.setGroupId(mockTeam.getId());
    joinTeamRequest2.setTimestamp(123456790L);

    assertEquals("No pending requests", 0, joinTeamRequestService.findPendingRequests(mockTeam).size());

    joinTeamRequestService.saveOrUpdate(joinTeamRequest);
    joinTeamRequestService.saveOrUpdate(joinTeamRequest2);

    assertEquals(2, joinTeamRequestService.findPendingRequests(mockTeam).size());
    assertNotNull(joinTeamRequestService.findPendingRequest(mockPerson2, mockTeam));
  }
}
