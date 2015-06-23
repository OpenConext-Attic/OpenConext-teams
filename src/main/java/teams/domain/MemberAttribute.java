/*
 * Copyright 2011 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teams.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Proxy;


/**
 * Custom attributes for a {@link Member} of a {@link Team}
 * <p/>
 * This saves extra OpenSocial calls when querying lists in Grouper
 */
@SuppressWarnings({"serial", "UnusedDeclaration"})
@Entity
@Table(name = "member_attributes",
  uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "attribute_name"}))
@Proxy(lazy = false)
public class MemberAttribute extends DomainObject {

  @Index(name = "member_attribute", columnNames = {"member_id", "attribute_name"})
  @Column(name = "member_id", nullable = false)
  private String memberId;

  @Column(name = "attribute_name", nullable = false)
  private String attributeName;

  @Column(name = "attribute_value")
  private String attributeValue;

  /**
   * Attribute to indicate the current member is a guest user
   * if corresponding value is {@literal true}
   */
  public static final String ATTRIBUTE_GUEST = "guest";

  /**
   * Constructor necessary for Hibernate. Avoid using it.
   *
   * @deprecated use {@link MemberAttribute(String, String, String)}
   */
  public MemberAttribute() {
    this(null, null, null);
  }

  /**
   * Constructor
   *
   * @param memberId       unique identifier of the {@link Member} (required)
   * @param attributeName  name of the custom attribute (required)
   * @param attributeValue value of the custom attribute
   */
  public MemberAttribute(String memberId, String attributeName, String attributeValue) {
    super();
    this.memberId = memberId;
    this.attributeName = attributeName;
    this.attributeValue = attributeValue;
  }

  public String getMemberId() {
    return memberId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getAttributeValue() {
    return attributeValue;
  }

  public void setAttributeValue(String attributeValue) {
    this.attributeValue = attributeValue;
  }
}
