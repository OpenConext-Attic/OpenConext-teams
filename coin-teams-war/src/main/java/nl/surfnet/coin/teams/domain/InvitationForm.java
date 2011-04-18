package nl.surfnet.coin.teams.domain;

import org.opensocial.models.Person;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
  Bean for the invitation form
 */
public class InvitationForm {

  private String emails;
  private String message;
  private String teamId;
  private MultipartFile csvFile;
  private Person inviter;

  public String getEmails() {
    return emails;
  }

  public void setEmails(String emails) {
    this.emails = emails;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getTeamId() {
    return teamId;
  }

  public void setTeamId(String teamId) {
    this.teamId = teamId;
  }

  public MultipartFile getCsvFile() {
    return csvFile;
  }

  public void setCsvFile(MultipartFile csvFile) {
    this.csvFile = csvFile;
  }

  public Person getInviter() {
    return inviter;
  }

  public void setInviter(Person inviter) {
    this.inviter = inviter;
  }

  public boolean hasCsvFile() {
    return csvFile!=null && StringUtils.hasText(csvFile.getName());
  }
}
