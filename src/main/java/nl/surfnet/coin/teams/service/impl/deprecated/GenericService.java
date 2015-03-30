package nl.surfnet.coin.teams.service.impl.deprecated;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;

import nl.surfnet.coin.teams.domain.DomainObject;

@Deprecated // Copied this in from the infamous coin-shared only to be removed ASAP.
public interface GenericService<T extends DomainObject> {

  Long saveOrUpdate(T t);

  /**
   * Find by primary key
   *
   * @param id
   *          the entity primary key
   * @return the domainObject
   */
  T findById(Long id);

  void delete(T o);

  List<T> findAll();

  List<T> findByExample(T exampleInstance);

  Class<T> getPersistentClass();

  void detachFromSession(T o);

  int getCount();

  void saveOrUpdate(Collection<T> coll);

  Criteria createCriteria();

  List<T> findByExample(T exampleInstance, String[] excludes);
}
