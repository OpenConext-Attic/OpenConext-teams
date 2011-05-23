/*
 * Copyright 2011 SURFnet bv
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

import org.junit.Test;

import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.util.TeamEnvironment;

/**
 * 
 * Integration test for Grouper. Note: this is not run as part of the build
 *
 */
public class GrouperTeamServiceTestIntegration {

  @Test
  public void testFindAllTeams() {
    LoginInterceptor.setLoggedInUser("urn:collab:person:test.surfguest.nl:oharsta");
    
    GrouperTeamService teamService = new GrouperTeamService();
    TeamEnvironment environment = new TeamEnvironment();
    environment.setDefaultStemName("nl:surfnet:diensten");
    environment.setGrouperPowerUser("GrouperSystem");
    teamService.setEnvironment(environment );
    teamService.findAllTeamsOld(null);
  }
  
  

}
