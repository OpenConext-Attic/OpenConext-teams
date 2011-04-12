package nl.surfnet.coin.teams.service.impl;

import org.springframework.stereotype.Component;

import nl.surfnet.coin.shared.service.GenericServiceHibernateImpl;
import nl.surfnet.coin.teams.domain.JoinTeamRequest;
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
}
