/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package nl.surfnet.coin.teams.service;

import java.util.List;

import nl.surfnet.coin.api.client.domain.Group20Entry;
import nl.surfnet.coin.teams.domain.ExternalGroupDetailWrapper;
import nl.surfnet.coin.teams.domain.GroupProvider;

/**
 * All the communication and interaction with external group providers
 *
 */
public interface ExternalGroupProviderProcessor {

  List<GroupProvider> getGroupProvidersForUser(String userId, List<GroupProvider> allGroupProviders);

  ExternalGroupDetailWrapper getGroupDetails(String userId, String groupId, List<GroupProvider> allGroupProviders,
      String groupProviderIdentifier, int offset, int pageSize);

  Group20Entry getExternalGroupsForGroupProviderId(GroupProvider groupProvider, String userId, int offset, int pageSize);

  /**
   * Gets a List of {@link GroupProvider}'s for a given user
   * 
   * @return List of GroupProvider's, can be empty
   */
  List<GroupProvider> getAllGroupProviders();

  GroupProvider getGroupProviderByStringIdentifier(String provider, List<GroupProvider> allGroupProviders);

  GroupProvider getGroupProviderByLongIdentifier(Long provider, List<GroupProvider> allGroupProviders);

}