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
package nl.surfnet.coin.teams.model;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import nl.surfnet.coin.teams.service.ProvisioningManager.Operation;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * AbstractScimBase.java
 * 
 */
@SuppressWarnings("serial")
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ScimEvent implements Serializable {

  private List<String> schemas = asList("urn:scim:schemas:core:1.0");

  @JsonIgnore
  private Operation operation;

  @JsonIgnore
  private ScimType type;

  private String id;
  private String displayName;
  private List<ScimMember> members;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public List<ScimMember> getMembers() {
    return members;
  }

  public void setMembers(List<ScimMember> members) {
    this.members = members;
  }

  public List<String> getSchemas() {
    return schemas;
  }

  public void setSchemas(List<String> schemas) {
    this.schemas = schemas;
  }

  public Operation getOperation() {
    return operation;
  }

  public void setOperation(Operation operation) {
    this.operation = operation;
  }

  public ScimType getType() {
    return type;
  }

  public void setType(ScimType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "ScimEvent [schemas=" + schemas + ", operation=" + operation + ", type=" + type + ", id=" + id + ", displayName=" + displayName
        + ", members=" + members + "]";
  }
}
