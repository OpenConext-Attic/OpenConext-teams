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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import nl.surfnet.coin.shared.service.GenericServiceHibernateImpl;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.MemberAttribute;
import nl.surfnet.coin.teams.service.MemberAttributeService;

/**
 * Hibernate implementation for {@link MemberAttributeService}
 */
@Component("memberAttributeService")
public class MemberAttributeServiceHibernateImpl
        extends GenericServiceHibernateImpl<MemberAttribute>
        implements MemberAttributeService {

  public MemberAttributeServiceHibernateImpl() {
    super(MemberAttribute.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<MemberAttribute> findAttributesForMembers(Collection<Member> members) {
    List<String> memberIds = new ArrayList<String>();
    for (Member member : members) {
      memberIds.add(member.getId());
    }
    return findByCriteria(Restrictions.in("memberId", memberIds));
  }

}
