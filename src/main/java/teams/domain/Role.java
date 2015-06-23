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
package teams.domain;

import edu.internet2.middleware.grouperClient.ws.beans.WsGrouperPrivilegeResult;

import java.util.HashSet;
import java.util.Set;

/**
 * The Role of a {@link Member} in a {@link Team}
 * 
 */
public enum Role {
    Admin, Member, Manager, None;

  /**
   * Get the Teams role, by the set of privileges as returned by Grouper WS
   *
   * Grouper UI explains:
   * <pre>
   * MEMBER: Entity is a member of this group
   * OPTOUT: Entity may choose to leave this group
   * OPTIN: Entity may choose to join this group
   * VIEW: Entity may see that this group exists
   * READ: Entity may see the membership list for this group
   * UPDATE: Entity may modify the membership of this group
   * ADMIN: Entity may modify group attributes, delete this group, or assign any privilege to any entity
   * </pre>
   * @param wsPrivilegeResults the Grouper WS results from getGrouperPrivileges
   * @return teams Role
   */
  public static Role fromGrouperPrivileges(WsGrouperPrivilegeResult[] wsPrivilegeResults) {
    if (wsPrivilegeResults == null || wsPrivilegeResults.length == 0) {
      return Member;
    }

    Set<String> privilegeNames = new HashSet<>();
    for (WsGrouperPrivilegeResult priv : wsPrivilegeResults) {
      // Exclude privileges that were inherited through special group memberships (etc:sysadminwhatever...)
      if (priv.getOwnerSubject() != null
              && priv.getOwnerSubject().getSourceId() != null
              && priv.getOwnerSubject().getSourceId().equals("g:isa")) {
        continue;
      }
      privilegeNames.add(priv.getPrivilegeName());
    }

    if (privilegeNames.contains("admin")) {
      return Admin;
    }
    if (privilegeNames.contains("update")) {
      return Manager;
    }
    return Member;
  }
}
