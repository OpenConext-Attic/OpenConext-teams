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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.util.CollectionUtils;

/**
 * Team
 */
@SuppressWarnings("serial")
public class Team implements Serializable {
  private String id;
  private String name;
  private String description;
  private List<Member> members = new ArrayList<Member>();
  private Role viewerRole;
  private boolean viewable;
  private int numberOfMembers;

  public Team() {
    super();
  }

  /**
   * @param id          of the team
   * @param name        of the team
   * @param description extra description
   * @param members     {@link List} of {@link Member}'s
   */
  public Team(String id, String name, String description, List<Member> members) {
    super();
    this.id = id;
    this.name = name;
    this.description = description;
    this.members = members;
  }

  /**
   * @param id          of the team
   * @param name        of the team
   * @param description extra description
   * @param members     {@link List} of {@link Member}'s
   * @param viewable    if {@literal false} then it's a private team
   */
  public Team(String id, String name, String description, List<Member> members, boolean viewable) {
    this(id, name, description, members);
    this.viewable = viewable;
  }

  /**
   * @param id          of the team
   * @param name        of the team
   * @param description extra description
   */
  public Team(String id, String name, String description) {
    this(id, name, description, new ArrayList<Member>());
  }

  /**
   * @param id          of the team
   * @param name        of the team
   * @param description extra description
   * @param viewable    if {@literal false} then it's a private team
   */
  public Team(String id, String name, String description, boolean viewable) {
    this(id, name, description, new ArrayList<Member>());
    this.viewable = viewable;
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
  public List<Member> getMembers() {
    Collections.sort(members, new MemberComparator());
    return members;
  }

  /**
   * Add a member
   *
   * @param member the new member
   */
  public void addMembers(Member... member) {
    for (int i = 0; i < member.length; i++) {
      members.add(member[i]);
    }
  }

  /**
   * Remove members
   *
   * @param member varag of {@link Member}
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
   * @param person id of the person to assign the viewerRole to
   */
  public void setViewerRole(Role role) {
    this.viewerRole = role;
  }

  /**
   * @return the viewerRole
   */
  public Role getViewerRole() {
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Team other = (Team) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  /**
   * Comparator to sort members by name
   */
  class MemberComparator implements Comparator<Member> {

    @Override
    public int compare(Member member1, Member member2) {
        return member1.getName().compareToIgnoreCase(member2.getName());
    }

  }

  /**
   * @return the numberOfMembers
   */
  public int getNumberOfMembers() {
    return numberOfMembers;
  }

  /**
   * @param numberOfMembers the numberOfMembers to set
   */
  public void setNumberOfMembers(int numberOfMembers) {
    this.numberOfMembers = numberOfMembers;
  }
}
