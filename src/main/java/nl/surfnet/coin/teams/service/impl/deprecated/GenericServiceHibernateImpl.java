package nl.surfnet.coin.teams.service.impl.deprecated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.transaction.annotation.Transactional;

import nl.surfnet.coin.teams.domain.DomainObject;


@Transactional
@Deprecated // Copied this in from the infamous coin-shared only to be removed ASAP.
public class GenericServiceHibernateImpl<T extends DomainObject> implements GenericService<T> {

  @Autowired
  private SessionFactory portalSessionFactory;

  /*
   * The domainObject class
   */
  private Class<T> persistentClass;

  /**
   * Constructor
   *
   * @param type the clazz
   */
  public GenericServiceHibernateImpl(Class<T> type) {
    this.persistentClass = type;
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.coin.service.GenericService#delete(nl.surfnet.coin.domain.DomainObject)
   */
  @Override
  public void delete(T o) {
    portalSessionFactory.getCurrentSession().delete(o);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * nl.surfnet.coin.service.GenericService#detachFromSession(nl.surfnet.coin.domain.DomainObject)
   */
  @Override
  public void detachFromSession(T o) {
    portalSessionFactory.getCurrentSession().evict(o);
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.coin.service.GenericService#findAll()
   */
  @Override
  public List<T> findAll() {
    return findByCriteria();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * nl.surfnet.coin.service.GenericService#findByExample(nl.surfnet.coin.domain.DomainObject)
   */
  @Override
  public List<T> findByExample(T exampleInstance) {
    return findByExample(exampleInstance, new String[]{});
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * nl.surfnet.coin.service.GenericService#findByExample(nl.surfnet.coin.domain.DomainObject)
   */
  @Override
  public List<T> findByExample(T exampleInstance, String[] excludes) {
    Example create = Example.create(exampleInstance);
    for (String name : excludes) {
      create.excludeProperty(name);
    }
    return findByCriteria(create);
  }

  /**
   * Convenience method for subclasses to find domain objects that match the Criterion's
   *
   * @param criterion array of {@link Criterion}'s
   * @return List of domain objects
   */
  @SuppressWarnings("unchecked")
  protected List<T> findByCriteria(Criterion... criterion) {
    List<Criterion> criterionList = new ArrayList<Criterion>(criterion.length);
    Collections.addAll(criterionList, criterion);
    return findByCriteriaOrdered(criterionList, Collections.<Order>emptyList());
  }


  /**
   * Convenicence method for subclasses to find domain objects that match the list of Criterion's in the given order
   *
   * @param criterionList List of {@link Criterion}'s
   * @param orderList List of {@link Order}'s
   * @return Sorted list of domain objects
   */
  @SuppressWarnings("unchecked")
  protected List<T> findByCriteriaOrdered(List<Criterion> criterionList, List<Order> orderList) {
    Criteria crit = portalSessionFactory.getCurrentSession().createCriteria(getPersistentClass());
    for (Criterion c : criterionList) {
      crit.add(c);
    }
    for (Order order : orderList) {
      crit.addOrder(order);
    }
    crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    return crit.list();
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.coin.service.GenericService#findById(java.lang.Long)
   */
  @SuppressWarnings("unchecked")
  @Override
  public T findById(Long id) {
    return (T) portalSessionFactory.getCurrentSession().load(getPersistentClass(), id);
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.coin.service.GenericService#getCount()
   */
  @Override
  public int getCount() {
    return DataAccessUtils.intResult(portalSessionFactory.getCurrentSession().createQuery("select count(*) from " + getPersistentClass().getName())
      .list());
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.coin.service.GenericService#getPersistentClass()
   */
  @Override
  public Class<T> getPersistentClass() {
    return persistentClass;
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.coin.service.GenericService#saveOrUpdate(nl.surfnet.coin.domain.DomainObject)
   */
  @Override
  public Long saveOrUpdate(T t) {
    portalSessionFactory.getCurrentSession().saveOrUpdate(t);
    return t.getId();
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.coin.service.GenericService#saveOrUpdate(nl.surfnet.coin.domain.DomainObject)
   */
  public void saveOrUpdate(Collection<T> coll) {
    for (T t : coll) {
      saveOrUpdate(t);
    }
  }

  /**
   * @return the portalSessionFactory
   */
  protected Session getSession() {
    return portalSessionFactory.getCurrentSession();
  }

  /* (non-Javadoc)
   * @see nl.surfnet.coin.portal.service.GenericService#createCriteria()
   */
  @Override
  public Criteria createCriteria() {
    return portalSessionFactory.getCurrentSession().createCriteria(getPersistentClass());

  }
}
