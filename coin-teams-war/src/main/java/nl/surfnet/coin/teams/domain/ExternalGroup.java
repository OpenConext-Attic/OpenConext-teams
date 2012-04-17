/*
 * Copyright 2012 SURFnet bv, The Netherlands
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

import nl.surfnet.coin.shared.domain.DomainObject;

/**
 * Metadata of an external group
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "external_groups")
@Proxy(lazy = false)
public class ExternalGroup extends DomainObject {

  @Column
  private String identifier;

  @Column
  private String name;

  @Column
  @Lob
  private String description;

  @Column(name = "group_provider")
  private String groupProvider;

  /**
   * @return identifier of the group {@literal urn:collab:groups:university.nl:students}
   */
  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * @return human readable name of the group {@literal University: Students}
   */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return long description of the group
   *         {@literal This is the group that contains all students from the University}
   */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return identifier of the group provider {@literal University.nl}
   */
  public String getGroupProvider() {
    return groupProvider;
  }

  public void setGroupProvider(String groupProvider) {
    this.groupProvider = groupProvider;
  }
}
