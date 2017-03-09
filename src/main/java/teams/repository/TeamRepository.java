package teams.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import teams.migration.Team;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface TeamRepository extends PagingAndSortingRepository<Team, Long> {

    List<Team> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    Optional<Team> findByUrn(String urn);

    Page<Team> findByMembershipsUrnPersonOrderByNameAsc(String personUrn, Pageable pageable);

    Page<Team> findByNameContainingIgnoreCaseAndMembershipsUrnPersonOrderByNameAsc(String name, String personUrn, Pageable pageable);
}
