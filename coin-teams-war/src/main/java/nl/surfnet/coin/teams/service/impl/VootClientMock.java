package nl.surfnet.coin.teams.service.impl;

import nl.surfnet.coin.teams.domain.ExternalGroup;
import nl.surfnet.coin.teams.domain.ExternalGroupProvider;
import nl.surfnet.coin.teams.service.VootClient;

import java.util.Arrays;
import java.util.List;

public class VootClientMock implements VootClient {
  @Override
  public List<ExternalGroup> groups(String userId) {
    return Arrays.asList(createExternalGroup());
  }


  private ExternalGroup createExternalGroup() {
    return new ExternalGroup("urn:collab:group:example.org:test", "test", "test description", new ExternalGroupProvider("org.example", "org.example"));
  }
}
