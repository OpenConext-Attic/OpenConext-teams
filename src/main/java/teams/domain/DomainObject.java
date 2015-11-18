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
public abstract class DomainObject implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    return (id == null) ? super.hashCode() : id.hashCode();
  }

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

  @Override
  public String toString() {
    return getClass() + "(id='" + id + "')";
  }

}
