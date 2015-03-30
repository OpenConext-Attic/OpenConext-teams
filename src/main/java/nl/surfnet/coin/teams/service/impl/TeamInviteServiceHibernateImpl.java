/*
 * Copyright 2012 SURFnet bv, The Netherlands
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

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.service.impl.deprecated.GenericServiceHibernateImpl;

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
  public Invitation findOpenInvitation(String email, Team team) {
    List<Invitation> invitations = findByCriteria(
      Restrictions.eq("email", email),
      Restrictions.eq("teamId", team.getId()),
      Restrictions.eq("accepted", false),
      Restrictions.eq("declined", false)
    );
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
  public Invitation findAllInvitationById(final String invitationId) {
    cleanupExpiredInvitations();
    List<Invitation> invitations = findByCriteria(
      Restrictions.eq("invitationHash", invitationId));
    return CollectionUtils.isEmpty(invitations) ? null : invitations.get(0);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({"unchecked"})
  @Override
  public List<Invitation> findAllInvitationsForTeam(Team team) {
    cleanupExpiredInvitations();
    Criteria criteria = createCriteria();
    criteria.add(Restrictions.eq("teamId", team.getId()));
    criteria.addOrder(Order.asc("email"));
    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    return criteria.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Invitation> findInvitationsForTeamExcludeAccepted(Team team) {
    cleanupExpiredInvitations();
    Criteria criteria = createCriteria();
    criteria.add(Restrictions.eq("teamId", team.getId()));
    criteria.add(Restrictions.eq("accepted", false));
    criteria.addOrder(Order.asc("email"));
    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    return criteria.list();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Invitation> findPendingInvitationsByEmail(String email) {
    cleanupExpiredInvitations();
    return findByCriteria(
      Restrictions.eq("email", email),
      Restrictions.ne("declined", true),
      Restrictions.ne("accepted", true)
    );
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
