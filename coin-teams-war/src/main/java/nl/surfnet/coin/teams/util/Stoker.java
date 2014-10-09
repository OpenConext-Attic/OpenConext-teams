package nl.surfnet.coin.teams.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collection;

public class Stoker {
  private final ObjectMapper objectMapper;
  private final Resource resource;
  private final StokerData stokerData;
  private final static Predicate<StokerEntry> onlyServiceProviders = new Predicate<StokerEntry>() {
    @Override
    public boolean apply(StokerEntry input) {
      return input.isServiceProvider();
    }
  };

  public Stoker(Resource resource) throws IOException {
    this.resource = resource;
    this.objectMapper = new ObjectMapper();
    this.stokerData = objectMapper.readValue(IOUtils.toString(resource.getInputStream()), StokerData.class);
  }

  public Collection<StokerEntry> getEduGainServiceProviders() {
    return Collections2.filter(stokerData.getEntities(), onlyServiceProviders);
  }

  public Collection<StokerEntry> getEduGainServiceProviders(final Collection<String> spEntityIds) {
    return Collections2.filter(getEduGainServiceProviders(), new Predicate<StokerEntry>() {
      @Override
      public boolean apply(StokerEntry input) {
        return spEntityIds.contains(input.getEntityId());
      }
    });
  }
}
