package nl.surfnet.coin.teams.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    assertFalse(teamInviteService.alreadyInvited(email, team));

    Invitation invitation = new Invitation(email, team.getId(), null);
    teamInviteService.saveOrUpdate(invitation);

    assertTrue(teamInviteService.alreadyInvited(email, team));
  }

  @Test
  public void testFindInvitationByInviteId() throws Exception {
    String email = "coincalendar@gmail.com";
    Team team = mock(Team.class);
    when(team.getId()).thenReturn("team-1");

    Invitation invitation = new Invitation(email, team.getId(), null);
    String hash = invitation.getInvitationHash();

    assertNull(teamInviteService.findInvitationByInviteId(hash));
    teamInviteService.saveOrUpdate(invitation);

    assertNotNull(teamInviteService.findInvitationByInviteId(hash));
  }

  @Test
  public void testDonotFindExpiredInvitationByInviteId() throws Exception {
    String email = "coincalendar@gmail.com";
    Team team = mock(Team.class);
    when(team.getId()).thenReturn("team-1");

    Invitation invitation = new Invitation(email, team.getId(), null);
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_WEEK, -16);
    invitation.setTimestamp(calendar.getTimeInMillis() / Invitation.DATE_PRECISION_DIVIDER);

    String hash = invitation.getInvitationHash();

    assertEquals(0, teamInviteService.findAll().size());
    assertNull("Nothing saved yet", teamInviteService.findInvitationByInviteId(hash));
    teamInviteService.saveOrUpdate(invitation);
    assertEquals(1, teamInviteService.findAll().size());
    assertNull("Don't find expired", teamInviteService.findInvitationByInviteId(hash));

  }

  @Test
  public void testCleanupExpiredInvitations() throws Exception {
    String email = "coincalendar@gmail.com";
    Team team = mock(Team.class);
    when(team.getId()).thenReturn("team-1");

    Invitation oldInvitation = new Invitation(
            email, team.getId(), null);
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MONTH, -2);
    oldInvitation.setTimestamp(calendar.getTimeInMillis() / Invitation.DATE_PRECISION_DIVIDER);

    Invitation newInvitation = new Invitation(
            "coincalendar@yahoo.com", team.getId(), null);

    assertEquals(0, teamInviteService.findAll().size());
    teamInviteService.saveOrUpdate(oldInvitation);
    teamInviteService.saveOrUpdate(newInvitation);
    assertEquals(2, teamInviteService.findAll().size());
    teamInviteService.cleanupExpiredInvitations();
    assertEquals(1, teamInviteService.findAll().size());
  }

}
