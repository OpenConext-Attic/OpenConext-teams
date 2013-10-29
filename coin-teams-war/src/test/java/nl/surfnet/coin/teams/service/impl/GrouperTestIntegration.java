package nl.surfnet.coin.teams.service.impl;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GrouperTestIntegration {


  @Test
  public void getPublicGroupsForUserUnFiltered() {
    WsQueryFilter filter = new WsQueryFilter();
    filter.setPageSize("10");
    filter.setPageNumber("1");
    filter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
    filter.setGroupName("%");
    String personId = "urn:collab:person:example.com:admin";
    WsFindGroupsResults results = new GcFindGroups()
            .assignQueryFilter(filter)
            .assignIncludeGroupDetail(false)
            .assignActAsSubject(new WsSubjectLookup(personId, null, null))
            .execute();
  }

  @Test
  public void getPublicGroupsForUserFiltered() {
    WsQueryFilter filter = new WsQueryFilter();
    filter.setPageSize("10");
    filter.setPageNumber("1");
    filter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
    filter.setGroupName("%Nog%");
    String personId = "urn:collab:person:example.com:admin";
    WsFindGroupsResults results = new GcFindGroups()
            .assignQueryFilter(filter)
            .assignIncludeGroupDetail(false)
            .assignActAsSubject(new WsSubjectLookup(personId, null, null))
            .execute();
    assertEquals(5, results.getGroupResults().length);
  }

  @Test
  public void getMemberGroupsFiltered() {
    String personId = "urn:collab:person:example.com:admin";
    /*

    GcGetMemberships: membership, but no pagination, no filtering
    GcFindGroups: filtering, pagination, but no membership
    GcGetGroups, membership, pagination, no filtering (or has it? Scope?)
     */

    WsGetGroupsResults results = new GcGetGroups()
            .addSubjectId(personId)
            .assignPageNumber(1)
            .assignPageSize(10)
            .assignScope("%nog%")
            .execute();
    assertEquals(5, results.getResults()[0].getWsGroups().length);
    System.out.println(results.getResults()[0].getWsGroups()[0].getDisplayExtension());
  }

}
