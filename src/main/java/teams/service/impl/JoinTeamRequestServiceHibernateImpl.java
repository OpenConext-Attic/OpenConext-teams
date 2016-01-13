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
package teams.service.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import teams.domain.JoinTeamRequest;
import teams.service.JoinTeamRequestService;

@Service("joinTeamRequestService")
@Transactional
public class JoinTeamRequestServiceHibernateImpl implements JoinTeamRequestService {

  private final EntityManager entityManager;

  @Autowired
  public JoinTeamRequestServiceHibernateImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<JoinTeamRequest> findPendingRequests(String teamId) {
    String jpaQl = "select jtr from JoinTeamRequest jtr where jtr.groupId = :groupId ORDER BY jtr.personId ASC";

    return entityManager.createQuery(jpaQl, JoinTeamRequest.class)
     .setParameter("groupId", teamId)
     .getResultList();
  }

  @Override
  public JoinTeamRequest findPendingRequest(String personId, String teamId) {
    String jpaQl = "select jtr from JoinTeamRequest jtr where jtr.groupId = :groupId and jtr.personId = :personId ORDER BY jtr.personId ASC";

    List<JoinTeamRequest> resultList = entityManager.createQuery(jpaQl, JoinTeamRequest.class)
        .setParameter("groupId", teamId)
        .setParameter("personId", personId)
        .getResultList();

    return CollectionUtils.isEmpty(resultList) ? null : resultList.get(0);
  }

  @Override
  public void delete(JoinTeamRequest request) {
    entityManager.remove(request);
  }

  @Override
  public void saveOrUpdate(JoinTeamRequest joinTeamRequest) {
    if (joinTeamRequest.getId() == null) {
      entityManager.persist(joinTeamRequest);
    } else {
      entityManager.merge(joinTeamRequest);
    }
  }
}
