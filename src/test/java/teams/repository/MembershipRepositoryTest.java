package teams.repository;

import org.junit.Test;
import teams.AbstractApplicationTest;
import teams.migration.Membership;
import teams.migration.Role;
import teams.migration.Team;

import java.util.Optional;

import static org.junit.Assert.*;

public class MembershipRepositoryTest extends AbstractApplicationTest {

  @Test
  public void findByTeamUrnAndPersonUrn() throws Exception {
    Optional<Membership> membershipOptional = membershipRepository.findByUrnTeamAndUrnPerson("nl:surfnet:diensten:riders", "urn:collab:person:surfnet.nl:jdoe");
    Membership membership = membershipOptional.get();
    assertEquals(Role.ADMIN, membership.getRole());

    Team team = teamRepository.findByUrn("nl:surfnet:diensten:riders").get();
    team.getMemberships().remove(membership);
    membershipRepository.delete(membership);
    membershipOptional = membershipRepository.findByUrnTeamAndUrnPerson("nl:surfnet:diensten:riders", "urn:collab:person:surfnet.nl:jdoe");
    assertFalse(membershipOptional.isPresent());
  }

}
