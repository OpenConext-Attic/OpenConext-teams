package nl.surfnet.coin.teams.service.impl;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.opensocial.models.Person;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import nl.surfnet.coin.shared.service.GenericServiceHibernateImpl;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.JoinTeamRequestService;

/**
 * Hibernate implementation for {@link JoinTeamRequestService}
 */
@Component("joinTeamRequestService")
public class JoinTeamRequestServiceHibernateImpl
        extends GenericServiceHibernateImpl<JoinTeamRequest>
        implements JoinTeamRequestService {

  public JoinTeamRequestServiceHibernateImpl() {
    super(JoinTeamRequest.class);
  }

  /**
   * Constructor
   *
   * @param type the clazz
   */
  public JoinTeamRequestServiceHibernateImpl(Class<JoinTeamRequest> type) {
    super(type);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<JoinTeamRequest> findPendingRequests(Team team) {
    return findByCriteria(Restrictions.eq("groupId", team.getId()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JoinTeamRequest findPendingRequest(Person person, Team team) {
    SimpleExpression personId = Restrictions.eq("personId", person.getId());
    SimpleExpression groupId = Restrictions.eq("groupId", team.getId());
    List<JoinTeamRequest> list = findByCriteria(personId, groupId);
    return CollectionUtils.isEmpty(list) ? null : list.get(0);
  }
}
