package nl.surfnet.coin.teams.service;

import nl.surfnet.coin.api.client.domain.Person;

public interface ApiService {
    Person getPerson(String personId);
}
