<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
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
<c:set var="pageTitle"><spring:message code='jsp.jointeam.Title' /></c:set>
<teams:genericpage pageTitle="${pageTitle}">
<%-- = TeamContainer --%>
<div class="section" id="TeamContainer">
  <%-- = Header --%>
  <div id="Header">
    <h1>${pageTitle}</h1>
    <c:url value="home.shtml" var="closeUrl"><c:param name="teams" value="all" /><c:param name="view" value="${view}" /></c:url>
    <p class="close"><a href="<c:out value="${closeUrl}"/>"><spring:message code='jsp.general.CloseForm' /></a></p>
  <%-- / Header --%>
  </div>
  <%-- = Content --%>
  <div id="Content">
    <form:form id="JoinTeamForm" method="post" commandName="joinTeamRequest" action="dojointeam.shtml">
      <p class="label-field-wrapper">
        <input type="hidden" name="team" value="<c:out value='${joinTeamRequest.groupId}' />" />
        <input type="hidden" name="view" value="<c:out value='${view}' />" />
        <form:label path="message"><spring:message code='jsp.general.Message' /></form:label>
        <form:textarea path="message" rows="4" cols="5"/>
      </p>
      <p class="label-field-wrapper">
        <span class="consent-wrapper">&nbsp;</span>
        <input id ="TeamConsent" type="checkbox" name="consent" /><label for="TeamConsent" class="consent"><spring:message code='jsp.jointeam.Consent' /></label>
      </p>
      <p class="submit-wrapper">
        <input class="button-disabled" type="submit" disabled="disabled" name="joinTeam"
               value="<spring:message code='jsp.jointeam.Submit' />" />
        <input class="button-secondary" type="submit" name="cancelJoinTeam"
               value="<spring:message code='jsp.general.Cancel' />" />
      </p>
    </form:form>
    <div class="clear" ></div>
    <%-- / Content --%>
  </div>
<%-- / TeamContainer --%>
</div>
</teams:genericpage>