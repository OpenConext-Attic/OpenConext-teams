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

package teams.util;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import teams.domain.Invitation;
import teams.domain.Person;
import teams.domain.Team;

public interface ControllerUtil {

  /**
   * Get the team from the {@link javax.servlet.http.HttpServletRequest} request.
   *
   * @param request the {@link javax.servlet.http.HttpServletRequest}
   * @return The {@link teams.domain.Team} team
   * @throws RuntimeException if the team cannot be found
   */
  Team getTeam(HttpServletRequest request);

  /**
   * Get the team from the {@link String} teamId.
   *
   * @param teamId the {@link String} teamId
   * @return The {@link Team} team
   * @throws RuntimeException if the team cannot be found
   */
  Team getTeamById(String teamId);

  /**
   * Checks if the current user has administrative privileges (whether he is admin OR manager) for a given team.
   *
   * @param person {@link teams.domain.Person}
   * @param team {@link teams.domain.Team} the team for which the person's privileges are checked
   * @return {@link boolean} <code>true/code> if the user is admin AND/OR manager <code>false</code> if the user isn't
   */
  boolean hasUserAdministrativePrivileges(Person person, Team team);

  /**
   * Checks if the current user has admin privileges for a given team.
   *
   * @param person {@link Person}
   * @param team {@link teams.domain.Team} the team for which the person's privileges are checked
   * @return {@link boolean} <code>true/code> if the user is admin AND/OR manager <code>false</code> if the user isn't
   */
  boolean hasUserAdminPrivileges(Person person, Team team);

  /**
   * Check if a {@link Person} is member of the given {@link Team}
   *
   * @param person {@link Person} the person
   * @param team   {@link Team} the team
   * @return {@literal true} if the user is member of the team, {@literal false} if the user isn't member
   */
  boolean isPersonMemberOfTeam(Person person, Team team);

  void sendInvitationMail(Team team, Invitation invitation, String subject, Person inviter);

  void sendDeclineMail(Person memberToAdd, Team team, Locale locale);

  void sendAcceptMail(Person memberToAdd, Team team, Locale locale);

  void sendJoinTeamMail(Team team, Person person, String message, Locale locale);
}
