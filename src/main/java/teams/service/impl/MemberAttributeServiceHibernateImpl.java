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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import teams.domain.Member;
import teams.domain.MemberAttribute;
import teams.service.MemberAttributeService;

@Transactional
@Service
public class MemberAttributeServiceHibernateImpl implements MemberAttributeService {

  private final EntityManager entityManager;

  @Autowired
  public MemberAttributeServiceHibernateImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<MemberAttribute> findAttributesForMembers(Collection<Member> members) {
    List<String> memberIds = new ArrayList<>();
    for (Member member : members) {
      memberIds.add(member.getId());
    }
    String jpaQl = "select ma from MemberAttribute ma where ma.memberId in :memberIds";
    final TypedQuery<MemberAttribute> q = entityManager.createQuery(jpaQl, MemberAttribute.class);
    q.setParameter("memberIds", memberIds);
    return q.getResultList();
  }

  @Override
  public List<MemberAttribute> findAttributesForMemberId(String memberId) {
    String jpaQl = "select ma from MemberAttribute ma where ma.memberId = :memberId";
    final TypedQuery<MemberAttribute> q = entityManager.createQuery(jpaQl, MemberAttribute.class);
    q.setParameter("memberId", memberId);
    return q.getResultList();
  }

  @Override
  public void saveOrUpdate(List<MemberAttribute> memberAttributes) {
    for (MemberAttribute memberAttribute : memberAttributes) {
      if (memberAttribute.getId() == null) {
        entityManager.persist(memberAttribute);
      } else {
        entityManager.merge(memberAttribute);
      }
    }
  }

}
