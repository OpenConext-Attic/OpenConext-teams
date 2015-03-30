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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.List;

/**
 * Result for a teams query
 */
public class TeamResultWrapper {

  private List<Team> teams;
  private int totalCount;
  private int offset;
  private int pageSize;
  private Pager pager;

  public TeamResultWrapper(List<Team> teams, int totalCount, int offset, int pageSize) {
    super();
    this.teams = teams;
    this.totalCount = totalCount;
    this.offset = offset;
    this.pageSize = pageSize;
  }

  /**
   * @return the teams
   */
  public List<Team> getTeams() {
    return teams;
  }

  /**
   * @return offset for the current resultset
   */
  public int getOffset() {
    return offset;
  }

  /**
   * @return (maximum) pagesize for the current resultset
   */
  public int getPageSize() {
    return pageSize;
  }

  public int getTotalCount() {
    return totalCount;
  }

  /**
   * @return {@link Pager}
   */
  public Pager getPager() {
    if (pager == null) {
      pager = new Pager(totalCount, offset, pageSize);
    }
    return pager;
  }

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("totalCount", totalCount)
      .append("offset", offset)
      .append("pageSize", pageSize)
      .append("teams", teams)
      .toString();
  }
}
