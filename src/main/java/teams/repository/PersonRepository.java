package teams.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import teams.migration.Person;

import java.util.Optional;

public interface PersonRepository extends PagingAndSortingRepository<Person, Long> {

  Optional<Person> findByUrn(String urn);

}
