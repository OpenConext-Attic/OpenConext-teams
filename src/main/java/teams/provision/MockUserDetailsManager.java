package teams.provision;

import teams.domain.Member;
import teams.domain.Person;

import java.util.Optional;

public class MockUserDetailsManager implements UserDetailsManager {

  @Override
  public boolean existingPerson(String urn) {
    return true;
  }

  @Override
  public void createPerson(Person person) {
    //will never get called as we always return true in #existingPerson
  }

  @Override
  public Optional<teams.migration.Person> findPersonById(String urn) {
    return Optional.empty();
  }
}
