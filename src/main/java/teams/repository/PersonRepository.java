package teams.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import teams.migration.Person;

public interface PersonRepository extends PagingAndSortingRepository<Person, Long> {

}
