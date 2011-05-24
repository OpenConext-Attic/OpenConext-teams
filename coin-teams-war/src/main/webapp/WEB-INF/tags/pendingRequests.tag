<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%--
  ~ Copyright 2011 SURFnet bv, The Netherlands
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<c:if test="${offset eq 0 and fn:length(pendingRequests)>0}">
  <br class="clear" />
  <h2><spring:message code="jsp.detailteam.PendingRequests"/></h2>
  <div class="team-table-wrapper">
  <table class="team-table">
    <thead>
    <th colspan="3"><spring:message code='jsp.detailteam.Name'/></th>
    </thead>
    <tbody>
    <c:forEach var="pending" items="${pendingRequests}">
      <tr>
        <td><c:out value="${pending.displayName}"/></td>
        <c:url value="dodeleterequest.shtml" var="deleteRequestUrl">
          <c:param name="team" value="${team.id}"/>
          <c:param name="member" value="${pending.id}"/>
          <c:param name="view" value="${view}" />
        </c:url>
        <td><a href="${deleteRequestUrl}">
          <spring:message code="jsp.detailteam.DenyJoinRequest"/></a></td>
        <c:url value="doapproverequest.shtml" var="approveRequestUrl">
          <c:param name="team" value="${team.id}"/>
          <c:param name="member" value="${pending.id}"/>
          <c:param name="view" value="${view}" />
        </c:url>
        <td><a href="${approveRequestUrl}">
          <spring:message code="jsp.detailteam.AcceptJoinRequest"/></a>
        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
  </div>

  <br class="clear" />
  <h2><spring:message code="jsp.detailteam.TeamMembers"/></h2>
</c:if>