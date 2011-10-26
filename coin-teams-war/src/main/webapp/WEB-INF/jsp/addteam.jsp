<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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
    <h1><spring:message code='jsp.addteam.Title' /></h1>
    <c:url value="home.shtml" var="closeUrl"><c:param name="teams" value="my" /><c:param name="view" value="${view}" /></c:url>
    <p class="close"><a href="${closeUrl}"><spring:message code='jsp.general.CloseForm' /></a></p>
  <!-- / Header -->
  </div>
  <!-- = Content -->
  <div id="Content">
    <c:url value="doaddteam.shtml" var="doAddTeamUrl"><c:param name="view" value="${view}" /></c:url>
    <form:form id="AddTeamForm" action="${doAddTeamUrl}" method="post" commandName="team">
      <input type="hidden" name="token" value="<c:out value='${tokencheck}'/>"/>
      <input type="hidden" name="view" value="<c:out value='${view}' />" />
      <p class="label-field-wrapper">
        <c:set var="errorClass"><c:if test="${not empty nameerror}">error</c:if></c:set>
        <label for="TeamName"><spring:message code='jsp.general.TeamName' /></label>
        <form:input path="name" id="TeamName" cssClass="required" cssErrorClass="error"/>
        <c:choose>
          <c:when test="${nameerror eq 'empty'}">
            <label for="TeamName" class="error"><spring:message code="jsp.error.Field.Required"/></label>
          </c:when>
          <c:when test="${nameerror eq 'duplicate'}">
            <label for="TeamName" class="error"><spring:message code="jsp.addteam.error.duplicate"/></label>
          </c:when>
        </c:choose>

      </p>
      <p class="label-field-wrapper">
        <label for="TeamDescription"><spring:message code='jsp.general.Description' /></label>
        <form:input path="description" id="TeamDescription"/>
      </p>
      <p class="label-field-wrapper">
        <span class="consent-wrapper">&nbsp;</span>
        <input id="TeamConsent" name="consent" type="checkbox"><label class="consent" for="TeamConsent"><spring:message code='jsp.addteam.Consent' /></label>
      </p>
      <p class="submit-wrapper">
        <input class="button-disabled" type="submit" name="createTeam" value="<spring:message code='jsp.addteam.Submit' />" disabled="disabled" />
        <input class="button-secondary" type="submit" name="cancelCreateTeam" value="<spring:message code='jsp.general.Cancel' />" />
        <%-- Mindgame: Checkbox "Make private" has opposite value of Team#isViewable --%>
        <c:set var="private"><c:if test="${team.viewable eq false}"> checked</c:if></c:set>
        <input id="TeamViewability" type="checkbox" name="viewabilityStatus" value="1" ${private}/>
        <label for="TeamViewability"><spring:message code='jsp.general.TeamViewability' /></label>
      </p>
    </form:form>
    <div class="clear"></div>
    <!-- / Content -->
  </div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>