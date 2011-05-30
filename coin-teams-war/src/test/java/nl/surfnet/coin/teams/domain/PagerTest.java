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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * test for {@link Pager}
 */
public class PagerTest {
  private static final int PAGESIZE = 10;

  @Test
  public void testTotalFilledUp() {
    Pager pager = new Pager(0, 0, PAGESIZE);
    assertEquals(0, pager.totalFilledUp());
    pager = new Pager(5, 0, PAGESIZE);
    assertEquals(10, pager.totalFilledUp());
    pager = new Pager(10, 0, PAGESIZE);
    assertEquals(10, pager.totalFilledUp());
    pager = new Pager(11, 0, PAGESIZE);
    assertEquals(20, pager.totalFilledUp());
    pager = new Pager(99, 0, PAGESIZE);
    assertEquals(100, pager.totalFilledUp());
    pager = new Pager(100, 0, PAGESIZE);
    assertEquals(100, pager.totalFilledUp());
  }

  @Test
  public void testEmptyResultSet() {
    List<Page> noPages = new ArrayList<Page>();
    Pager pager = new Pager(0, 0, PAGESIZE);
    assertNull(pager.getFirstPage());
    assertNull(pager.getPreviousPage());
    assertNull(pager.getNextPage());
    assertNull(pager.getLastPage());
    assertEquals(noPages, pager.getVisiblePages());
  }

  @Test
  public void testResultsetNotEnoughForPage() {
    Pager pager = new Pager(5, 0, PAGESIZE);
    assertNull(pager.getFirstPage());
    assertNull(pager.getPreviousPage());
    assertNull(pager.getNextPage());
    assertNull(pager.getLastPage());
    final List<Page> visiblePages = pager.getVisiblePages();
    assertEquals(0, visiblePages.size());
  }

  @Test
  public void testResultsetTwoPages() {
    Pager pager = new Pager(15, 0, PAGESIZE);
    assertNull(pager.getFirstPage());
    assertNull(pager.getPreviousPage());
    assertEquals(new Page(2, 10, false), pager.getNextPage());
    assertEquals(new Page(2, 10, false), pager.getLastPage());
    final List<Page> visiblePages = pager.getVisiblePages();
    assertEquals(2, visiblePages.size());
    assertEquals(1, visiblePages.get(0).getPageNumber());
  }

  @Test
  public void testResultsetThirdPage() {
    Pager pager = new Pager(65, 20, PAGESIZE);
    assertEquals(new Page(1, 0, false), pager.getFirstPage());
    assertEquals(new Page(2, 10, false), pager.getPreviousPage());
    assertEquals(new Page(4, 30, false), pager.getNextPage());
    assertEquals(new Page(7, 60, false), pager.getLastPage());
    final List<Page> visiblePages = pager.getVisiblePages();
    assertEquals(5, visiblePages.size());
    assertEquals(1, visiblePages.get(0).getPageNumber());
    assertEquals(new Page(3, 20, true), visiblePages.get(2));
  }

  @Test
  public void testMalcolmInTheMiddle() {
    Pager pager = new Pager(169, 70, PAGESIZE);
    assertEquals(new Page(1, 0, false), pager.getFirstPage());
    assertEquals(new Page(7, 60, false), pager.getPreviousPage());
    assertEquals(new Page(9, 80, false), pager.getNextPage());
    assertEquals(new Page(17, 160, false), pager.getLastPage());
    final List<Page> visiblePages = pager.getVisiblePages();
    assertEquals(5, visiblePages.size());
    final Page firstVisible = new Page(6, 50, false);
    assertEquals(firstVisible, visiblePages.get(0));
    assertEquals(new Page(8, 70, true), visiblePages.get(2));
  }

  @Test
  public void testResultsetLastPageWithRemainder() {
    Pager pager = new Pager(93, 90, PAGESIZE);
    assertEquals(new Page(1, 0, false), pager.getFirstPage());
    assertEquals(new Page(9, 80, false), pager.getPreviousPage());
    assertNull(pager.getNextPage());
    assertNull(pager.getLastPage());
    final List<Page> visiblePages = pager.getVisiblePages();
    assertEquals(5, visiblePages.size());
    assertEquals(6, visiblePages.get(0).getPageNumber());
    assertEquals(new Page(10, 90, true), visiblePages.get(4));
  }

  @Test
  public void testResultsetLastPageNoRemainder() {
    Pager pager = new Pager(100, 90, PAGESIZE);
    assertEquals(new Page(1, 0, false), pager.getFirstPage());
    assertEquals(new Page(9, 80, false), pager.getPreviousPage());
    assertNull(pager.getNextPage());
    assertNull(pager.getLastPage());
    final List<Page> visiblePages = pager.getVisiblePages();
    assertEquals(5, visiblePages.size());
    assertEquals(6, visiblePages.get(0).getPageNumber());
    assertEquals(new Page(10, 90, true), visiblePages.get(4));
  }


}
