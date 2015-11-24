package teams.service;

import java.util.List;

import teams.domain.ExternalGroup;

public interface VootClient {

  List<ExternalGroup> groups(String userId);

}
