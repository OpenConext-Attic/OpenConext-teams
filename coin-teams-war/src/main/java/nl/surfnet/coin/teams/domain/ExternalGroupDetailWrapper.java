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
package nl.surfnet.coin.teams.domain;

import nl.surfnet.coin.api.client.domain.Group20;
import nl.surfnet.coin.api.client.domain.GroupMembersEntry;

/**
 * Simple wrapper to bundle group information
 *
 */
public class ExternalGroupDetailWrapper {

  private Group20 group20;
  private GroupMembersEntry groupMembersEntry;

  /**
   * @param group20
   * @param groupMembersEntry
   */
  public ExternalGroupDetailWrapper(Group20 group20, GroupMembersEntry groupMembersEntry) {
    this.group20 = group20;
    this.groupMembersEntry = groupMembersEntry;
  }

  /**
   * @return the group20
   */
  public Group20 getGroup20() {
    return group20;
  }

  /**
   * @return the groupMembersEntry
   */
  public GroupMembersEntry getGroupMembersEntry() {
    return groupMembersEntry;
  }

}
