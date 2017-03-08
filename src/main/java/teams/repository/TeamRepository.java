package teams.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import teams.migration.Team;

import java.util.Optional;
import java.util.stream.Stream;

public interface TeamRepository extends PagingAndSortingRepository<Team, Long> {

    Stream<Team> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    Optional<Team> findByUrn(String urn);

}