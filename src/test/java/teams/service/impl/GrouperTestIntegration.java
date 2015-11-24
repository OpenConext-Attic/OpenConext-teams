package teams.service.impl;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class GrouperTestIntegration {

  private static final Logger LOG = LoggerFactory.getLogger(GrouperTestIntegration.class);

  @Test
  public void getPublicGroupsForUserUnFiltered() {
    WsQueryFilter filter = new WsQueryFilter();
    filter.setPageSize("10");
    filter.setPageNumber("1");
    filter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
    filter.setGroupName("%");
    String personId = "urn:collab:person:example.com:admin";

    new GcFindGroups()
            .assignQueryFilter(filter)
            .assignIncludeGroupDetail(false)
            .assignActAsSubject(new WsSubjectLookup(personId, null, null))
            .execute();
  }

  @Test
  public void foo() {
    WsQueryFilter filter = new WsQueryFilter();
    String partOfGroupname = "foo";

    filter.setGroupName("nl:surfnet:diensten:%" + partOfGroupname + "%");
    filter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
    filter.setStemName("nl:surfnet:diensten");
    WsFindGroupsResults results = new GcFindGroups()
            .assignQueryFilter(filter)
            .assignActAsSubject(new WsSubjectLookup("urn:collab:person:example.com:somuser", "", ""))
            .assignIncludeGroupDetail(false)
            .execute();

    assertEquals(2, results.getGroupResults().length);
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
  }

  @Test
  public void privs() {
    String groupName = "nl:surfnet:diensten:archimate_cursisten";
    String userId = "urn:collab:person:surfnet.nl:niels";
    WsGetGrouperPrivilegesLiteResult privilegesResults = new GcGetGrouperPrivilegesLite()
            .assignSubjectLookup(new WsSubjectLookup(userId, null, null))
            .assignActAsSubject(new WsSubjectLookup(userId, "", ""))
            .assignGroupName(groupName)
            .assignIncludeGroupDetail(false)
            .assignIncludeSubjectDetail(false)
            .execute();

    for (WsGrouperPrivilegeResult privResult : privilegesResults.getPrivilegeResults()) {
      LOG.debug("{} {}, {}, {}, {}, {}",
              privResult.getPrivilegeName(),
              privResult.getPrivilegeType(),
              privResult.getAllowed(),
              privResult.getRevokable(),
              privResult.getOwnerSubject().getSourceId(),
              privResult.getWsStem()
      );
    }
  }
}
