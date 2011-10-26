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

package nl.surfnet.coin.teams.util;

import nl.surfnet.coin.teams.domain.Team;
import org.opensocial.models.Person;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
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
   * @param person {@link org.opensocial.models.Person}
   * @param teamId {@link String} the team Id for which the person's privileges are checked
   * @return {@link boolean} <code>true/code> if the user is admin AND/OR manager <code>false</code> if the user isn't
   */
  public boolean hasUserAdministrativePrivileges(Person person, String teamId);
}
