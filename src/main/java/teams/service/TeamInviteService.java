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

package teams.service;

import java.util.List;
import java.util.Optional;

import teams.domain.Invitation;
import teams.domain.Team;

public interface TeamInviteService {

  /**
   * Searches for an {@link Invitation} by email address and team.
   * May check for expiration.
   *
   * @param email address to send invitation to
   * @param team  {@link Team}
   * @return {@link Invitation} or {@literal null}
   */
  Optional<Invitation> findOpenInvitation(String email, Team team);

  /**
   * Searches for an {@link Invitation} by its generated hash
   * (which is sent to the invitee). May check for expiration.
   *
   * @param invitationId String that was sent to the invitee
   * @return {@link Invitation} if found, otherwise {@literal null}
   */
  Optional<Invitation> findInvitationByInviteId(String invitationId);

  /**
   * Searches for an {@link Invitation} by its generated hash
   * (which is sent to the invitee). irrespective of their expiration date.
   *
   * @param invitationId String that was sent to the invitee
   * @return {@link Invitation} if found, otherwise {@literal null}
   */
  Optional<Invitation> findAllInvitationById(String invitationId);

  /**
   * Searches for {@link Invitation}'s for a {@link Team}.
   *
   * @param team {@link Team}
   * @return List of Invitation's, can be empty.
   */
  List<Invitation> findAllInvitationsForTeam(Team team);

  /**
   * Searches for {@link Invitation}'s for a {@link Team}.
   *
   * @param team {@link Team}
   * @return List of Invitation's, can be empty.
   */
  List<Invitation> findInvitationsForTeamExcludeAccepted(Team team);

  /**
   * Searches for pending {@link Invitation}'s by email address
   *
   * @param email {@link String}
   * @return List of Invitation's, can be empty
   */
  List<Invitation> findPendingInvitationsByEmail(String email);

  /**
   * Cleans up expired {@link Invitation}'s
   */
  void cleanupExpiredInvitations();

  void delete(Invitation invitation);

  void saveOrUpdate(Invitation invitation);
}
