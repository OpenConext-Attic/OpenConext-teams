package teams.service.impl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import teams.AbstractApplicationTest;
import teams.domain.ExternalGroup;
import teams.domain.TeamExternalGroup;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

public class TeamExternalGroupDaoImplTest extends AbstractApplicationTest {

  @Autowired
  private TeamExternalGroupDaoImpl subject;

  @Test
  public void getByTeamIdentifier() throws Exception {
    List<TeamExternalGroup> teamExternalGroups = subject.getByTeamIdentifier("nl:surfnet:diensten:giants", "nl:surfnet:diensten:riders");
    assertEquals(3, teamExternalGroups.size());
    teamExternalGroups.forEach(this::assertTeamExternalGroup);
  }

  @Test
  public void getByTeamIdentifierAndExternalGroupIdentifier() throws Exception {
    TeamExternalGroup teamExternalGroup = subject.getByTeamIdentifierAndExternalGroupIdentifier("nl:surfnet:diensten:riders", "urn:collab:group:example.org:name2");
    assertTeamExternalGroup(teamExternalGroup);
  }

  @Test
  public void saveOrUpdate() throws Exception {
    TeamExternalGroup teamExternalGroup = subject.getByTeamIdentifierAndExternalGroupIdentifier("nl:surfnet:diensten:riders", "urn:collab:group:example.org:name2");
    teamExternalGroup.setGrouperTeamId("bogus");
    subject.saveOrUpdate(teamExternalGroup);
    teamExternalGroup = subject.getByTeamIdentifierAndExternalGroupIdentifier("bogus", "urn:collab:group:example.org:name2");
    assertTeamExternalGroup(teamExternalGroup);
  }

  @Test
  public void delete() throws Exception {
    TeamExternalGroup teamExternalGroup = subject.getByTeamIdentifierAndExternalGroupIdentifier("nl:surfnet:diensten:riders", "urn:collab:group:example.org:name2");
    subject.delete(teamExternalGroup);
    teamExternalGroup = subject.getByTeamIdentifierAndExternalGroupIdentifier("nl:surfnet:diensten:riders", "urn:collab:group:example.org:name2");
    assertNull(teamExternalGroup);
  }

  @Test
  public void getByExternalGroupIdentifiers() throws Exception {
    List<TeamExternalGroup> teamExternalGroups = subject.getByExternalGroupIdentifiers(Arrays.asList("urn:collab:group:example.org:name1", "urn:collab:group:example.org:name2"));
    assertEquals(3, teamExternalGroups.size());
    teamExternalGroups.forEach(this::assertTeamExternalGroup);
  }

  private void assertTeamExternalGroup(TeamExternalGroup teamExternalGroup) {
    String grouperTeamId = teamExternalGroup.getGrouperTeamId();
    assertNotNull(grouperTeamId);
    ExternalGroup externalGroup = teamExternalGroup.getExternalGroup();
    assertNotNull(externalGroup.getGroupProviderIdentifier());
    assertNotNull(externalGroup.getIdentifier());
  }

}
