/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.internet2.middleware.grouper.ws.soap.GrouperServicePortType;
import edu.internet2.middleware.grouper.ws.soap.xsd.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.xsd.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap.xsd.WsQueryFilter;
import edu.internet2.middleware.grouper.ws.soap.xsd.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap.xsd.WsSubjectLookup;


/**
 * 
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:coin-teams-context.xml", "classpath:coin-teams-properties-context.xml"})
public class GrouperServiceTest {

    @Autowired
    private GrouperServicePortType grouperService;

    //https://spaces.internet2.edu/display/GrouperWG/Grouper+Web+Services
    @Test
    public void testGetGroups() {
        String clientVersion = "v1_6_000";
        WsQueryFilter wsQueryFilter = new WsQueryFilter();
        wsQueryFilter.setQueryFilterType("FIND_BY_TYPE");
        //wsQueryFilter.setQueryTerm("ALL");
        
        WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup();
        String value = "urn:collab:person:surfnet.nl:hansz";
        actAsSubjectLookup.setSubjectId(value);
        //actAsSubjectLookup.setSubjectIdentifier(value);
        //actAsSubjectLookup.setSubjectSourceId(value);
        String includeGroupDetail = "T";
        List<WsParam> params = null;
        List<WsGroupLookup> wsGroupLookups = null;
        WsFindGroupsResults findGroups = grouperService.findGroups(clientVersion, wsQueryFilter, actAsSubjectLookup, includeGroupDetail, params,
                wsGroupLookups);
        assertNotNull(findGroups);
        List<WsSubjectLookup> subjectLookups = new ArrayList<WsSubjectLookup>();
        
        String memberFilter = null;
        String includeSubjectDetail = "Y";
        List<String> subjectAttributeNames = null;
        String fieldName = null;
        String scope = null;
        WsStemLookup wsStemLookup = null;
        String stemScope = null;
        String enabled = "T";
        String pageSize = null;
        String pageNumber = null;
        String sortString = null;
        String ascending = null;
        WsGetGroupsResults groups = grouperService.getGroups(clientVersion, subjectLookups, memberFilter, actAsSubjectLookup, includeGroupDetail, includeSubjectDetail, subjectAttributeNames, params, fieldName, scope, wsStemLookup, stemScope, enabled, pageSize, pageNumber, sortString, ascending);
        assertNotNull(groups);
        //http://stackoverflow.com/questions/2177153/get-rid-of-jaxbelement-in-classes-generated-by-wsimport-called-from-ant
    }
}
