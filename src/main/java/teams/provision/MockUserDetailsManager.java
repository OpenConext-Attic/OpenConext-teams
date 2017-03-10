package teams.provision;

import teams.domain.Member;
import teams.domain.Person;

import java.util.Optional;

public class MockUserDetailsManager implements UserDetailsManager {

  @Override
  public Optional<teams.migration.Person> findPersonById(String urn) {
    return Optional.empty();
  }
}
