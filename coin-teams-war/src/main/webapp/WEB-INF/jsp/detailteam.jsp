<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://teamfn" prefix="teamfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
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
<!-- = TeamContainer -->
<div class="section" id="TeamContainer">
  <!-- = Header -->
  <div id="Header">
    <c:if test="${fn:length(message) > 0}"><div id="__notifyBar" class="hide"><spring:message code='${message}' /></div></c:if>
    <teams:teamOptions/>
    <br class="clear" />
    <h1 class="team-title"><c:out value="${team.name}" /></h1>

    <c:if test="${role eq adminRole or role eq managerRole}">
      <p class="add">
        <c:url value="addmember.shtml" var="addmemberUrl"><c:param name="team" value="${team.id}" /><c:param name="view" value="${view}" /></c:url>
        <a class="button-primary" href="${addmemberUrl}"><spring:message code='jsp.addmember.Title' /></a>
      </p>
    </c:if>

    <br class="clear" />
  <!-- / Header -->
  </div>
  <!-- = Content -->
  <div id="Content">
    <p class="description">
      <c:set var="noDescription"><spring:message code='jsp.general.NoDescription' /></c:set>
      <c:out value="${team.description}" default="${noDescription}"/>
    </p>

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
          <c:url value="jointeam.shtml" var="joinUrl"><c:param name="team" value="${team.id}"/>
            <c:param name="view" value="${view}"/></c:url>
          <a class="button-primary" href="${joinUrl}"><spring:message code='jsp.detailteam.Join'/></a>
        </p>
        <br class="clear" />
      </c:otherwise>
    </c:choose>


  <!-- / Content -->
  </div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>