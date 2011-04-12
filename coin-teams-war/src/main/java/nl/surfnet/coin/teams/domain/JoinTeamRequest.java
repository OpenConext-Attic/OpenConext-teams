package nl.surfnet.coin.teams.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
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

  @Deprecated
  @Column(nullable = false)
  private String uuid;

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
   * @return old uuid
   * @deprecated use {@link #getId()} instead
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * @param uuid to set
   * @deprecated use {@link #setId(Long)} instead
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

}
