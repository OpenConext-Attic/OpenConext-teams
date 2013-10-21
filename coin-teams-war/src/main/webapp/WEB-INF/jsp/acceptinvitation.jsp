<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
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
<c:set var="pageTitle"><spring:message code="jsp.acceptinvitation.Title" /></c:set>
<teams:genericpage pageTitle="${pageTitle}">
<%-- = Content --%>
<div id="Content">
  <h2>Invitation to join <c:out value="${team.name}" /></h2>
  <br class="clear" />
  <table class="invitationDetails">
    <tr class="odd">
      <td class="width200"><spring:message code="jsp.acceptinvitation.DisplayName" /></td>
      <td><c:out value="${sessionScope.person.displayName}" /></td>
    </tr>
    <tr class="even">
      <td><spring:message code="jsp.acceptinvitation.UserID" /></td>
      <td><c:out value="${sessionScope.person.id}" /></td>
    </tr>
    <tr class="odd">
      <td>E-mailaddress</td>
      <td>
        <c:forEach var="email" items="${sessionScope.person.emails}">
          <c:out value="${email.value}" />&nbsp;
        </c:forEach>
      </td>
    </tr>
    <tr class="even">
      <td><spring:message code="jsp.acceptinvitation.HomeOrganization" /></td>
      <td><c:out value="${header.schacHomeOrganization}" /></td>
    </tr>
  </table>
  <br class="clear" />
  <form action="doAcceptInvitation.shtml" id="AcceptInvitationForm">
    <fieldset>
      <input type="hidden" name="view" value="${view}"/>
      <input type="hidden" name="id" value="<c:out value="${invitation.invitationHash}"/>">
      <p class="label-field-wrapper">
        <input id ="TeamConsent" type="checkbox" name="consent" /><label for="TeamConsent" class="consent"><spring:message code='jsp.jointeam.Consent' /></label>
      </p>
    </fieldset>
    <fieldset class="center">
      <p class="submit-wrapper">
        <input class="button-disabled" type="submit" disabled="disabled" name="joinTeam"
               value="<spring:message code='jsp.acceptinvitation.Accept' />" />
        <input class="button" type="submit" name="cancelJoinTeam"
               value="<spring:message code='jsp.general.Cancel' />" />
      </p>
    </fieldset>
  </form>
<%-- / Content --%>
</div>
</teams:genericpage>


