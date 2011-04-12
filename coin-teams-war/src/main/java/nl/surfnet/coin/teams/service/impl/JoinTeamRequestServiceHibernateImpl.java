package nl.surfnet.coin.teams.service.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.opensocial.models.Person;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import nl.surfnet.coin.shared.service.GenericServiceHibernateImpl;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.JoinTeamRequestService;

/**
 * Created by IntelliJ IDEA.
 * User: jashaj
 * Date: 11-04-11
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
@Component("joinTeamRequestService")
public class JoinTeamRequestServiceHibernateImpl
  extends GenericServiceHibernateImpl<JoinTeamRequest>
  implements JoinTeamRequestService {

  private static final int ONE_RESULT = 1;

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
  public boolean isNewRequestForTeam(Person person, Team team) {
    Criteria criteria = createCriteria()
      .add(Restrictions.eq("personId", person.getId()))
      .add(Restrictions.eq("groupId", team.getId()));
    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    criteria.setMaxResults(ONE_RESULT);
    List list = criteria.list();
    return CollectionUtils.isEmpty(list);
  }
}
