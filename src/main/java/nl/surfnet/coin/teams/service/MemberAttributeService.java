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

package nl.surfnet.coin.teams.service;

import java.util.Collection;
import java.util.List;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.MemberAttribute;
import nl.surfnet.coin.teams.service.impl.deprecated.GenericService;

/**
 * Interface to handle {@link MemberAttribute}'s
 */
public interface MemberAttributeService extends GenericService<MemberAttribute> {

  /**
   * Finds the {@link MemberAttribute}'s for the given {@link Member}'s
   *
   * @param members Collection of Member's
   * @return List of MemberAttribute's, can be empty
   */
  List<MemberAttribute> findAttributesForMembers(Collection<Member> members);

  /**
   * Finds the {@link MemberAttribute}'s for the given member id
   *
   * @param memberId unique identifier of the member
   * @return List of MemberAttribute's, can be empty
   */
  List<MemberAttribute> findAttributesForMemberId(String memberId);
}
