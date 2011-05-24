<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://teamfn" prefix="teamfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams" %>
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

<teams:genericpage>
  <div id="header">
    <ul class="team-actions">
      <c:url value="home.shtml" var="myTeamsUrl"><c:param name="teams" value="my"/><c:param name="view"
                                                                                            value="${view}"/></c:url>
      <c:url value="home.shtml" var="allTeamsUrl"><c:param name="teams" value="all"/><c:param name="view"
                                                                                              value="${view}"/></c:url>
      <c:url value="myinvitations.shtml" var="myInvitationsUrl"><c:param name="view" value="${view}"/></c:url>
      <li class="first"><a class="btn-my-teams" href="${myTeamsUrl}"><spring:message code='jsp.home.MyTeams'/></a></li>
      <li class="middle"><a class="btn-all-teams" href="${allTeamsUrl}"><spring:message code='jsp.home.AllTeams'/></a></li>
      <li class="last"><a href="${myInvitationsUrl}" class="selected"><spring:message code="jsp.home.MyInvitations"/></a></li>
    </ul>
    <br class="clear" />
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
              <td><a href="acceptInvitation.shtml?id=${invitation.invitationHash}"><spring:message code="jsp.acceptinvitation.Accept"/></a></td>
              <td><a href="declineInvitation.shtml?id=${invitation.invitationHash}"><spring:message code="jsp.declineinvitation.Decline"/></a></td>
            </tr>
          </c:forEach>
          </tbody>
          </table>
      </c:otherwise>
    </c:choose>
  </div>
</teams:genericpage>