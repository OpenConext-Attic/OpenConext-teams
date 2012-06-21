<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%--
  Copyright 2012 SURFnet bv, The Netherlands

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  --%>

<c:if test="${pager.offset eq 0 and fn:length(pendingRequests)>0}">
  <br class="clear" />
  <h2><spring:message code="jsp.detailteam.PendingRequests"/></h2>
  <div class="team-table-wrapper">
  <table class="team-table">
    <thead>
    <th><spring:message code='jsp.detailteam.Name'/></th>
    <th><spring:message code='jsp.general.Email'/></th>
    <th colspan="2"></th>
    </thead>
    <tbody>
    <c:forEach var="pending" items="${pendingRequests}">
      <%-- Ugly hack to either call nl.surfnet.coin.teams.domain.Person#getEmail first.
      Doesn't always work, so then try nl.surfnet.coin.teams.domain.Person#get("emails") --%>
      <c:catch>
        <c:if test="${not empty pending.email}">
          <c:set var="email" value="${pending.email}"/>
        </c:if>
      </c:catch>
      <c:if test="${empty email}">
        <c:catch>
          <c:if test="${fn:length(pending.emails)>0}">
            <c:set var="email" value="${pending.emails[0].value}"/>
          </c:if>
        </c:catch>
      </c:if>
      <tr>
        <td><c:out value="${pending.displayName}"/></td>
        <td>
          <c:out value="${email}"/>
        </td>
        <td>
          <form name="deleteRequestForm" action="dodeleterequest.shtml" method="POST">
            <input type="hidden" name="token" value="<c:out value="${tokencheck}"/>"/>
            <input type="hidden" name="team" value="<c:out value="${team.id}"/>"/>
            <input type="hidden" name="member" value="<c:out value="${pending.id}"/>"/>
            <input type="hidden" name="view" value="<c:out value="${view}"/>"/>
          </form>
          <a href="#" class="deny-join-request"><spring:message code="jsp.detailteam.DenyJoinRequest"/></a>
        </td>
        <td>
          <form name="approveRequestForm" action="doapproverequest.shtml" method="POST">
            <input type="hidden" name="token" value="<c:out value="${tokencheck}"/>"/>
            <input type="hidden" name="team" value="<c:out value="${team.id}"/>"/>
            <input type="hidden" name="member" value="<c:out value="${pending.id}"/>"/>
            <input type="hidden" name="view" value="<c:out value="${view}"/>"/>
          </form>
          <a href="#" class="approve-join-request"><spring:message code="jsp.detailteam.AcceptJoinRequest"/></a>
        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
  </div>

  <br class="clear" />
  <h2><spring:message code="jsp.detailteam.TeamMembers"/></h2>
</c:if>