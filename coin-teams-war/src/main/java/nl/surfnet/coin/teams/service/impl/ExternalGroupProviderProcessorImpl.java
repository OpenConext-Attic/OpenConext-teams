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
package nl.surfnet.coin.teams.service.impl;

import java.util.ArrayList;
import java.util.List;

import nl.surfnet.coin.api.client.domain.Group20;
import nl.surfnet.coin.api.client.domain.Group20Entry;
import nl.surfnet.coin.api.client.domain.GroupMembersEntry;
import nl.surfnet.coin.teams.control.ExternalGroupController;
import nl.surfnet.coin.teams.domain.ExternalGroupDetailWrapper;
import nl.surfnet.coin.teams.domain.GroupProvider;
import nl.surfnet.coin.teams.domain.GroupProviderType;
import nl.surfnet.coin.teams.domain.GroupProviderUserOauth;
import nl.surfnet.coin.teams.service.BasicAuthGroupService;
import nl.surfnet.coin.teams.service.ExternalGroupProviderProcessor;
import nl.surfnet.coin.teams.service.GroupProviderService;
import nl.surfnet.coin.teams.service.OauthGroupService;
import nl.surfnet.coin.teams.util.GroupProviderPropertyConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * An ExternalGroupProviderProcessor
 * 
 */
@Component("externalGroupProviderProcessor")
public class ExternalGroupProviderProcessorImpl implements ExternalGroupProviderProcessor {

  private static final Logger log = LoggerFactory.getLogger(ExternalGroupController.class);

  @Autowired
  private GroupProviderService groupProviderService;

  @Autowired
  private OauthGroupService oauthGroupService;

  @Autowired
  private BasicAuthGroupService basicAuthGroupService;

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.ExternalGroupProvider#getGroupProvidersForUser
   * (java.lang.String, java.util.List)
   */
  @Override
  public List<GroupProvider> getGroupProvidersForUser(String userId, List<GroupProvider> allGroupProviders) {
    // Get the OAuth keys for external group providers
    final List<GroupProviderUserOauth> groupProviderUserOauths = groupProviderService
        .getGroupProviderUserOauths(userId);

    List<GroupProvider> groupProviders = new ArrayList<GroupProvider>();

    for (GroupProviderUserOauth oauth : groupProviderUserOauths) {
      // Get the external group provider belonging to this OAuth key
      final GroupProvider groupProvider = getGroupProviderByStringIdentifier(oauth.getProvider(), allGroupProviders);
      groupProviders.add(groupProvider);

    }
    // Get all basic authentication group providers (constrained by the userId)
    for (GroupProvider groupProvider : allGroupProviders) {
      if (groupProvider.getGroupProviderType().equals(GroupProviderType.BASIC_AUTHENTICATION)
          && groupProvider.isMeantForUser(userId)) {
        groupProviders.add(groupProvider);
      }
    }
    return groupProviders;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.ExternalGroupProvider#getGroupDetails(java
   * .lang.String, java.lang.String, java.util.List, java.lang.String, int, int)
   */
  @Override
  public ExternalGroupDetailWrapper getGroupDetails(String userId, String groupId,
      List<GroupProvider> allGroupProviders, String groupProviderIdentifier, int offset, int pageSize) {
    GroupProvider groupProvider = getGroupProviderByStringIdentifier(groupProviderIdentifier, allGroupProviders);

    if (GroupProviderPropertyConverter.isGroupFromGroupProvider(groupId, groupProvider)) {
      throw new RuntimeException(String.format("GroupId(%s) can not be retrieved from external group provider(%s)",
          groupId, groupProvider));
    }
    GroupProviderUserOauth oauth = null;
    if (groupProvider.getGroupProviderType().equals(GroupProviderType.OAUTH_THREELEGGED)) {
      final List<GroupProviderUserOauth> oauthList = groupProviderService.getGroupProviderUserOauths(userId);
      oauth = getGroupProviderUserOauth(oauthList, groupProviderIdentifier);
    }

    Group20 group20 = getExternalGroupForGroupProviderId(oauth, groupProvider, userId, groupId);
    GroupMembersEntry groupMembersEntry = getExternalGroupMembersEntry(oauth, groupProvider, userId, groupId, pageSize,
        offset);
    return new ExternalGroupDetailWrapper(group20, groupMembersEntry);
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.ExternalGroupProvider#
   * getExternalGroupsForGroupProviderId
   * (nl.surfnet.coin.teams.domain.GroupProvider, java.lang.String, int, int)
   */
  @Override
  public Group20Entry getExternalGroupsForGroupProviderId(GroupProvider groupProvider, String userId, int offset,
      int pageSize) {
    final List<GroupProviderUserOauth> groupProviderUserOauths = groupProviderService
        .getGroupProviderUserOauths(userId);
    // sensible default
    Group20Entry group20Entry = new Group20Entry(new ArrayList<Group20>());
    switch (groupProvider.getGroupProviderType()) {
    case BASIC_AUTHENTICATION: {
      group20Entry = basicAuthGroupService.getGroup20Entry(groupProvider, userId, pageSize, offset);
      break;
    }
    case OAUTH_THREELEGGED: {
      GroupProviderUserOauth oauth = getGroupProviderUserOauth(groupProviderUserOauths, groupProvider.getIdentifier());
      group20Entry = oauthGroupService.getGroup20Entry(oauth, groupProvider, pageSize, offset);
      break;
    }
    default:
      throw new RuntimeException(String.format("Not supported GroupProviderType(%s)",
          groupProvider.getGroupProviderType()));
    }
    return group20Entry;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.coin.teams.service.ExternalGroupProvider#getAllGroupProviders()
   */
  @Override
  public List<GroupProvider> getAllGroupProviders() {
    return groupProviderService.getAllGroupProviders();
  }

  private GroupMembersEntry getExternalGroupMembersEntry(GroupProviderUserOauth oauth, GroupProvider groupProvider,
      String userId, String groupId, int pageSize, int offset) {
    GroupMembersEntry entry = new GroupMembersEntry();
    switch (groupProvider.getGroupProviderType()) {
    case BASIC_AUTHENTICATION: {
      entry = basicAuthGroupService.getGroupMembersEntry(groupProvider, userId, groupId, pageSize, offset);
      break;
    }
    case OAUTH_THREELEGGED: {
      entry = oauthGroupService.getGroupMembersEntry(oauth, groupProvider, groupId, pageSize, offset);
      break;
    }
    default:
      throw new RuntimeException(String.format("Not supported GroupProviderType(%s)",
          groupProvider.getGroupProviderType()));
    }
    return entry;
  }

  private Group20 getExternalGroupForGroupProviderId(GroupProviderUserOauth oauth, GroupProvider groupProvider,
      String userId, String groupId) {
    Group20 group20 = null;
    switch (groupProvider.getGroupProviderType()) {
    case BASIC_AUTHENTICATION: {
      group20 = getGroup20FromGroupProviderWithHack(groupProvider, userId, groupId);
      break;
    }
    case OAUTH_THREELEGGED: {
      group20 = oauthGroupService.getGroup20(oauth, groupProvider, groupId);
      break;
    }
    default:
      throw new RuntimeException(String.format("Not supported GroupProviderType(%s)",
          groupProvider.getGroupProviderType()));
    }
    return group20;
  }

  /**
   * TODO replace this method with
   * {@link OauthGroupService#getGroup20(nl.surfnet.coin.teams.domain.GroupProviderUserOauth, nl.surfnet.coin.teams.domain.GroupProvider, String)}
   * when all institutions comply to the OS spec
   */
  private Group20 getGroup20FromGroupProviderWithHack(GroupProvider groupProvider, String userId, String groupId) {
    Group20 group20 = null;
    try {
      group20 = basicAuthGroupService.getGroup20(groupProvider, userId, groupId);
    } catch (RuntimeException e) {
      log.debug("Group provider does not support retrieving single group, will iterate over all groups", e.getMessage());
      Group20Entry group20Entry = basicAuthGroupService.getGroup20Entry(groupProvider, userId, Integer.MAX_VALUE, 0);
      for (Group20 g : group20Entry.getEntry()) {
        if (groupId.equals(g.getId())) {
          group20 = g;
          break;
        }
      }
    }
    return group20;
  }

  private GroupProviderUserOauth getGroupProviderUserOauth(List<GroupProviderUserOauth> groupProviderUserOauths,
      String groupProviderIdentifier) {
    for (GroupProviderUserOauth groupProviderUserOauth : groupProviderUserOauths) {
      if (groupProviderUserOauth.getProvider().equals(groupProviderIdentifier)) {
        return groupProviderUserOauth;
      }
    }
    String format = String.format("GroupProviderIdentifier %s is request but no oauth user key is present",
        groupProviderIdentifier);
    throw new RuntimeException(format);
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.ExternalGroupProvider#
   * getGroupProviderByStringIdentifier(java.lang.String, java.util.List)
   */
  @Override
  public GroupProvider getGroupProviderByStringIdentifier(String provider, List<GroupProvider> allGroupProviders) {
    for (GroupProvider groupProvider : allGroupProviders) {
      if (groupProvider.getIdentifier().equals(provider)) {
        return groupProvider;
      }
    }
    return throwUnknowGroupProviderException(provider);
  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.coin.teams.service.ExternalGroupProvider#
   * getGroupProviderByLongIdentifier(java.lang.Long, java.util.List)
   */
  @Override
  public GroupProvider getGroupProviderByLongIdentifier(Long provider, List<GroupProvider> allGroupProviders) {
    for (GroupProvider groupProvider : allGroupProviders) {
      if (groupProvider.getId().equals(provider)) {
        return groupProvider;
      }
    }
    return throwUnknowGroupProviderException(provider);
  }

  private GroupProvider throwUnknowGroupProviderException(Object provider) {
    String format = String.format("Provider %s is present in the group_provider_user_oauth table, "
        + "but no configuration is present in group_provider", provider);
    throw new RuntimeException(format);
  }

}
