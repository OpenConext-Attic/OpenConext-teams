package teams.repository;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import teams.AbstractApplicationTest;
import teams.migration.Membership;
import teams.migration.Person;
import teams.migration.Role;
import teams.migration.Team;

import java.rmi.server.UID;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

public class TeamRepositoryTest extends AbstractApplicationTest {

  @Test
  public void findByNameContainingIgnoreCaseOrderByNameAsc() throws Exception {
    List<Team> teams = teamRepository.findByNameContainingIgnoreCaseOrderByNameAsc("ER");
    assertEquals("gliders", teams.get(0).getName());
    assertEquals("riders", teams.get(1).getName());
  }

  @Test
  public void findByUrn() throws Exception {
    Optional<Team> teamOptional = teamRepository.findByUrn("nl:surfnet:diensten:giants");
    assertEquals("giants", teamOptional.get().getName());
  }

  @Test
  public void findByMembershipsPersonUrn() throws Exception {
    List<Team> teams = teamRepository.findByMembershipsUrnPersonOrderByNameAsc(
      "urn:collab:person:surfnet.nl:jdoe", new PageRequest(0, 10)).getContent();
    assertEquals(3, teams.size());
  }

  @Test
  public void findByNameContainingIgnoreCaseAndMembershipsPersonUrnOrderByNameAsc() throws Exception {
    List<Team> teams = teamRepository.findByNameContainingIgnoreCaseAndMembershipsUrnPersonOrderByNameAsc(
      "ERS", "urn:collab:person:surfnet.nl:jdoe", new PageRequest(0, 10)).getContent();
    assertEquals(2, teams.size());
  }

  @Test
  @Ignore
  public void testDummyContent() throws Exception {
    Person person = personRepository.findByUrn("urn:collab:person:surfnet.nl:jdoe").get();
    for (int i = 0; i < 1000;i++) {
      Team saved = teamRepository.save(new Team("nl:surfnet:diensten:"+ UUID.randomUUID().toString(),"Dummy test "+ i,"decsription dummy "+ i));
      membershipRepository.save(new Membership(Role.ADMIN, saved, person));
    }
    System.out.println();
  }

}
