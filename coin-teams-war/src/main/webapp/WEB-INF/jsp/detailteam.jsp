<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://teamfn" prefix="teamfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
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

<c:set var="pageTitle"><c:out value="${team.name}" /> (<c:out value="${team.stem.name}" />)</c:set>
<teams:genericpage pageTitle="${pageTitle}">
<c:if test="${fn:length(message) > 0}"><div id="__notifyBar" class="hide"><spring:message code='${message}' /></div></c:if>
<c:if test="${role eq adminRole}">
  <div class="jquery-warning-bar <c:if test="${onlyAdmin ne true}">hide</c:if>" id="onlyAdmin">
    <p><spring:message code="jsp.detailteam.OnlyAdminWarning"/><a href="https://wiki.surfnet.nl/display/conextsupport/SURFconext+Teams+Best+Practice" target="_blank"><img src="media/question-mark.jpg"/></a></p>
  </div>
</c:if>
<%-- = TeamContainer --%>
<div class="section" id="TeamContainer">
  <%-- = Header --%>
  <div id="Header">
    <teams:teamOptions/>
    <br class="clear" />
    <h1 class="team-title">${pageTitle}</h1>

    <c:if test="${role eq adminRole or role eq managerRole}">
      <c:if test="${displayAddExternalGroupToTeam eq true}">
        <p class="add">
          <c:url value="/addexternalgroup.shtml" var="addexternalgroupUrl"><c:param name="teamId" value="${team.id}" /><c:param name="view" value="${view}" /></c:url>
          <a class="button" href="<c:out value="${addexternalgroupUrl}"/>"><spring:message code="jsp.addexternalgroup.Title"/></a>
        </p>
      </c:if>
      <p class="add">
        <c:url value="/addmember.shtml" var="addmemberUrl"><c:param name="team" value="${team.id}" /><c:param name="view" value="${view}" /></c:url>
        <a class="button" href="<c:out value="${addmemberUrl}"/>"><spring:message code='jsp.addmember.Title' /></a>
      </p>
    </c:if>

    <br class="clear" />
  <%-- / Header --%>
  </div>
  <%-- = Content --%>
  <div id="Content">
    <p class="description">
      <c:set var="noDescription"><spring:message code='jsp.general.NoDescription' /></c:set>
      <c:out value="${team.descriptionAsHtml}" default="${noDescription}" escapeXml="false"/>
    </p>

    <c:if test="${fn:length(teamExternalGroups)>0}">
      <h2><spring:message code="jsp.detailteam.InstitutionalGroups"/></h2>
      <div class="team-table-wrapper">
        <table class="team-table">
          <thead>
          <tr>
            <c:if test="${role eq adminRole}">
              <th class="remove"></th>
            </c:if>

            <th class="logo"></th>
            <th class="name"><spring:message code='jsp.detailteam.Name'/></th>
            <th class="description"><spring:message code="jsp.general.Description"/></th>
          </tr>
          </thead>
          <tbody>
            <%--@elvariable id="teamExternalGroups" type="java.util.List<nl.surfnet.coin.teams.domain.TeamExternalGroup>"--%>
          <%--@elvariable id="groupProviderMap" type="java.util.Map<java.lang.String, nl.surfnet.coin.teams.domain.GroupProvider>"--%>
          <c:forEach var="teg" items="${teamExternalGroups}">
          	<tr>
          	<c:url var="deleteexternalgroup" value="/deleteexternalgroup.shtml">
              <c:param name="teamId" value="${team.id}"/>
              <c:param name="groupIdentifier" value="${teg.externalGroup.identifier}"/>
              <c:param name="token" value="${tokencheck}"/>
              <c:param name="view" value="${view}"/>
            </c:url>
            <c:if test="${role eq adminRole}"><td><a href="${deleteexternalgroup}" class="RemoveExternalGroup delete"><spring:message
                code="jsp.detailteam.RemoveExternalGroupFromTeam"/></a></td></c:if>
            <td>
              <c:set var="groupProvider" target="nl.surfnet.coin.teams.domain.GroupProvider"
                     value="${groupProviderMap[teg.externalGroup.groupProviderIdentifier]}"/>
              <c:if test="${not empty groupProvider and not empty groupProvider.logoUrl}">
              <img src="<c:out value="${groupProvider.logoUrl}"/>" alt="" height="15px"/>
              </c:if>
            </td>
            <td><c:out value="${teg.externalGroup.name}"/></td>
            <td><c:out value="${teg.externalGroup.description}"/></td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
      <br class="clear"/>

      <h2><spring:message code="jsp.detailteam.IndividualMembers"/></h2>
    </c:if>

    <c:choose>
      <c:when test="${role eq adminRole or role eq managerRole}">
        <teams:pendingRequests/>
        <teams:detailTeamForm/>
      </c:when>
      <c:when test="${role eq memberRole}">
        <teams:detailTeamForm/>
      </c:when>
      <c:otherwise>
        <p class="more">
          <c:url value="/jointeam.shtml" var="joinUrl"><c:param name="team" value="${team.id}"/>
            <c:param name="view" value="${view}"/></c:url>
          <a class="button" href="<c:out value="${joinUrl}"/>"><spring:message code='jsp.detailteam.Join'/></a>
        </p>
        <div class="clear" ></div>
      </c:otherwise>
    </c:choose>


  <%-- / Content --%>
  </div>
<%-- / TeamContainer --%>
</div>
</teams:genericpage>