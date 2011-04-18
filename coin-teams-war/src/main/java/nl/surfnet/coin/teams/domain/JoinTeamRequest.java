package nl.surfnet.coin.teams.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

import nl.surfnet.coin.shared.domain.DomainObject;

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

  @Column(columnDefinition = "TEXT")
  @Lob
  private String message;

  /**
   * For each timestamp manipulation,
   * devide by this value for backwards compatibility.
   */
  public static final long DATE_PRECISION_DIVIDER = 1000L;

  /**
   * Necessary constructor for Hibernate.
   * Avoid to call this in the code.
   */
  public JoinTeamRequest() {
    this(null, null);
  }

  /**
   * Constructor with required fields
   *
   * @param personId id of the {@link org.opensocial.models.Person}
   * @param groupId  id of the {@link Team}
   */
  public JoinTeamRequest(String personId, String groupId) {
    super();
    this.setPersonId(personId);
    this.setGroupId(groupId);
    this.setTimestamp(new Date().getTime() / DATE_PRECISION_DIVIDER);
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
