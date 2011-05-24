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
import java.util.HashSet;
import java.util.Set;

/**
 * A member of a team
 * 
 */
public class Member implements Serializable {
 
  private Set<Role> roles;
  private String name;
  private String id;
  private String email;

  public static final Set<Role> member = new HashSet<Role>();//Collections.singleton(Role.Member);
  
  /**
   * @param roles
   * @param name
   * @param id
   * @param email
   */
  public Member(Set<Role> roles, String name, String id, String email) {
    super();
    this.roles = roles;
    this.name = name;
    this.id = id;
    this.email = email;
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
   * 
   * @param role the role to be added
   * @return boolean is successful
   */
  
  public boolean addRole(Role role) {
    if (this.roles == null) {
      this.roles= new HashSet<Role>();
    }
    return roles.add(role);
  }

  /**
   * 
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
   * Copy the {@link Member}
   * @return copy of this instance
   */
  public Member copy() {
    return new Member(new HashSet<Role>(getRoles()),getName(),getId(),getEmail());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Member other = (Member) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
}
