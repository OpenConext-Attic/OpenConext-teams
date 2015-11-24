package teams.service.impl;

import teams.domain.ExternalGroup;
import teams.domain.ExternalGroupProvider;
import teams.service.VootClient;

import java.util.ArrayList;
import java.util.List;

public class VootClientMock implements VootClient {

  @Override
  public List<ExternalGroup> groups(String userId) {
    List<ExternalGroup> groups = new ArrayList<ExternalGroup>();
    for (int i = 0; i < 100; i++) {
      groups.add(createExternalGroup("name" + i));
    }
    return groups;
  }


  private ExternalGroup createExternalGroup(String name) {
    return new ExternalGroup("urn:collab:group:example.org:" + name, name, "test description", new ExternalGroupProvider("org.example", "org.example"));
  }
}
