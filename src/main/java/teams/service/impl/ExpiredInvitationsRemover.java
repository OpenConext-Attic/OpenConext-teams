package teams.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import teams.service.TeamInviteService;

@Component
public class ExpiredInvitationsRemover {

  private static final Logger LOG = LoggerFactory.getLogger(ExpiredInvitationsRemover.class);

  @Autowired
  private TeamInviteService teamInviteService;

  @Scheduled(cron = "0 50 23 * * *") // every day at 23:50
  public void removeExpiredInvitations() {
    try {
      teamInviteService.cleanupExpiredInvitations();
    } catch (ConcurrencyFailureException e) {
      LOG.info("Failed to remove expired invitations other server instance has won");
    }
  }

}
