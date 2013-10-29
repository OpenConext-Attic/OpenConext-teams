/*
 * Copyright 2013 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.coin.teams.domain;

import edu.internet2.middleware.grouperClient.ws.beans.WsGrouperPrivilegeResult;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RoleTest {

  @Test
  public void testFromGrouperPrivileges() throws Exception {

    assertEquals("view, read, optout should result in Member", Role.Member, Role.fromGrouperPrivileges(privs("view", "read", "optout")));


    assertEquals("admin role", Role.Admin, Role.fromGrouperPrivileges(privs("member", "read", "optout", "admin", "update")));

    assertEquals("manage role", Role.Manager, Role.fromGrouperPrivileges(privs("member", "read", "optout", "update")));

    assertEquals("None", Role.None, Role.fromGrouperPrivileges(privs("optout")));

  }

  private WsGrouperPrivilegeResult[] privs(String... privNames) {
    List<WsGrouperPrivilegeResult> privs = new ArrayList<>();
    for (String name : privNames) {
      privs.add(priv(name));
    }
    return privs.toArray(new WsGrouperPrivilegeResult[privs.size()]);
  }

  private WsGrouperPrivilegeResult priv(String name) {
    WsGrouperPrivilegeResult wsGrouperPrivilegeResult = new WsGrouperPrivilegeResult();
    wsGrouperPrivilegeResult.setPrivilegeName(name);

    return wsGrouperPrivilegeResult;
  }
}
