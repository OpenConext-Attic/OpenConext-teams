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
 * Defines the interface for implementations of ProvisioningManangers that
 * listen to events that change the teams repository.
 * 
 */
public interface ProvisioningManager {

  enum Operation {
    DELETE, CREATE, UPDATE
  }

  void groupEvent(String teamId, String displayName, Operation operation);

  void teamMemberEvent(String teamId, String memberId, String role, Operation operation);

  void roleEvent(String teamId, String memberId, String role, Operation operation);

  void init(Environment env);
}
