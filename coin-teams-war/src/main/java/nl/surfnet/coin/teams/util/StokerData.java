package nl.surfnet.coin.teams.util;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.joda.time.DateTime;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StokerData {

  private Collection<StokerEntry> entities;
  private DateTime cacheUntil;
  private DateTime processed;
  private DateTime validUntil;

  public Collection<StokerEntry> getEntities() {
    return entities;
  }

  public void setEntities(Collection<StokerEntry> entities) {
    this.entities = entities;
  }

  public DateTime getCacheUntil() {
    return cacheUntil;
  }

  public void setCacheUntil(DateTime cacheUntil) {
    this.cacheUntil = cacheUntil;
  }

  public DateTime getProcessed() {
    return processed;
  }

  public void setProcessed(DateTime processed) {
    this.processed = processed;
  }

  public DateTime getValidUntil() {
    return validUntil;
  }

  public void setValidUntil(DateTime validUntil) {
    this.validUntil = validUntil;
  }
}
