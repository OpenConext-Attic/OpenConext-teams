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

import nl.surfnet.coin.teams.domain.JoinTeamRequest;
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

/**
 *Test for {@link JoinTeamRequestServiceHibernateImpl}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
  "classpath:applicationContext.xml"})
@TransactionConfiguration(transactionManager = "teamTransactionManager", defaultRollback = true)
@Transactional
@ActiveProfiles({"openconext","dev"})
public class JoinTeamRequestServiceHibernateImplTest {

  @Autowired
  private JoinTeamRequestService joinTeamRequestService;

  @Test
  public void testFindPendingRequests() throws Exception {
    String group1 = "group1";
    String person1 = "person1";
    assertTrue(joinTeamRequestService.findPendingRequests(group1).isEmpty());
    assertNull(joinTeamRequestService.findPendingRequest(person1, group1));

    joinTeamRequestService.saveOrUpdate(new JoinTeamRequest(person1, group1, "email", "John Doe"));
    joinTeamRequestService.saveOrUpdate(new JoinTeamRequest(person1, group1, "email", "John Doe"));

    assertEquals(2, joinTeamRequestService.findPendingRequests(group1).size());
    assertNotNull(joinTeamRequestService.findPendingRequest(person1, group1));
  }
}
