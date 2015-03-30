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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.MemberAttribute;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.service.MemberAttributeService;


/**
 * Test for {@link MemberAttributeServiceHibernateImpl}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:coin-teams-context.xml",
  "classpath:coin-teams-properties-context.xml",
  "classpath:coin-shared-context.xml"})
@TransactionConfiguration(transactionManager = "teamTransactionManager", defaultRollback = true)
@Transactional
@ActiveProfiles({"openconext", "dev"})
public class MemberAttributeServiceHibernateImplTest {

  @Autowired
  MemberAttributeService memberAttributeService;

  @Test
  public void testFindAttributesByMember() throws Exception {
    Set<Role> roleSet = new HashSet<Role>();
    roleSet.add(Role.Member);

    Member member1 = new Member(roleSet, "Member One", "member-1", "member1@example.com");
    MemberAttribute member1GuestAttr = new MemberAttribute(member1.getId(),
      MemberAttribute.ATTRIBUTE_GUEST, "true");
    member1.addMemberAttribute(member1GuestAttr);
    MemberAttribute member1DummyAttr = new MemberAttribute(member1.getId(),
      "dummyname", "dummyvalue");
    member1.addMemberAttribute(member1DummyAttr);
    memberAttributeService.saveOrUpdate(member1.getMemberAttributes());
    assertTrue(member1.isGuest());

    Member member2 = new Member(roleSet, "Member Two", "member-2", "member1@example.com");
    MemberAttribute member2DummyAttr = new MemberAttribute(member2.getId(),
      "dummyname", "dummyvalue");
    member2.addMemberAttribute(member2DummyAttr);
    memberAttributeService.saveOrUpdate(member2.getMemberAttributes());
    assertFalse(member2.isGuest());

    List<Member> members = new ArrayList<Member>();
    members.add(member1);
    members.add(member2);
    List<MemberAttribute> memberAttributeList =
      memberAttributeService.findAttributesForMembers(members);
    assertEquals(3, memberAttributeList.size());

  }
}
