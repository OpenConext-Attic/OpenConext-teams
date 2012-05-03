<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
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
<c:set var="pageTitle"><spring:message code='jsp.editteam.Title' /></c:set>
<teams:genericpage pageTitle="${pageTitle}">
<!-- = TeamContainer -->
<div class="section" id="TeamContainer">
  <!-- = Header -->
  <div id="Header">
    <h1>${pageTitle}</h1>
    <c:url value="home.shtml" var="closeUrl"><c:param name="teams" value="my" /><c:param name="view" value="${view}" /></c:url>
    <p class="close"><a href="${closeUrl}"><spring:message code='jsp.general.CloseForm' /></a></p>
  <!-- / Header -->
  </div>
  <!-- = Content -->
  <div id="Content">
    <c:url value="doeditteam.shtml" var="doEditTeamUrl"><c:param name="view" value="${view}" /></c:url>
    <form id="EditTeamForm" action="<c:out value='${doEditTeamUrl}' />" method="post">
      <p class="label-field-wrapper">
        <input type="hidden" name="token" value="<c:out value="${tokencheck}"/>"/>
        <input type="hidden" name="team" value="<c:out value='${team.id}' />" />
        <input type="hidden" name="view" value="<c:out value='${view}' />" />
        <label for="TeamName"><spring:message code='jsp.general.TeamName' /></label>
        <input id="TeamName" type="text" name="teamName" value="<c:out value="${team.name}" />" readonly="readonly" class="required" />
      </p>
      <p class="label-field-wrapper">
        <label for="TeamDescription"><spring:message code='jsp.general.Description' /></label>
        <textarea id="TeamDescription" name="description"><c:out value="${team.description}" /></textarea>
      </p>
      <p class="label-field-wrapper">
        <span class="consent-wrapper">&nbsp;</span>
        <input id="TeamViewability" type="checkbox" name="viewabilityStatus" value="1"<c:if test="${team.viewable ne false}"> checked</c:if> />
        <label for="TeamViewability" class="consent"><spring:message code='jsp.general.TeamViewability' /></label>
      </p>
      <p class="submit-wrapper">
        <input class="button-primary" type="submit" name="editTeam" value="<spring:message code='jsp.editteam.Submit' />" />
        <input class="button-secondary" type="submit" name="cancelEditTeam" value="<spring:message code='jsp.general.Cancel' />" />
      </p>
    </form>
    <div class="clear"></div>
    <!-- / Content -->
  </div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>