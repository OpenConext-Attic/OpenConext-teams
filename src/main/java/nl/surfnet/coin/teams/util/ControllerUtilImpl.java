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

package nl.surfnet.coin.teams.util;

import nl.surfnet.coin.teams.domain.Person;
import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.GrouperTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * This class includes methods that are often used by controllers
 */
@Component("controllerUtil")
public class ControllerUtilImpl implements ControllerUtil {

  @Autowired
  private GrouperTeamService grouperTeamService;

  /**
   * Get the team from the {@link HttpServletRequest} request.
   *
   * @param request the {@link HttpServletRequest}
   * @return The {@link Team} team
   * @throws RuntimeException if the team cannot be found
   */
  public Team getTeam(HttpServletRequest request) {
    String teamId = request.getParameter("team");
    return getTeamById(teamId);
  }

  /**
   * Get the team from the {@link String} teamId.
   *
   * @param teamId the {@link String} teamId
   * @return The {@link Team} team
   * @throws RuntimeException if the team cannot be found
   */
  public Team getTeamById(String teamId) {
    Team team = null;
    if (StringUtils.hasText(teamId)) {
      team = grouperTeamService.findTeamById(teamId);
    }
    if (team == null) {
      throw new RuntimeException("Team (" + teamId + ") not found");
    }
    return team;
  }

  /**
   * Checks if the current user has administrative privileges (whether he is admin OR manager) for a given team.
   *
   * @param person {@link Person}
   * @param teamId {@link String} the team Id for which the person's privileges are checked
   * @return {@link boolean} <code>true/code> if the user is admin AND/OR manager <code>false</code> if the user isn't
   */
  public boolean hasUserAdministrativePrivileges(Person person, String teamId) {
    // Check if the requester is member of the team AND
    // Check if the requester has the role admin or manager, so he is allowed to invite new members.
    Member member = grouperTeamService.findMember(teamId, person.getId());
    return member != null && (member.getRoles().contains(Role.Admin) || member.getRoles().contains(Role.Manager));
  }

  /**
   * Checks if the current user has admin privileges for a given team.
   *
   * @param person {@link Person}
   * @param teamId {@link String} the team Id for which the person's privileges are checked
   * @return {@link boolean} <code>true/code> if the user is admin AND/OR manager <code>false</code> if the user isn't
   */
  public boolean hasUserAdminPrivileges(Person person, String teamId) {
    // Check if the requester is member of the team AND
    // Check if the requester has the role admin or manager, so he is allowed to invite new members.
    Member member = grouperTeamService.findMember(teamId, person.getId());
    return member != null && (member.getRoles().contains(Role.Admin));
  }

  /**
   * {@inheritDoc}
   */
  public boolean isPersonMemberOfTeam(String personId, Team team) {
    List<Member> members = team.getMembers();
    boolean isMember = false;

    for (Member member : members) {
      if (member.getId().equals(personId)) {
        isMember = true;
      }
    }
    return isMember;
  }

  @Override
  public MimeMultipart getMimeMultipartMessageBody(String plainText, String html) throws MessagingException {
    MimeMultipart multiPart = new MimeMultipart("alternative");
    MimeBodyPart textPart = new MimeBodyPart();
    textPart.setText(plainText, "utf-8");

    MimeBodyPart htmlPart = new MimeBodyPart();
    htmlPart.setContent(html, "text/html; charset=utf-8");

    multiPart.addBodyPart(textPart); // least important
    multiPart.addBodyPart(htmlPart); // most important
    return multiPart;
  }

}