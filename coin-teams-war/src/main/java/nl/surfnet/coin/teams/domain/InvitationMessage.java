package nl.surfnet.coin.teams.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Proxy;

import nl.surfnet.coin.shared.domain.DomainObject;

/**
 * Message content of the sent invitation
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "invitation_message")
@Proxy(lazy = false)
public class InvitationMessage extends DomainObject implements Comparable<InvitationMessage>{

  @ManyToOne
  @JoinColumn(name = "invitation_id", nullable = false)
  private Invitation invitation;

  @Lob
  private String message;

  @Column(nullable = false)
  @Index(name = "messagetimestamp")
  private long timestamp;

  private String inviter;

  /**
   * Constructor necessary for Hibernate
   */
  public InvitationMessage() {
    this(null, null);
  }

  public InvitationMessage(final String message, final String inviter) {
    super();
    this.message = message;
    this.inviter = inviter;
    this.setTimestamp(new Date().getTime());
  }

  /**
   * @return {@link Invitation} basic information
   */
  public Invitation getInvitation() {
    return invitation;
  }

  /**
   * @param invitation {@link Invitation} to set
   */
  public void setInvitation(Invitation invitation) {
    this.invitation = invitation;
  }

  /**
   * @return message body that was sent
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message body to sent
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * @return id of the person that has sent the invitation
   */
  public String getInviter() {
    return inviter;
  }

  /**
   * @param inviter person the sends the invitation
   */
  public void setInviter(String inviter) {
    this.inviter = inviter;
  }

  /**
   * @return timestamp of the invitation
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * @param timestamp of the invitation
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public int compareTo(InvitationMessage o) {
    if (o == null || this.timestamp > o.getTimestamp()) {
      return 1;
    } else if (this.timestamp < o.getTimestamp()) {
      return -1;
    }
    return 0;
  }
}
