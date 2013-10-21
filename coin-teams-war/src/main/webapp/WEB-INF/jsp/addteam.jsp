<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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
<c:set var="pageTitle"><spring:message code='jsp.addteam.Title' /></c:set>
<teams:genericpage pageTitle="${pageTitle}">
<%-- = TeamContainer --%>
<div class="section" id="TeamContainer">
  <%-- = Header --%>
  <div id="Header">
    <h1>${pageTitle}</h1>
    <c:url value="/home.shtml" var="closeUrl"><c:param name="teams" value="my" /><c:param name="view" value="${view}" /></c:url>
    <p class="close"><a href="${closeUrl}"><spring:message code='jsp.general.CloseForm' /></a></p>
  <%-- / Header --%>
  </div>
  <%-- = Content --%>
  <div id="Content">
    <c:url value="/doaddteam.shtml" var="doAddTeamUrl"><c:param name="view" value="${view}" /></c:url>
    <form:form id="AddTeamForm" action="${doAddTeamUrl}" method="post" commandName="team">
      <input type="hidden" name="token" value="<c:out value='${tokencheck}'/>"/>
      <input type="hidden" name="view" value="<c:out value='${view}' />" />
      <p class="label-field-wrapper">
        <label for="TeamName"><spring:message code='jsp.general.TeamName' /></label>
        <form:input path="name" id="TeamName" cssClass="required" cssErrorClass="error" required="required"/>
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
        <form:textarea path="description" id="TeamDescription" cssClass="withinfo"/>
        <span class="textareainfo"><spring:message code="jsp.addteam.description.info"/></span>
      </p>
      <c:if test="${hasMultipleStems}">
        <p class="label-field-wrapper">
          <label for="TeamSource"><spring:message code='jsp.general.Source' /></label>
          <form:select path="stem" id="TeamSource">
            <form:options items="${stems}" itemValue="id" itemLabel="name"/>
          </form:select>
        </p>
      </c:if>
      <p class="label-field-wrapper">
        <span class="consent-wrapper">&nbsp;</span>
        <form:checkbox id="TeamViewability" path="viewable" />
        <label class="consent" for="TeamViewability"><spring:message code='jsp.general.TeamViewability' /></label>
      </p>
      <p class="label-field-wrapper">
        <span class="label"><spring:message code="jsp.addteam.admin1"/></span>
        <span class="input"><c:out value="${sessionScope.person.displayName} "/><spring:message code="jsp.addteam.admin1.you"/></span>
      </p>
      <%-- admin2 not managed through Team object --%>
      <p class="label-field-wrapper">
        <label for="admin2"><spring:message code="jsp.addteam.admin2"/></label>
        <spring:message code="jsp.addteam.admin2.placeholder" var="admin2Placeholder"/>
        <input type="email" id="admin2" name="admin2" placeholder="${admin2Placeholder}" value="<c:out value="${admin2}"/>" />
        <span class="inputinfo"><spring:message code="jsp.addteam.admin2.info"/></span>
      </p>

      <p class="label-field-wrapper" id="admin2messagecontainer">
        <label for="admin2message"><spring:message code='jsp.addmember.Message.label'/></label>
        <textarea id="admin2message" name="admin2message" cols="5" rows="4"><c:out value="${admin2message}"/></textarea>
      </p>
      <p class="label-field-wrapper">
        <span class="consent-wrapper">&nbsp;</span>
        <input id="TeamConsent" name="consent" type="checkbox"/><label class="consent" for="TeamConsent"><spring:message code='jsp.addteam.Consent' /></label>
      </p>
      <p class="submit-wrapper">
        <input class="button-disabled" type="submit" name="createTeam" value="<spring:message code='jsp.addteam.Submit' />" disabled="disabled" />
        <input class="button" type="submit" name="cancelCreateTeam" value="<spring:message code='jsp.general.Cancel' />" />
      </p>
    </form:form>
    <div class="clear"></div>
    <%-- / Content --%>
  </div>
<%-- / TeamContainer --%>
</div>
</teams:genericpage>