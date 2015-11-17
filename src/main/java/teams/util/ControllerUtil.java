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

import teams.domain.Person;
import teams.domain.Team;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

public interface ControllerUtil {

  /**
   * Get the team from the {@link javax.servlet.http.HttpServletRequest} request.
   *
   * @param request the {@link javax.servlet.http.HttpServletRequest}
   * @return The {@link nl.surfnet.coin.teams.domain.Team} team
   * @throws RuntimeException if the team cannot be found
   */
  public Team getTeam(HttpServletRequest request);

  /**
   * Get the team from the {@link String} teamId.
   *
   * @param teamId the {@link String} teamId
   * @return The {@link Team} team
   * @throws RuntimeException if the team cannot be found
   */
  public Team getTeamById(String teamId);

  /**
   * Checks if the current user has administrative privileges (whether he is admin OR manager) for a given team.
   *
   * @param person {@link nl.surfnet.coin.teams.domain.Person}
   * @param teamId {@link String} the team Id for which the person's privileges are checked
   * @return {@link boolean} <code>true/code> if the user is admin AND/OR manager <code>false</code> if the user isn't
   */
  public boolean hasUserAdministrativePrivileges(Person person, String teamId);

  /**
   * Checks if the current user has admin privileges for a given team.
   *
   * @param person {@link Person}
   * @param teamId {@link String} the team Id for which the person's privileges are checked
   * @return {@link boolean} <code>true/code> if the user is admin AND/OR manager <code>false</code> if the user isn't
   */
  public boolean hasUserAdminPrivileges(Person person, String teamId);

  /**
   * Check if a {@link Person} is member of the given {@link Team}
   *
   * @param personId {@link String} the person identifier
   * @param team   {@link Team} the team
   * @return {@literal true} if the user is member of the team, {@literal false} if the user isn't member
   */
  public boolean isPersonMemberOfTeam(String personId, Team team);

  /**
   * Makes {@link MimeMultipart} with a plain text and html version of the mail
   *
   * @param plainText contents of the plain text part of the mail
   * @param html      contents of the html part of the mail
   * @return MimeMultipart
   * @throws {@link MessagingException} if making the multipart fails
   */
  public MimeMultipart getMimeMultipartMessageBody(String plainText, String html)
      throws MessagingException;
}
