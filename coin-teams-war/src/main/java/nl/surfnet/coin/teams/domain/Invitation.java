package nl.surfnet.coin.teams.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

import nl.surfnet.coin.shared.domain.DomainObject;
import nl.surfnet.coin.teams.util.InvitationHashGenerator;

@SuppressWarnings("serial")
@Entity
@Table(name = "invitations")
@Proxy(lazy = false)
public class Invitation extends DomainObject {

  /**
   * For each timestamp manipulation,
   * devide by this value for backwards compatibility.
   */
  public static final long DATE_PRECISION_DIVIDER = 1000L;

  /**
   * Constructor Hibernate needs when fetching results from the db.
   * Do not use to create new Invitations.
   */
  public Invitation() {
  }

  /**
   * Constructor with the most common fields
   * 
   * @param email address of the person to invite
   * @param teamId id of the team the person will join
   * @param inviter identifier of the inviter
   */
  public Invitation(String email, String teamId, String inviter) {
    super();
    this.setEmail(email);
    this.setTeamId(teamId);
    this.setInviter(inviter);
    this.setTimestamp(new Date().getTime() / DATE_PRECISION_DIVIDER);
    this.setInvitationHash();

  }

  @Column(name = "group_id", nullable = false)
  private String teamId;

  @Column(name = "mailaddress", nullable = false)
  private String email;

  @Column(nullable = false)
  private long timestamp;

  @Column(name = "invitation_uiid", nullable = false)
  private String invitationHash;

  // 0 or 1
  @Column(name = "denied")
  private boolean declined;

  // value: Person#getId
  // TODO: find out where this was used in the PHP code
  private String inviter;

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
   * @return timestamp when the invitation was created
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * @param timestamp to indicate when the invitation was created
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
    this.invitationHash = InvitationHashGenerator.generateHash(email+teamId);
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
   * @return identifier of the inviter, similar to {@link org.opensocial.models.Person#getId()}
   */
  public String getInviter() {
    return inviter;
  }

  /**
   * @param inviter similar to {@link org.opensocial.models.Person#getId()}
   */
  public void setInviter(String inviter) {
    this.inviter = inviter;
  }
}
