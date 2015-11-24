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

package teams.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for paging
 */
public class Pager {
  private int totalCount;
  private int offset;
  private int pageSize;

  private static final int MAX_VISIBLE = 5;
  private static final int PAGES_BEFORE = 2; // Math.ceil(MAX_VISIBLE/2)

  public Pager(int totalCount, int offset, int pageSize) {
    this.totalCount = totalCount;
    this.offset = offset;
    this.pageSize = pageSize;
  }

  /**
   * @return the totalCount
   */
  public int getTotalCount() {
    return totalCount;
  }

  /**
   * @return {@link teams.domain.Page} in paging that is the first possible page
   *         or {@literal null} if no such navigation item is needed (first page is current)
   */
  public Page getFirstPage() {
    if (totalCount == 0 || offset < pageSize) {
      return null;
    }
    return new Page(1, 0, false);
  }

  /**
   * @return {@link teams.domain.Page} in paging that goes to the previous page
   *         or {@literal null} if no such item is needed (first page is current)
   */
  public Page getPreviousPage() {
    if (totalCount == 0 || offset < pageSize) {
      return null;
    }
    int pageNumber = (offset / pageSize);
    return new Page(pageNumber, offset - pageSize, false);
  }

  /**
   * @return {@link teams.domain.Page} in paging that goes to the next page
   *         or {@literal null} if no such item is needed (last page is current)
   */
  public Page getNextPage() {
    if (offset + pageSize >= totalFilledUp()) {
      return null;
    }
    int pageNumber = (offset / pageSize) + 2;
    return new Page(pageNumber, offset + pageSize, false);
  }

  /**
   * @return {@link teams.domain.Page} in paging that goes to the last possible page
   *         or {@literal null} if no such item is needed (last page is current)
   */
  public Page getLastPage() {
    if (offset + pageSize >= totalFilledUp()) {
      return null;
    }
    int pageNumber = (totalFilledUp() / pageSize);
    return new Page(pageNumber, (pageNumber - 1) * pageSize, false);

  }

  /**
   * Start algorithm, example: pagesize 10, visible pages 5, total pages 15
   * <ul>
   * <li>Current page is 4 (first 5): start at 1: 1 - 2 - 3 - <b>4</b> - 5</li>
   * <li>Current page is 12 (last 5): start at 11: 11 - <b>12</b> - 13 - 14 - 15 </li>
   * <li>Current page is 8 (middle): start at 6: 6 - 7 - <b>8</b> - 9 - 10</li>
   * </ul>
   * If there is at most 1 page, an empty list is returned.
   *
   * @return List of {@link teams.domain.Page}'s that should be visible (sliding pager),
   *         can be empty
   */
  public List<Page> getVisiblePages() {
    List<Page> visiblePages = new ArrayList<Page>();
    if (totalCount < pageSize) {
      return visiblePages;
    }
    int start;
    if (offset < (MAX_VISIBLE * pageSize)) {
      // 'first 5'
      start = 0;
    } else if (offset + (MAX_VISIBLE * pageSize) > totalFilledUp()) {
      // 'last 5'
      start = totalFilledUp() - ((MAX_VISIBLE) * pageSize);
    } else {
      // 'in the middle'
      start = offset - (PAGES_BEFORE * pageSize);
    }

    for (int i = 0; i < MAX_VISIBLE; i++) {
      if (start + pageSize > totalFilledUp()) {
        // in case we have only 1 till 4 pages
        break;
      }
      final Page page = new Page((start / pageSize + 1), start, start == offset);
      visiblePages.add(page);
      start += pageSize;
    }

    return visiblePages;
  }

  /**
   * Rounds up the totalCount to the next page.
   * <p/>Example with pagesize 10:
   * <ul>
   * <li>0 rounded to 0</li>
   * <li>1 rounded to 10</li>
   * <li>9 rounded to 10</li>
   * <li>10 rounded to 10</li>
   * <li>11 rounded to 20</li>
   * </ul>
   *
   * @return totalCount to the next page.
   */
  int totalFilledUp() {
    return (int) ((Math.ceil((double) totalCount / (double) pageSize)) * pageSize);
  }

  /**
   * @return offset the startpoint within the totalresult for this request
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
}
