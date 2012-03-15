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

import org.junit.Test;

import nl.surfnet.coin.teams.util.TeamEnvironment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 *  Integration test for GrouperTeamService
 *
 */
public class TeamServiceTestIntegration {

  /**
   * Test the stem name
   */
  @Test
  public void testStemName() {
    GrouperTeamServiceWsImpl teamService = new GrouperTeamServiceWsImpl();
    TeamEnvironment environment = new TeamEnvironment();
    environment.setGrouperPowerUser("GrouperSystem");
    teamService.setEnvironment(environment );
    boolean doesStemExists = teamService.doesStemExists("nl:surfnet:diensten");
    assertTrue(doesStemExists);

    doesStemExists = teamService.doesStemExists("bla-bla");
    assertFalse(doesStemExists);

    doesStemExists = teamService.doesStemExists(null);
    assertFalse(doesStemExists);

  }
  
  
}
