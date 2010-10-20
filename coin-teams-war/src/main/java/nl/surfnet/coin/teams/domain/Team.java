/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Team
 * 
 */
@SuppressWarnings("serial")
public class Team implements Serializable {
  private String id;
  private String name;
  private String description;
  private Set<Member> members = new HashSet<Member>();
  private String viewerRole;
  private boolean viewable;

  /**
   * @param id
   * @param name
   * @param description
   * @param members
   * @param viewable
   */
  public Team(String id, String name, String description, Set<Member> members, boolean viewable) {
    super();
    this.id = id;
    this.name = name;
    this.description = description;
    this.members = members;
    this.viewable = viewable;
  }

  /**
   * @param id
   * @param name
   * @param description
   * @param viewable
   */
  public Team(String id, String name, String description, boolean viewable) {
    this(id, name, description, new HashSet<Member>(), viewable);
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the members
   */
  public Set<Member> getMembers() {
    return members;
  }

  /**
   * Add a member
   * 
   * @param member
   *          the new member
   */
  public void addMembers(Member... member) {
    for (int i = 0; i < member.length; i++) {
      members.add(member[i]);
    }
  }

  /**
   * Remove a member
   * 
   * @param members
   *          the members
   */
  public void removeMembers(Member... member) {
    for (int i = 0; i < member.length; i++) {
      members.remove(member[i]);
    }
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @param viewerRole the viewerRole to set
   */
  public void setViewerRole(String viewerRole) {
    this.viewerRole = viewerRole;
  }

  /**
   * @return the viewerRole
   */
  public String getViewerRole() {
    return viewerRole;
  }

  /**
   * @param viewable the viewable to set
   */
  public void setViewable(boolean viewable) {
    this.viewable = viewable;
  }

  /**
   * @return the viewable
   */
  public boolean isViewable() {
    return viewable;
  }

}
