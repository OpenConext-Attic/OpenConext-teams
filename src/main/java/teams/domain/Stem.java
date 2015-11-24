package teams.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

public class Stem implements Serializable {

  private static final long serialVersionUID = 0L;

  private String id;
  private String name;
  private String description;

  /**
   * Constructor for a stem
   *
   * @param id {@link String} the stem identifier
   * @param name {@link String} the stem name
   * @param description {@link String} the stem description
   */
  public Stem(String id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  /**
   * Get the Stem identifier
   *
   * @return String the stem Identifier
   */
  public String getId() {
    return id;
  }

  /**
   * Set the stem identifier
   *
   * @param id {@link String} the stem identifier
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the stem name
   *
   * @return {@link String} the stem name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the stem name
   *
   * @param name {@link String} the stem name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the stem description
   *
   * @return {@link String} the stem description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the stem description
   *
   * @param description {@link String} the stem description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("id", id)
      .append("name", name)
      .toString();
  }

}
