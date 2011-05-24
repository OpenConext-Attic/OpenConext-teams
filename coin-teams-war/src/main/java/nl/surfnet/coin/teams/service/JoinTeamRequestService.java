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

package nl.surfnet.coin.teams.service;

import java.util.List;

import org.opensocial.models.Person;

import nl.surfnet.coin.shared.service.GenericService;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Team;

/**
 * Interface to handle {@link JoinTeamRequest} CRUD operations
 */
public interface JoinTeamRequestService extends GenericService<JoinTeamRequest> {

  /**
   * Searches for pending {@link JoinTeamRequest}'s in the database
   *
   * @param team {@link Team}
   * @return List of JoinTeamRequest, can be empty, not null
   */
  List<JoinTeamRequest> findPendingRequests(Team team);

  /**
   * Searches for the pending {@link JoinTeamRequest}
   *
   * @param person {@link Person} who wants to join
   * @param team   the {@link Team} he wants to join
   * @return {@link JoinTeamRequest} if there is any,
   *         otherwise {@literal null}
   */
  JoinTeamRequest findPendingRequest(Person person, Team team);
}
