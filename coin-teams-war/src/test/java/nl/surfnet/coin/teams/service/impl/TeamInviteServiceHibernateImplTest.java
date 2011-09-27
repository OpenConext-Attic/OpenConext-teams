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

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamInviteService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link TeamInviteServiceHibernateImpl}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:coin-teams-context.xml",
        "classpath:coin-teams-properties-hsqldb-context.xml",
        "classpath:coin-shared-context.xml"})
@TransactionConfiguration(transactionManager = "teamTransactionManager", defaultRollback = true)
@Transactional
public class TeamInviteServiceHibernateImplTest {

  @Autowired
  private TeamInviteService teamInviteService;

  @Test
  public void testAlreadyInvited() throws Exception {
    String email = "coincalendar@gmail.com";
    Team team = mock(Team.class);
    when(team.getId()).thenReturn("team-1");

    assertNull(teamInviteService.findInvitation(email, team));

    Invitation invitation = new Invitation(email, team.getId());
    teamInviteService.saveOrUpdate(invitation);

    assertNotNull(teamInviteService.findInvitation(email, team));
  }

  @Test
  public void testFindInvitationByInviteId() throws Exception {
    String email = "coincalendar@gmail.com";
    Team team = mock(Team.class);
    when(team.getId()).thenReturn("team-1");

    Invitation invitation = new Invitation(email, team.getId());
    String hash = invitation.getInvitationHash();

    assertNull(teamInviteService.findInvitationByInviteId(hash));
    teamInviteService.saveOrUpdate(invitation);

    assertNotNull(teamInviteService.findInvitationByInviteId(hash));
  }
  @Test
  public void testFindAllInvitationById() throws Exception {
    String email = "coincalendar@gmail.com";
    Team team = mock(Team.class);
    when(team.getId()).thenReturn("team-1");

    Invitation invitation = new Invitation(email, team.getId());
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_WEEK, -20);
    invitation.setTimestamp(calendar.getTimeInMillis());

    String hash = invitation.getInvitationHash();

    assertNull(teamInviteService.findInvitationByInviteId(hash));
    teamInviteService.saveOrUpdate(invitation);
  }

  @Test
  public void testDonotFindExpiredInvitationByInviteId() throws Exception {
    String email = "coincalendar@gmail.com";
    Team team = mock(Team.class);
    when(team.getId()).thenReturn("team-1");

    Invitation invitation = new Invitation(email, team.getId());
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_WEEK, -16);
    invitation.setTimestamp(calendar.getTimeInMillis());

    String hash = invitation.getInvitationHash();

    assertEquals(0, teamInviteService.findAll().size());
    assertNull("Nothing saved yet", teamInviteService.findInvitationByInviteId(hash));
    teamInviteService.saveOrUpdate(invitation);
    assertEquals(1, teamInviteService.findAll().size());
    assertNull("Don't find expired", teamInviteService.findInvitationByInviteId(hash));

  }

  @Test
  public void testFindInvitationsForTeam() throws Exception {
    Team team1 = mock(Team.class);
    when(team1.getId()).thenReturn("team-1");
    Team team2 = mock(Team.class);
        when(team2.getId()).thenReturn("team-2");
    Invitation invitation1 = new Invitation(
            "coincalendar@gmail.com", team1.getId());
    Invitation invitation2 = new Invitation(
            "coincalendar@yahoo.com", team1.getId());
    Invitation invitation3 = new Invitation(
            "coincalendar@yahoo.com", team2.getId());
    assertEquals(0, teamInviteService.findInvitationsForTeam(team1).size());
    teamInviteService.saveOrUpdate(invitation1);
    teamInviteService.saveOrUpdate(invitation2);
    teamInviteService.saveOrUpdate(invitation3);
    assertEquals(2, teamInviteService.findInvitationsForTeam(team1).size());
    assertEquals(3, teamInviteService.findAll().size());
  }

  @Test
  public void testCleanupExpiredInvitations() throws Exception {
    String email = "coincalendar@gmail.com";
    Team team = mock(Team.class);
    when(team.getId()).thenReturn("team-1");

    Invitation oldInvitation = new Invitation(
            email, team.getId());
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MONTH, -2);
    oldInvitation.setTimestamp(calendar.getTimeInMillis());

    Invitation newInvitation = new Invitation(
            "coincalendar@yahoo.com", team.getId());

    assertEquals(0, teamInviteService.findAll().size());
    teamInviteService.saveOrUpdate(oldInvitation);
    teamInviteService.saveOrUpdate(newInvitation);
    assertEquals(2, teamInviteService.findAll().size());
    teamInviteService.cleanupExpiredInvitations();
    assertEquals(1, teamInviteService.findAll().size());
  }

}
