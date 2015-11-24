<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://teamfn" prefix="teamfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams" %>
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
<c:set var="pageTitle"><spring:message code="jsp.home.MyInvitations"/></c:set>
<teams:genericpage pageTitle="${pageTitle}">
  <div id="header">
    <c:url value="home.shtml" var="backUrl"><c:param name="teams" value="my"/><c:param name="view"
                                                                                       value="${view}"/></c:url>
    <p class="back"><a href="${backUrl}">&lt; <spring:message code='jsp.home.MyTeams'/></a></p>
    <br class="clear"/>
    <h1>${pageTitle}</h1>
  </div>
  <div id="content">
    <br class="clear" />
    <c:choose>
      <c:when test="${fn:length(invitations) eq 0}">
        <p><spring:message code="jsp.home.MyInvitations.NotFound"/></p>
      </c:when>
      <c:otherwise>
      <div class="team-table-wrapper">
        <table class="team-table">
          <thead>
            <tr>
              <th><spring:message code='jsp.home.table.Team' /></th>
              <th></th>
              <th></th>
            </tr>
            </thead>
          <tbody>
          <c:forEach items="${invitations}" var="invitation">
            <tr>
              <td>
                <c:forEach var="team" items="${teams}">
                  <c:if test="${team.id eq invitation.teamId}">
                    <c:out value="${team.name}"/>
                  </c:if>
                </c:forEach>
              </td>
              <td><a href="<c:out value="acceptInvitation.shtml?id=${invitation.invitationHash}&view=${view}"/>"><spring:message code="jsp.acceptinvitation.Accept"/></a></td>
              <td><a href="<c:out value="declineInvitation.shtml?id=${invitation.invitationHash}&view=${view}"/>"><spring:message code="jsp.declineinvitation.Decline"/></a></td>
            </tr>
          </c:forEach>
          </tbody>
          </table>
      </c:otherwise>
    </c:choose>
  </div>
</teams:genericpage>