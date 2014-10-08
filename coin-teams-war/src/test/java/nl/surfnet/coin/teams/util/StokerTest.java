package nl.surfnet.coin.teams.util;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class StokerTest {

  private Stoker stoker;

  @Test(expected = RuntimeException.class)
  public void testThrowsExceptionWhenFileDoesNotExist() throws Exception {
    stoker = new Stoker(new FileSystemResource("not exists"));
    Collection<StokerEntry> serviceProviders = stoker.getEduGainServiceProviders();
    assertEquals(0, serviceProviders.size());
  }

  @Test
  public void testReturnServices() throws Exception {
    stoker = new Stoker(new ClassPathResource("/metadata.index.formatted.json"));
    Collection<StokerEntry> serviceProviders = stoker.getEduGainServiceProviders();
    assertEquals(2, serviceProviders.size());
  }
}
