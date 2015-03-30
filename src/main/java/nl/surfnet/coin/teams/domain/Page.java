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

/**
 * POJO for a Page within paging
 */
public class Page {

  private int pageNumber;
  private int offset;
  private boolean currentPage;

  public Page(int pageNumber, int offset, boolean currentPage) {
    this.pageNumber = pageNumber;
    this.offset = offset;
    this.currentPage = currentPage;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  /**
   * @return the startpoint in the totalresult for this {@link Page}
   */
  public int getOffset() {
    return offset;
  }

  /**
   * @return boolean that indicates if this {@link Page} is the requested page
   *         based on offset and pagesize (can be used for styling)
   */
  public boolean isCurrentPage() {
    return currentPage;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    Page that = (Page) obj;
    return (this.pageNumber == that.pageNumber
            && this.offset == that.offset
            && this.currentPage == that.isCurrentPage());
  }

  @Override
  public int hashCode() {
    int primitive = 31;
    int result = 1;
    result = result * primitive + pageNumber;
    result = result * primitive + offset;
    result = result * primitive + Boolean.valueOf(currentPage).hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Page{" +
            "pageNumber=" + pageNumber +
            ", offset=" + offset +
            ", currentPage=" + currentPage +
            '}';
  }
}
