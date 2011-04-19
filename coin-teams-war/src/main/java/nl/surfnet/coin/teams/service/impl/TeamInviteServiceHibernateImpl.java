package nl.surfnet.coin.teams.service.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import nl.surfnet.coin.shared.service.GenericServiceHibernateImpl;
import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamInviteService;

/**
 * Hibernate implementation for {@link TeamInviteService}
 */
@Component("teamInviteService")
public class TeamInviteServiceHibernateImpl
        extends GenericServiceHibernateImpl<Invitation>
        implements TeamInviteService {

  private static final long TWO_WEEKS = 14L * 24L * 60L * 60L * 1000L;
  private static final long THIRTY_DAYS = 30L * 24L * 60L * 60L * 1000L;

  /**
   * Default constructor
   */
  public TeamInviteServiceHibernateImpl() {
    super(Invitation.class);
  }

  /**
   * Constructor
   *
   * @param type the clazz
   */
  public TeamInviteServiceHibernateImpl(Class<Invitation> type) {
    super(type);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Invitation findInvitation(String email, Team team) {
    List<Invitation> invitations = findByCriteria(
        Restrictions.eq("email", email),
        Restrictions.eq("teamId", team.getId()));
    return CollectionUtils.isEmpty(invitations) ? null : invitations.get(0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Invitation findInvitationByInviteId(String invitationId) {
    cleanupExpiredInvitations();
    long twoWeeksAgo = (new Date().getTime()) - TWO_WEEKS;
    List<Invitation> invitations = findByCriteria(
            Restrictions.eq("invitationHash", invitationId),
            Restrictions.ge("timestamp", twoWeeksAgo));
    return CollectionUtils.isEmpty(invitations) ? null : invitations.get(0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Invitation> findInvitationsForTeam(Team team) {
    cleanupExpiredInvitations();
    return findByCriteria(Restrictions.eq("teamId", team.getId()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cleanupExpiredInvitations() {
    long thirtyDaysAgo = (new Date().getTime()) - THIRTY_DAYS;
    List<Invitation> invitations = findByCriteria(
            Restrictions.lt("timestamp", thirtyDaysAgo));
    for (Invitation invitation : invitations) {
      delete(invitation);
    }
  }

}
