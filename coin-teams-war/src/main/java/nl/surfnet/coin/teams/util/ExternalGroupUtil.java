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

package nl.surfnet.coin.teams.util;

import java.util.ArrayList;
import java.util.List;

import nl.surfnet.coin.api.client.domain.Group20;
import nl.surfnet.coin.api.client.domain.Group20Entry;
import nl.surfnet.coin.teams.domain.ExternalGroup;
import nl.surfnet.coin.teams.domain.GroupProvider;

/**
 * Util class for External Groups
 */
public final class ExternalGroupUtil {

  private ExternalGroupUtil() {
  }

  /**
   * Converts a {@link nl.surfnet.coin.api.client.domain.Group20Entry} to a Collection of {@link nl.surfnet.coin.teams.domain.ExternalGroup}'s
   *
   * @param groupProvider {@link nl.surfnet.coin.teams.domain.GroupProvider}
   * @param entry         {@link nl.surfnet.coin.api.client.domain.Group20Entry} returned by the GroupProvider
   * @return Collection of {@link nl.surfnet.coin.teams.domain.ExternalGroup}'s, can be empty
   */
  public static List<ExternalGroup> convertToExternalGroups(GroupProvider groupProvider, Group20Entry entry) {
    List<ExternalGroup> externalGroups = new ArrayList<ExternalGroup>();
    if (entry == null || entry.getEntry() == null) {
      return externalGroups;
    }
    for (Group20 group20 : entry.getEntry()) {
      ExternalGroup externalGroup = new ExternalGroup(group20, groupProvider);
      externalGroups.add(externalGroup);
    }
    return externalGroups;
  }

}
