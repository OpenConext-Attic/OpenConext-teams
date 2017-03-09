package teams.repository;

import org.junit.Test;
import teams.AbstractApplicationTest;
import teams.migration.Person;

import java.util.Optional;

import static org.junit.Assert.*;

public class PersonRepositoryTest extends AbstractApplicationTest{

  @Test
  public void findByUrn() throws Exception {
    Optional<Person> personOptional = personRepository.findByUrn("urn:collab:person:surfnet.nl:jdoe");
    assertEquals("John Doe", personOptional.get().getName());
  }

}
