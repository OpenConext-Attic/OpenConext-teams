package teams.provision;

import teams.domain.Member;
import teams.domain.Person;

import java.util.Optional;

public interface UserDetailsManager {

  boolean existingPerson(String urn);

  void createPerson(Person person);

  Optional<teams.migration.Person> findPersonById(String urn);

}
