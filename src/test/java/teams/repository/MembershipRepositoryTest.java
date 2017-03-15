package teams.repository;

import org.junit.Test;
import teams.AbstractApplicationTest;
import teams.migration.Membership;
import teams.migration.Role;

import java.util.Optional;

import static org.junit.Assert.*;

public class MembershipRepositoryTest extends AbstractApplicationTest {

  @Test
  public void findByTeamUrnAndPersonUrn() throws Exception {
    Optional<Membership> membershipOptional = membershipRepository.findByTeamUrnAndPersonUrn("nl:surfnet:diensten:riders", "urn:collab:person:surfnet.nl:jdoe");
    assertEquals(Role.MEMBER, membershipOptional.get().getRole());
  }

}
