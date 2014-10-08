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

  public Stoker(Resource resource) {
    this.resource = resource;
    this.objectMapper = new ObjectMapper();
  }

  public Collection<StokerEntry> getEduGainServiceProviders() {
    try {

      StokerData stokerData = objectMapper.readValue(IOUtils.toString(resource.getInputStream()), StokerData.class);
      return Collections2.filter(stokerData.getEntities(), new Predicate<StokerEntry>() {
        @Override
        public boolean apply(StokerEntry input) {
          return input.isServiceProvider();
        }
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
