package teams.provision;

import teams.domain.Person;

public interface UserDetailsManager {

  boolean existingPerson(String urn);

  void createPerson(Person person);

}
