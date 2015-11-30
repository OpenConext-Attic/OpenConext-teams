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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import teams.domain.Invitation;
import teams.domain.Team;
import teams.service.TeamInviteService;

@Service
@Transactional
public class TeamInviteServiceHibernateImpl implements TeamInviteService {

  private static final Logger LOG = LoggerFactory.getLogger(TeamInviteServiceHibernateImpl.class);

  private static final long TWO_WEEKS = 14L * 24L * 60L * 60L * 1000L;

  private final EntityManager entityManager;

  @Autowired
  public TeamInviteServiceHibernateImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public Optional<Invitation> findOpenInvitation(String email, Team team) {
    checkNotNull(team);

    String jpaQl = "select i from Invitation i where i.email = :email and i.teamId = :teamId and i.accepted = false and i.declined = false";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("email", email);
    q.setParameter("teamId", team.getId());

    return q.getResultList().stream().findFirst();
  }

  @Override
  public Optional<Invitation> findInvitationByInviteId(String invitationId) {
    String jpaQl = "select i from Invitation i where i.invitationHash = :invitationId";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("invitationId", invitationId);

    return q.getResultList().stream().findFirst();
  }

  @Override
  public List<Invitation> findAllInvitationsForTeam(Team team) {
    String jpaQl = "select distinct i from Invitation i where i.teamId = :teamId order by i.email asc";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("teamId", team.getId());

    return q.getResultList();
  }

  @Override
  public List<Invitation> findInvitationsForTeamExcludeAccepted(Team team) {
    String jpaQl = "select distinct i from Invitation i where i.teamId = :teamId and i.accepted = false order by i.email asc";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("teamId", team.getId());

    return q.getResultList();
  }

  @Override
  public List<Invitation> findPendingInvitationsByEmail(String email) {
    String jpaQl = "select i from Invitation i where i.email = :email and i.accepted = true and i.declined = true";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("email", email);

    return q.getResultList();
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

  @Override
  @Scheduled(cron = "0 50 23 * * *") // every day at 23:50
  public void cleanupExpiredInvitationsJob() {
    String jpaQl = "select i from Invitation i where i.timestamp <= :expireInterval";
    TypedQuery<Invitation> q = entityManager.createQuery(jpaQl, Invitation.class);
    q.setParameter("expireInterval", new Date().getTime() - TWO_WEEKS);

    List<Invitation> results = q.getResultList();

    LOG.info("Deleting {} expired invitations", results.size());

    results.stream().forEach(this::delete);
  }
}
