package teams.control;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import teams.domain.Invitation;
import teams.domain.Language;
import teams.domain.Role;

public class ResendInvitationCommand {

  private String invitationId;
  private String teamId;
  @NotEmpty @Email
  private String email;
  private String messageText;
  private Role intendedRole;
  private Language language;

  public ResendInvitationCommand(Invitation invitation) {
    this.invitationId = invitation.getInvitationHash();
    this.email = invitation.getEmail();
    this.teamId = invitation.getTeamId();
    this.intendedRole = invitation.getIntendedRole();
    this.language = invitation.getLanguage();
  }

  public ResendInvitationCommand() {
  }

  public Role getIntendedRole() {
    return intendedRole;
  }

  public void setIntendedRole(Role intendedRole) {
    this.intendedRole = intendedRole;
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  public String getTeamId() {
    return teamId;
  }

  public void setTeamId(String teamId) {
    this.teamId = teamId;
  }

  public String getMessageText() {
    return messageText;
  }

  public void setMessageText(String messageText) {
    this.messageText = messageText;
  }

  public String getInvitationId() {
    return invitationId;
  }

  public void setInvitationId(String invitationId) {
    this.invitationId = invitationId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void apply(Invitation invitation) {
    invitation.setLanguage(language);
    invitation.setIntendedRole(intendedRole);
    invitation.setEmail(email);
  }

}
