package nl.surfnet.coin.teams.service;

import org.opensocial.models.Person;

/**
 * Interface for {@link Person}
 */
public interface TeamPersonService {

  /**
   * Get the OpenSocial Person
   *
   * @param userId the unique identifier
   * @return the {@link org.opensocial.models.Person}
   */
  Person getPerson(String userId);
}
