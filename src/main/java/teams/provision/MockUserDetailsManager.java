package teams.provision;

import teams.domain.Person;

public class MockUserDetailsManager implements UserDetailsManager {

  @Override
  public boolean existingPerson(String urn) {
    return true;
  }

  @Override
  public void createPerson(Person person) {
    //will never get called as we always return true in #existingPerson
  }
}
