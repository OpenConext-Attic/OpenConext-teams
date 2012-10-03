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
package nl.surfnet.coin.teams.service;

import org.springframework.core.env.Environment;

/**
 * NoOpProvisioningManager.java
 * 
 */
public class NoOpProvisioningManager implements ProvisioningManager {

  @Override
  public void groupEvent(String teamId, String displayName, Operation operation) {
  }

  @Override
  public void teamMemberEvent(String teamId, String memberId, String role, Operation operation) {
  }

  @Override
  public void roleEvent(String teamId, String memberId, String role, Operation operation) {
  }

  @Override
  public void init(Environment env) {
  }

}
