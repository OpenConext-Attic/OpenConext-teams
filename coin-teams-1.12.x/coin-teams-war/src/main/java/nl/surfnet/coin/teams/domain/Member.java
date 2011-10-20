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

package nl.surfnet.coin.teams.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opensocial.models.Person;

/**
 * A member of a {@link Team}
 */
public class Member implements Serializable {

  private Set<Role> roles;
  private String name;
  private String id;
  private String email;
  private List<MemberAttribute> memberAttributes;

  public static final Set<Role> member = new HashSet<Role>();//Collections.singleton(Role.Member);

  /**
   * @param roles Set of {@link Role}'s for this member
   * @param name  full name
   * @param id    uuid of the member
   * @param email address of the member
   */
  public Member(Set<Role> roles, String name, String id, String email) {
    super();
    this.roles = roles;
    this.name = name;
    this.id = id;
    this.email = email;
  }

  /**
   * @param roles  Set of {@link Role}'s for this member
   * @param person {@link Person} that represents this member
   */
  public Member(Set<Role> roles, Person person) {
    this(roles, person.getDisplayName(), person.getId(), person.getEmail());
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @return the roles
   */
  public Set<Role> getRoles() {
    if (roles == null) {
      roles = new HashSet<Role>();
    }
    return roles;
  }

  /**
   * @param role the role to be added
   * @return boolean is successful
   */

  public boolean addRole(Role role) {
    if (this.roles == null) {
      this.roles = new HashSet<Role>();
    }
    return roles.add(role);
  }

  /**
   * @param role the role to be removed
   * @return boolean is successful
   */

  public boolean removeRole(Role role) {
    if (this.roles == null) {
      return false;
    }
    return roles.remove(role);
  }

  /**
   * @return List of (custom) {@link MemberAttribute}'s,
   *         can be empty but not {@literal null}
   */
  public List<MemberAttribute> getMemberAttributes() {
    if (memberAttributes == null) {
      memberAttributes = new ArrayList<MemberAttribute>();
    }
    return memberAttributes;
  }

  /**
   * @param memberAttributes List of (custom) {@link MemberAttribute}'s
   */
  public void setMemberAttributes(List<MemberAttribute> memberAttributes) {
    this.memberAttributes = memberAttributes;
  }

  /**
   * Adds one {@link MemberAttribute} to the list
   *
   * @param memberAttribute MemberAttribute to add
   */
  public void addMemberAttribute(MemberAttribute memberAttribute) {
    getMemberAttributes().add(memberAttribute);
  }

  /**
   * Copy the {@link Member}
   *
   * @return copy of this instance
   */
  public Member copy() {
    Member copy = new Member(new HashSet<Role>(this.getRoles()), this.getName(), this.getId(), this.getEmail());
    copy.setMemberAttributes(this.getMemberAttributes());
    return copy;
  }

  /**
   * Defines if the Member has a guest status
   *
   * @return {@literal true} if the Member has a guest status
   */
  public boolean isGuest() {
    for (MemberAttribute memberAttribute : getMemberAttributes()) {
      if (MemberAttribute.ATTRIBUTE_GUEST.equals(memberAttribute.getAttributeName())) {
        return Boolean.valueOf(memberAttribute.getAttributeValue());
      }
    }
    return false;
  }

  /**
   * Sets the value that indicates if the Member has guest status
   *
   * @param isGuest {@link}
   */
  public void setGuest(boolean isGuest) {
    for (MemberAttribute memberAttribute : getMemberAttributes()) {
      if (MemberAttribute.ATTRIBUTE_GUEST.equals(memberAttribute.getAttributeName())) {
        memberAttribute.setAttributeValue(Boolean.toString(isGuest));
        return;
      }
    }
    MemberAttribute memberAttribute = new MemberAttribute(this.getId(),
            MemberAttribute.ATTRIBUTE_GUEST, Boolean.toString(isGuest));
    addMemberAttribute(memberAttribute);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    return prime * result + ((id == null) ? 0 : id.hashCode());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Member other = (Member) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }
}
