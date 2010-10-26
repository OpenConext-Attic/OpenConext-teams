package nl.surfnet.coin.teams.domain;

public class Invitation {
  
  private String teamId;
  private String email;
  
  public Invitation(String teamId, String email) {
    this.setTeamId(teamId);
    this.setEmail(email);
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

}
