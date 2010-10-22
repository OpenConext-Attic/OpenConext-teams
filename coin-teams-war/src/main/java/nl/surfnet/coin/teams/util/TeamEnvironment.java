/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.util;

/**
 * Encapsulating properties for general use
 * 
 */
public class TeamEnvironment {
  private String version;
  private String mockLogin;
  private String mockName;
  private String defaultStemName;
  private String grouperPowerUser;


  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param version
   *          the version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * @return the mockLogin
   */
  public String getMockLogin() {
    return mockLogin;
  }

  /**
   * @param mockLogin
   *          the mockLogin to set
   */
  public void setMockLogin(String mockLogin) {
    this.mockLogin = mockLogin;
  }

  /**
   * @return the mockName
   */
  public String getMockName() {
    return mockName;
  }

  /**
   * @param mockName
   *          the mockName to set
   */
  public void setMockName(String mockName) {
    this.mockName = mockName;
  }

  /**
   * @return the defaultStemName
   */
  public String getDefaultStemName() {
    return defaultStemName;
  }

  /**
   * @param defaultStemName
   *          the defaultStemName to set
   */
  public void setDefaultStemName(String defaultStemName) {
    this.defaultStemName = defaultStemName;
  }

  /**
   * @return the grouperPowerUser
   */
  public String getGrouperPowerUser() {
    return "GrouperSystem";
  }

  /**
   * @param grouperPowerUser the grouperPowerUser to set
   */
  public void setGrouperPowerUser(String grouperPowerUser) {
    this.grouperPowerUser = grouperPowerUser;
  }

}
