/**
 * Copyright 2010
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

}
