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
package teams.service;

import java.util.List;

import teams.domain.JoinTeamRequest;

/**
 * Interface to handle {@link JoinTeamRequest} CRUD operations
 */
public interface JoinTeamRequestService {

  /**
   * Searches for pending {@link JoinTeamRequest}'s in the database
   *
   * @param teamId {@link teams.domain.Team}
   * @return List of JoinTeamRequest, can be empty, not null
   */
  List<JoinTeamRequest> findPendingRequests(String teamId);

  /**
   * Searches for the pending {@link JoinTeamRequest}
   *
   * @param personId who wants to join
   * @param teamId   the team he wants to join
   * @return {@link JoinTeamRequest} if there is any, otherwise {@literal null}
   */
  JoinTeamRequest findPendingRequest(String personId, String teamId);

  void delete(JoinTeamRequest pendingRequest);

  void saveOrUpdate(JoinTeamRequest joinTeamRequest);
}
