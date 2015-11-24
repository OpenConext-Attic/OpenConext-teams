package teams.control;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import teams.domain.Language;
import teams.domain.Stem;

public class AddTeamCommand {
  private Stem stem;
  private boolean viewable;
  @NotEmpty
  private String teamName;
  private String teamDescription;

  @Email
  private String admin2Email;
  private Language admin2Language;
  private String admin2Message;

  public boolean isViewable() {
    return viewable;
  }
  public void setViewable(boolean viewable) {
    this.viewable = viewable;
  }
  public Stem getStem() {
    return stem;
  }
  public void setStem(Stem stem) {
    this.stem = stem;
  }
  public Language getAdmin2Language() {
    return admin2Language;
  }
  public void setAdmin2Language(Language admin2Langague) {
    this.admin2Language = admin2Langague;
  }
  public String getTeamName() {
    return teamName;
  }
  public void setTeamName(String teamName) {
    this.teamName = teamName;
  }
  public String getTeamDescription() {
    return teamDescription;
  }
  public String getAdmin2Message() {
    return admin2Message;
  }
  public void setAdmin2Message(String admin2Message) {
    this.admin2Message = admin2Message;
  }
  public void setTeamDescription(String teamDescription) {
    this.teamDescription = teamDescription;
  }
  public String getAdmin2Email() {
    return admin2Email;
  }
  public void setAdmin2Email(String admin2Email) {
    this.admin2Email = admin2Email;
  }
}
