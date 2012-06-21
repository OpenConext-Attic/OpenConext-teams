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

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import nl.surfnet.coin.api.client.domain.Person;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import nl.surfnet.coin.shared.service.GenericServiceHibernateImpl;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.JoinTeamRequestService;

/**
 * Hibernate implementation for {@link JoinTeamRequestService}
 */
@Component("joinTeamRequestService")
public class JoinTeamRequestServiceHibernateImpl
        extends GenericServiceHibernateImpl<JoinTeamRequest>
        implements JoinTeamRequestService {

  public JoinTeamRequestServiceHibernateImpl() {
    super(JoinTeamRequest.class);
  }

  /**
   * Constructor
   *
   * @param type the clazz
   */
  public JoinTeamRequestServiceHibernateImpl(Class<JoinTeamRequest> type) {
    super(type);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({"unchecked"})
  @Override
  public List<JoinTeamRequest> findPendingRequests(Team team) {
    Criteria criteria = createCriteria();
    criteria.add(Restrictions.eq("groupId", team.getId()));
    criteria.addOrder(Order.asc("personId"));
    return criteria.list();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JoinTeamRequest findPendingRequest(Person person, Team team) {
    SimpleExpression personId = Restrictions.eq("personId", person.getId());
    SimpleExpression groupId = Restrictions.eq("groupId", team.getId());
    List<JoinTeamRequest> list = findByCriteria(personId, groupId);
    return CollectionUtils.isEmpty(list) ? null : list.get(0);
  }
}
