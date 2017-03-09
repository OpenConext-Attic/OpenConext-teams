package teams.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import teams.migration.Membership;
import teams.migration.Person;

import java.util.Optional;

public interface MembershipRepository extends PagingAndSortingRepository<Membership, Long> {

  Optional<Membership> findByTeamUrnAndPersonUrn(String teamUrn, String personUrn);
}
