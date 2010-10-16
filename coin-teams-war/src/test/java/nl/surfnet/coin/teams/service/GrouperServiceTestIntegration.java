/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcFindStems;
import edu.internet2.middleware.grouperClient.api.GcGetGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGetMemberships;
import edu.internet2.middleware.grouperClient.api.GcGetPermissionAssignments;
import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindStemsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembership;
import edu.internet2.middleware.grouperClient.ws.beans.WsPermissionAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsStem;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * 
 *
 */
public class GrouperServiceTestIntegration {

    private String clientVersion = "v1_6_000";

    @Test
    public void testNativeClient() {
        GcGetGroups getGroups = new GcGetGroups();
        getGroups.assignClientVersion(clientVersion);
        String subjectId = "urn:collab:person:surfnet.nl:hansz";
        getGroups.addSubjectId(subjectId);
        WsSubjectLookup theActAsSubject = new WsSubjectLookup();
        theActAsSubject.setSubjectId(subjectId);
        getGroups.assignActAsSubject(theActAsSubject);
        edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults result = getGroups.execute();
        WsGetGroupsResult[] results = result.getResults();
        assertEquals(1, results.length);

        GcGetMembers getMember = new GcGetMembers();
        // getMember.addSourceId(subjectId);
        getMember.assignActAsSubject(theActAsSubject);
        getMember.assignIncludeSubjectDetail(Boolean.TRUE);
        getMember.assignIncludeGroupDetail(Boolean.TRUE);
        String uuid = results[0].getWsGroups()[1].getUuid();
        getMember.addGroupName("test:3tu_identity_management");
        WsGetMembersResults execute = getMember.execute();
        WsGetMembersResult[] results2 = execute.getResults();

        GcFindGroups findGroups = new GcFindGroups();
        findGroups.assignActAsSubject(theActAsSubject);
        findGroups.assignClientVersion(clientVersion);
        findGroups.assignIncludeGroupDetail(Boolean.TRUE);
        
        WsQueryFilter queryFilter = new WsQueryFilter();
        queryFilter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
        queryFilter.setGroupName("W");
        //queryFilter.setQueryTerm("W");
        findGroups.assignQueryFilter(queryFilter);
        WsFindGroupsResults findResults = findGroups.execute();
        WsGroup[] groupResults = findResults.getGroupResults();
        
        GcGetMemberships memberships = new GcGetMemberships();
        memberships.assignActAsSubject(theActAsSubject);
        memberships.assignClientVersion(clientVersion);
        memberships.assignIncludeGroupDetail(Boolean.TRUE);
        memberships.assignIncludeSubjectDetail(Boolean.TRUE);
        //memberships.addWsSubjectLookup(theActAsSubject);
        memberships.addGroupName("test:3tu_identity_management");
        WsGetMembershipsResults execute2 = memberships.execute();
        WsMembership[] wsMemberships = execute2.getWsMemberships();
        
        GcGetSubjects getSubject = new GcGetSubjects();
        getSubject.assignActAsSubject(theActAsSubject);
        getSubject.assignClientVersion(clientVersion);
        getSubject.assignIncludeSubjectDetail(Boolean.TRUE);
        getSubject.assignSearchString("H");
        getSubject.addSubjectAttributeName("mail");
        getSubject.addSubjectAttributeName("displayName");
        WsGetSubjectsResults execute3 = getSubject.execute();
        WsSubject[] wsSubjects = execute3.getWsSubjects();
        
        GcGetPermissionAssignments permissions = new GcGetPermissionAssignments();
        permissions.assignActAsSubject(theActAsSubject);
        permissions.assignClientVersion(clientVersion);
        permissions.assignIncludePermissionAssignDetail(true);
        permissions.addSubjectLookup(theActAsSubject);
        WsGetPermissionAssignmentsResults execute4 = permissions.execute();
        WsPermissionAssign[] wsPermissionAssigns = execute4.getWsPermissionAssigns();
        
        GcGetGrouperPrivilegesLite privileges = new GcGetGrouperPrivilegesLite();
        privileges.assignActAsSubject(theActAsSubject);
        privileges.assignClientVersion(clientVersion);
        privileges.assignGroupName("test:3tu_identity_management");
        //privileges.
        //privileges.assignSubjectLookup(theActAsSubject);
        WsGetGrouperPrivilegesLiteResult execute5 = privileges.execute();
        WsGrouperPrivilegeResult[] privilegeResults = execute5.getPrivilegeResults();
        
        GcFindStems stems = new GcFindStems();
        stems.assignActAsSubject(theActAsSubject);
        stems.assignClientVersion(clientVersion);
        WsStemQueryFilter theStemQueryFilter = new WsStemQueryFilter();
        theStemQueryFilter.setStemQueryFilterType("FIND_BY_STEM_NAME_APPROXIMATE");
        theStemQueryFilter.setStemName("E");
        stems.assignStemQueryFilter(theStemQueryFilter );
        WsFindStemsResults execute6 = stems.execute();
        WsStem[] stemResults = execute6.getStemResults();
    }

    // //https://spaces.internet2.edu/display/GrouperWG/Grouper+Web+Services
   
}
