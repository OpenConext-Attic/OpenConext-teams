package teams.repository;

import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import teams.AbstractApplicationTest;
import teams.migration.Team;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

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

  @Test(expected = DataIntegrityViolationException.class)
  public void addTeam() throws Exception {
    teamRepository.save(new Team("nl:surfnet:diensten:riders","riders", "we are riders"));
  }

}
