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

import java.util.List;

import nl.surfnet.coin.teams.domain.ExternalGroup;
import nl.surfnet.coin.teams.domain.TeamExternalGroup;

/**
 * Service for CRUD operations for the link between a SURFteam and an External Group
 */
public interface TeamExternalGroupDao {
// TODO: BACKLOG-329 Javadocs
  ExternalGroup getExternalGroupByIdentifier(String identifier);

  List<TeamExternalGroup> getByTeamIdentifier(String identifier);

  TeamExternalGroup getByTeamIdentifierAndExternalGroupIdentifier(String teamId, String externalGroupIdentifier);

  void saveOrUpdate(TeamExternalGroup teamExternalGroup);

  void delete(TeamExternalGroup teamExternalGroup);
}
