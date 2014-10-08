package nl.surfnet.coin.teams.util;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StokerEntry {
  public static final String TYPE_SERVICE_PROVIDER = "sp";
  private String displayNameEn;
  private String displayNameNl;
  private String entityId;
  private Collection<String> types;

  public String getDisplayNameEn() {
    return displayNameEn;
  }

  public void setDisplayNameEn(String displayNameEn) {
    this.displayNameEn = displayNameEn;
  }

  public String getDisplayNameNl() {
    return displayNameNl;
  }

  public void setDisplayNameNl(String displayNameNl) {
    this.displayNameNl = displayNameNl;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public Collection<String> getTypes() {
    return types;
  }

  public void setTypes(Collection<String> types) {
    this.types = types;
  }

  public boolean isServiceProvider() {
    return types != null && types.contains(TYPE_SERVICE_PROVIDER);
  }
}
