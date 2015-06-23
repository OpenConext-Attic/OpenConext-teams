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

package teams.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;


/**
 * Represents the request to join a team
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "requests")
@Proxy(lazy = false)
public class JoinTeamRequest extends DomainObject {

  @Column(name = "group_id", nullable = false)
  private String groupId;

  @Column(nullable = false)
  private long timestamp;

  @Column(name = "uuid", nullable = false)
  private String personId;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Lob
  private String message;

  /**
   * Necessary constructor for Hibernate.
   * Avoid to call this in the code.
   */
  public JoinTeamRequest() {
    this(null, null, null, null);
  }

  /**
   * Constructor with required fields
   *
   * @param personId id of the {@link Person}
   * @param groupId  id of the {@link Team}
   */
  public JoinTeamRequest(String personId, String groupId, String email, String displayName) {
    super();
    this.setPersonId(personId);
    this.setGroupId(groupId);
    this.email = email;
    this.displayName = displayName;
    this.setTimestamp(new Date().getTime());
  }

  /**
   * @return id of the group
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @param groupId to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * @return timestamp of the request
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * @param timestamp of the request
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * @return personId that wants to join the team
   */
  public String getPersonId() {
    return personId;
  }

  /**
   * @param personId to set
   */
  public void setPersonId(String personId) {
    this.personId = personId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @return message that was sent during the request
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message to send to the admin/manager of the group
   */
  public void setMessage(String message) {
    this.message = message;
  }
}
