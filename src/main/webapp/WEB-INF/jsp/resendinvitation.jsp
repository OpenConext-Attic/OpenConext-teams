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
<c:set var="pageTitle"><spring:message code='jsp.resendInvite.Title' /></c:set>
<teams:genericpage pageTitle="${pageTitle}">
<%-- = TeamContainer --%>
<div class="section" id="TeamContainer">
  <%-- = Header --%>
  <div id="Header">
    <h1>${pageTitle}</h1>
    <c:url value="/detailteam.shtml" var="closeUrl"><c:param name="team" value="${invitation.teamId}" /><c:param name="view" value="${view}" /></c:url>
    <p class="close"><a href="${closeUrl}"><spring:message code='jsp.general.CloseForm' /></a></p>
  <%-- / Header --%>
  </div>
  <%-- = Content --%>
  <div id="Content">
    <form:form action="doResendInvitation.shtml" commandName="invitation" method="post">
      <p class="label-field-wrapper">
        <input type="hidden" name="token" value="<c:out value='${tokencheck}'/>"/>
        <input type="hidden" name="view" value="<c:out value='${view}' />" />
        <input type="hidden" name="team" value="<c:out value='${invitation.teamId}' />" />
        <form:label path="email"><spring:message code ="jsp.general.Email"/></form:label>
        <form:input path="email" cssErrorClass="error"/>
        <form:errors path="email" cssClass="error" element="label"/>
      </p>
      <p class="label-field-wrapper">
        <form:label path="intendedRole"><spring:message code="jsp.addmember.Role"/></form:label>
        <form:select path="intendedRole" items="${roles}"/>
      </p>
      <p class="label-field-wrapper">
        <label for="messageText"><spring:message code='jsp.addmember.Message.label' /></label>
        <textarea id="messageText" name="messageText" rows="4" cols="5"><c:out value="${messageText}"/></textarea>
      </p>
      <p class="submit-wrapper">
        <input class="button" type="submit" name="addMember" value="<spring:message code='jsp.addmember.Submit' />" />
        <input class="button" type="submit" name="cancelAddMember" value="<spring:message code='jsp.general.Cancel' />" />
      </p>
    </form:form>
    <div class="clear"></div>

  <%-- / Content --%>
  </div>
<%-- / TeamContainer --%>
</div>
</teams:genericpage>
