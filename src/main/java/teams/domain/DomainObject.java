package teams.domain;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base domain object
 */

@SuppressWarnings("serial")
@MappedSuperclass
@Deprecated // Copied this in from the infamous coin-shared only to be removed ASAP.
public abstract class DomainObject implements Serializable {

  @Override
  public int hashCode() {
    return (id == null) ? super.hashCode() : id.hashCode();
  }

  /**
   * The id.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || !getClass().equals(other.getClass())
      || !(other instanceof DomainObject)) {
      return false;
    }
    DomainObject domainObject = (DomainObject) other;
    if (id == null && domainObject.id == null) {
      return super.equals(domainObject);
    }
    if ((id != null && domainObject.id == null)
      || (id == null && domainObject.id != null)) {
      return false;
    }
    return id.equals(domainObject.id);
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return getClass() + "(id='" + id + "')";
  }

}
