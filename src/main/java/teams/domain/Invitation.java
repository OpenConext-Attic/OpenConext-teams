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
package teams.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.SortNatural;
import org.springframework.util.CollectionUtils;

import teams.util.InvitationGenerator;

@SuppressWarnings("serial")
@Entity
@Table(name = "invitations")
@Proxy(lazy = false)
public class Invitation extends DomainObject {

  private static final long TWO_WEEKS = 14L * 24L * 60L * 60L * 1000L;

  @Column(name = "group_id", nullable = false)
  private String teamId;

  @Column(name = "mailaddress", nullable = false)
  private String email;

  @Column(nullable = false)
  private long timestamp;

  @Column(name = "invitation_uiid", nullable = false)
  private String invitationHash;

  @Column(name = "denied")
  private boolean declined;

  @Column(name = "accepted")
  private boolean accepted;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "invitation")
  @SortNatural
  private List<InvitationMessage> invitationMessages;

  @Enumerated(EnumType.STRING)
  @Column(name = "intended_role", nullable = true)
  private Role intendedRole;

  @Enumerated(EnumType.STRING)
  private Language language = Language.English;

  /**
   * Constructor Hibernate needs when fetching results from the db.
   * Do not use to create new Invitations.
   */
  public Invitation() {
    this(null, null);
  }

  /**
   * Constructor with the most common fields
   *
   * @param email  address of the person to invite
   * @param teamId id of the team the person will join
   */
  public Invitation(String email, String teamId) {
    this.setEmail(email);
    this.setTeamId(teamId);
    this.setInvitationHash();
    this.setTimestamp(new Date().getTime());
    this.setInvitationMessages(new ArrayList<>());
  }

  /**
   * @param teamId the teamId to set
   */
  public void setTeamId(String teamId) {
    this.teamId = teamId;
  }

  /**
   * @return the teamId
   */
  public String getTeamId() {
    return teamId;
  }

  /**
   * @param email the email to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @return timestamp when the invitation was last updates
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * @param timestamp to indicate when the invitation was last updated
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * @return unique hash to identify the invitation
   */
  public String getInvitationHash() {
    return invitationHash;
  }

  /**
   * sets an md5 hash created from a UUID generated from the email address
   */
  void setInvitationHash() {
    this.invitationHash = InvitationGenerator.generateHash();
  }

  /**
   * @return {@literal true} if the invitee has declined the invitation
   */
  public boolean isDeclined() {
    return declined;
  }

  /**
   * @param declined indicator if the invitation is denied
   */
  public void setDeclined(boolean declined) {
    this.declined = declined;
  }

  /**
   * @return {@literal true} if the invitee has accepted the invitation
   */
  public boolean isAccepted() {
    return accepted;
  }

  /**
   * @param accepted indicator if the invitation is denied
   */
  public void setAccepted(boolean accepted) {
    this.accepted = accepted;
  }

  /**
   * @return List of {@link InvitationMessage}'s
   */
  public List<InvitationMessage> getInvitationMessages() {
    return invitationMessages;
  }

  private void setInvitationMessages(List<InvitationMessage> invitationMessages) {
    this.invitationMessages = invitationMessages;
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  /**
   * Adds one {@link InvitationMessage} to this Invitation
   *
   * @param invitationMessage {@link InvitationMessage} to add
   */
  public void addInvitationMessage(InvitationMessage invitationMessage) {
    invitationMessage.setInvitation(this);
    this.invitationMessages.add(invitationMessage);
  }

  public Optional<InvitationMessage> getLatestInvitationMessage() {
    if (CollectionUtils.isEmpty(invitationMessages)) {
      return Optional.empty();
    }
    return Optional.of(invitationMessages.get(invitationMessages.size() - 1));
  }

  public List<InvitationMessage> getInvitationMessagesReversed() {
    List<InvitationMessage> copy = new ArrayList<>(invitationMessages.size());
    copy.addAll(invitationMessages);
    Collections.reverse(copy);
    return copy;
  }

  public long getExpireTime() {
    return timestamp + TWO_WEEKS;
  }

  /**
   * @return the {@link Role} the invitee should get within the team. Defaults to {@link Role#Member}
   */
  public Role getIntendedRole() {
    if (intendedRole == null) {
      intendedRole = Role.Member;
    }
    return intendedRole;
  }

  /**
   * Setting a role other than {@link Role#Member} can give the invitee more privileges automatically
   *
   * @param intendedRole the {@link Role} the invitee should get within the team. Can be {@literal null}
   */
  public void setIntendedRole(Role intendedRole) {
    this.intendedRole = intendedRole;
  }

  public void accept() {
    setAccepted(true);
  }

  public void decline() {
    setDeclined(true);
  }
}
