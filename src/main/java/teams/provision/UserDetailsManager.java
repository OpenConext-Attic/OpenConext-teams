package teams.provision;

import teams.domain.Member;
import teams.domain.Person;

import java.util.Optional;

public interface UserDetailsManager {

  Optional<teams.migration.Person> findPersonById(String urn);

}
