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
  private String teamsAPIUrl;
  private String oauthKey;
  private String oauthSecret;
  private String restEndpoint;
  private String rpcEndpoint;
  private String appId;


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
    return grouperPowerUser;
  }

  /**
   * @param grouperPowerUser the grouperPowerUser to set
   */
  public void setGrouperPowerUser(String grouperPowerUser) {
    this.grouperPowerUser = grouperPowerUser;
  }

  /**
   * @param teamsAPIUrl the teamsAPIUrl to set
   */
  public void setTeamsAPIUrl(String teamsAPIUrl) {
    this.teamsAPIUrl = teamsAPIUrl;
  }

  /**
   * @return the teamsAPIUrl
   */
  public String getTeamsAPIUrl() {
    return teamsAPIUrl;
  }

  /**
   * @param restEndpoint the restEndpoint to set
   */
  public void setRestEndpoint(String restEndpoint) {
    this.restEndpoint = restEndpoint;
  }

  /**
   * @return the restEndpoint
   */
  public String getRestEndpoint() {
    return restEndpoint;
  }

  /**
   * @param rpcEndpoint the rpcEndpoint to set
   */
  public void setRpcEndpoint(String rpcEndpoint) {
    this.rpcEndpoint = rpcEndpoint;
  }

  /**
   * @return the rpcEndpoint
   */
  public String getRpcEndpoint() {
    return rpcEndpoint;
  }

  /**
   * @param appId the appId to set
   */
  public void setAppId(String appId) {
    this.appId = appId;
  }

  /**
   * @return the appId
   */
  public String getAppId() {
    return appId;
  }

  /**
   * @param oauthKey the oauthKey to set
   */
  public void setOauthKey(String oauthKey) {
    this.oauthKey = oauthKey;
  }

  /**
   * @return the oauthKey
   */
  public String getOauthKey() {
    return oauthKey;
  }

  /**
   * @param oauthSecret the oauthSecret to set
   */
  public void setOauthSecret(String oauthSecret) {
    this.oauthSecret = oauthSecret;
  }

  /**
   * @return the oauthSecret
   */
  public String getOauthSecret() {
    return oauthSecret;
  }

}
