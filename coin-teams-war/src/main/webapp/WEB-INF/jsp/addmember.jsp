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
<c:set var="pageTitle"><spring:message code='jsp.addmember.Title' /></c:set>
<teams:genericpage pageTitle="${pageTitle}">
<%-- = TeamContainer --%>
<div class="section" id="TeamContainer">
  <%-- = Header --%>
  <div id="Header">
    <h1>${pageTitle}</h1>
    <c:url value="/home.shtml" var="closeUrl"><c:param name="teams" value="my" /><c:param name="view" value="${view}" /></c:url>
    <p class="close"><a href="<c:out value="${closeUrl}"/>"><spring:message code='jsp.general.CloseForm' /></a></p>
  <%-- / Header --%>
  </div>
  <%-- = Content --%>
  <div id="Content">
    <div>
      <spring:message code="invite.introduction" htmlEscape="false"/>
    </div>
    <c:url value="/doaddmember.shtml" var="doAddMemberUrl"><c:param name="view" value="${view}" /></c:url>
    <form:form action="${doAddMemberUrl}" commandName="invitationForm" method="post"
            enctype="multipart/form-data">
      <p class="label-field-wrapper">
        <input type="hidden" name="token" value="<c:out value='${tokencheck}'/>"/>
        <input type="hidden" name="view" value="<c:out value='${view}' />" />
        <input type="hidden" name="team" value="<c:out value='${team.id}' />" />
        <form:label path="emails" for="MemberEmail"><spring:message code='jsp.general.Email' /></form:label>
        <c:set var="emailsPlaceholder"><spring:message code='jsp.addmember.Email.placeholder' /></c:set>
        <form:input path="emails" id="MemberEmail" cssClass="multiemail"
                    placeholder="${emailsPlaceholder}" cssErrorClass="error"/>
        <spring:bind path="csvFile">
          <span id="fileUploadBox" class="fileUploadBox">
            <label for="csvFile"><spring:message code="jsp.addmember.CsvEmail"/></label>
            <i></i>
            <input id="csvFile" name="csvFile" type="file" accept="text/csv" onchange="this.focus(); this.blur();"/>
          </span>
        </spring:bind>
        <form:errors path="emails" cssClass="error" element="label"/>
        <form:errors path="csvFile" cssClass="error" element="label"/>
      </p>
      <%--@elvariable id="roles" type="nl.surfnet.coin.teams.domain.Role[]"--%>
      <c:if test="${fn:length(roles) > 1}">
        <p class="label-field-wrapper">
          <form:label path="intendedRole"><spring:message code="jsp.addmember.Role"/></form:label>
          <form:select path="intendedRole" items="${roles}"/>
        </p>
      </c:if>
      <p class="label-field-wrapper">
        <form:label path="message"><spring:message code='jsp.addmember.Message.label' /></form:label>
        <form:textarea path="message" cols="5" rows="4"/>
      </p>
      <p class="submit-wrapper">
        <input class="button-primary" type="submit" name="addMember" value="<spring:message code='jsp.addmember.Submit' />" />
        <input class="button-secondary" type="submit" name="cancelAddMember" value="<spring:message code='jsp.general.Cancel' />" />
      </p>
    </form:form>
    <div class="clear"></div>

  <%-- / Content --%>
  </div>
<%-- / TeamContainer --%>
</div>
</teams:genericpage>