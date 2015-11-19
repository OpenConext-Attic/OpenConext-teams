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
package teams.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import teams.domain.Invitation;
import teams.domain.Team;
import teams.service.TeamInviteService;

@Service
@Transactional
public class TeamInviteServiceHibernateImpl implements TeamInviteService {

  private static final long TWO_WEEKS = 14L * 24L * 60L * 60L * 1000L;
  private static final long THIRTY_DAYS = 30L * 24L * 60L * 60L * 1000L;

  private final EntityManager entityManager;

  @Autowired
  public TeamInviteServiceHibernateImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public Optional<Invitation> findOpenInvitation(String email, Team team) {
    String jpaQl = "select i from Invitation i where i.email = :email and i.teamId = :teamId and i.accepted = false and i.declined = false";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("email", email);
    q.setParameter("teamId", team.getId());

    List<Invitation> resultList = q.getResultList();

    return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
  }

  @Override
  public Optional<Invitation> findInvitationByInviteId(String invitationId) {
    cleanupExpiredInvitations();
    String jpaQl = "select i from Invitation i where i.invitationHash = :invitationId and i.timestamp >= :twoWeeksAgo";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("invitationId", invitationId);
    q.setParameter("twoWeeksAgo", (new Date().getTime()) - TWO_WEEKS);

    List<Invitation> resultList = q.getResultList();

    return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
  }

  @Override
  public Optional<Invitation> findAllInvitationById(final String invitationId) {
    cleanupExpiredInvitations();
    String jpaQl = "select i from Invitation i where i.invitationHash = :invitationId";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("invitationId", invitationId);

    List<Invitation> resultList = q.getResultList();

    return resultList.isEmpty() ? Optional.empty(): Optional.of(resultList.get(0));
  }

  @Override
  public List<Invitation> findAllInvitationsForTeam(Team team) {
    cleanupExpiredInvitations();
    String jpaQl = "select distinct i from Invitation i where i.teamId = :teamId order by i.email asc";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("teamId", team.getId());

    return q.getResultList();
  }

  @Override
  public List<Invitation> findInvitationsForTeamExcludeAccepted(Team team) {
    cleanupExpiredInvitations();
    String jpaQl = "select distinct i from Invitation i where i.teamId = :teamId and i.accepted = false order by i.email asc";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("teamId", team.getId());

    return q.getResultList();
  }

  @Override
  public List<Invitation> findPendingInvitationsByEmail(String email) {
    cleanupExpiredInvitations();
    String jpaQl = "select i from Invitation i where i.email = :email and i.accepted = true and i.declined = true";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("email", email);

    return q.getResultList();
  }

  @Override
  public void cleanupExpiredInvitations() {
    String jpaQl = "select i from Invitation i where i.timestamp <= :thirtyDaysAgo";
    final TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("thirtyDaysAgo", (new Date().getTime()) - THIRTY_DAYS);
    final List<Invitation> resultList = q.getResultList();
    for (Invitation invitation : resultList) {
      entityManager.remove(invitation);
    }
  }

  @Override
  public void delete(Invitation invitation) {
    entityManager.remove(invitation);
  }

  @Override
  public void saveOrUpdate(Invitation invitation) {
    if (invitation.getId() == null) {
      entityManager.persist(invitation);
    } else {
      entityManager.merge(invitation);
    }
  }
}
