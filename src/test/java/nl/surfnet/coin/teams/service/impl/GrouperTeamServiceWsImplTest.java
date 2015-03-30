package nl.surfnet.coin.teams.service.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GrouperTeamServiceWsImplTest {
  @Test
  public void testPagenumber() throws Exception {
    GrouperTeamServiceWsImpl ws = new GrouperTeamServiceWsImpl();

    assertEquals(1, ws.pagenumber(0, 10));
    assertEquals(1, ws.pagenumber(10, 20));
    assertEquals(2, ws.pagenumber(10, 10));
    assertEquals(8, ws.pagenumber(70, 10));
  }
}
