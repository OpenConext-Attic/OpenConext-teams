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

package teams.domain;

/**
 * Metadata of an external group stored in the SURFteams database. We have this redundant storage to show
 * the metadata to SURFteam member who are not a member of the external group.
 * <p/>
 * Because the original data behind this bean comes from external sources, we do all CRUD operations by jdbc templates
 * instead of Hibernate.
 * <p/>
 * MySQL query to create this table:
 * <pre>
 *   CREATE TABLE `external_groups` (
 * `id` bigint(20) NOT NULL AUTO_INCREMENT,
 * `description` longtext,
 * `group_provider` varchar(255) DEFAULT NULL,
 * `identifier` varchar(255) DEFAULT NULL,
 * `name` varchar(255) DEFAULT NULL,
 * PRIMARY KEY (`id`)
 * ) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8
 * </pre>
 */
@SuppressWarnings("serial")
public class ExternalGroup extends DomainObject {

  private String identifier;
  private String name;
  private String description;
  private String groupProviderIdentifier;

  /**
   * Field not stored in this database
   */
  private ExternalGroupProvider groupProvider;

  public ExternalGroup() {
  }

  public ExternalGroup(String id, String name, String description, ExternalGroupProvider groupProvider) {
    this.setIdentifier(id);
    this.setName(name);
    this.setDescription(description);
    this.setGroupProvider(groupProvider);
    this.setGroupProviderIdentifier(groupProvider.getIdentifier());
  }

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
   * {@literal This is the group that contains all students from the University}
   */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return identifier of the group provider, e.g. {@literal University.nl}
   */
  public String getGroupProviderIdentifier() {
    return groupProviderIdentifier;
  }

  public void setGroupProviderIdentifier(String groupProviderIdentifier) {
    this.groupProviderIdentifier = groupProviderIdentifier;
  }

  public ExternalGroupProvider getGroupProvider() {
    return groupProvider;
  }

  public void setGroupProvider(ExternalGroupProvider groupProvider) {
    this.groupProvider = groupProvider;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ExternalGroup that = (ExternalGroup) o;

    if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("ExternalGroup");
    sb.append("{id='").append(getId()).append('\'');
    sb.append(", identifier='").append(identifier).append('\'');
    sb.append(", name='").append(name).append('\'');
    sb.append(", description='").append(description).append('\'');
    sb.append(", groupProviderIdentifier='").append(groupProviderIdentifier).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
