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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Proxy;

import nl.surfnet.coin.shared.domain.DomainObject;

/**
 * The link between a team (SURFConext) and external groups from the universities
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "team_external_groups",
    uniqueConstraints = @UniqueConstraint(columnNames = {"grouper_team_id", "external_groups_id"}))
@Proxy(lazy = false)
public class TeamExternalGroup extends DomainObject {

  @Column(name = "grouper_team_id")
  private String grouperTeamId;

  @ManyToOne
  @JoinColumn(name = "external_groups_id")
  private ExternalGroup externalGroup;

  public String getGrouperTeamId() {
    return grouperTeamId;
  }

  public void setGrouperTeamId(String grouperTeamId) {
    this.grouperTeamId = grouperTeamId;
  }

  public ExternalGroup getExternalGroup() {
    return externalGroup;
  }

  public void setExternalGroup(ExternalGroup externalGroup) {
    this.externalGroup = externalGroup;
  }

}
