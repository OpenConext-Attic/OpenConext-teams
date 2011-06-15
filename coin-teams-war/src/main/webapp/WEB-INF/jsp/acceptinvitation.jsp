<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
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
<%-- = Content --%>
<div id="Content">
  <p><h1><spring:message code="jsp.acceptinvitation.Title" /></h1></p>
  <p><spring:message code="jsp.acceptinvitation.Explanation" /></p>
  <br class="clear" />
  <div class="column-container">
    <div class="column first-column">
      <h3><spring:message code="jsp.acceptinvitation.PersonDetails" /></h3>
      <dl>
        <dt><spring:message code="jsp.acceptinvitation.UserName" /></dt>
        <dd><c:out value="${sessionScope.person.displayName}" /></dd>
        <dt><spring:message code="jsp.acceptinvitation.UserID" /></dt>
        <dd><c:out value="${sessionScope.person.id}" /></dd>
        <dt><spring:message code="jsp.acceptinvitation.HomeOrganization" /></dt>
        <dd class="last"><c:out value="${header.schacHomeOrganization}" /></dd>
      </dl>
    </div>
    <div class="column second-column">
      <h3><spring:message code="jsp.acceptinvitation.InvitationDetails" /></h3>
      <dl>
        <dt><spring:message code="jsp.acceptinvitation.InvitedFor" /></dt>
        <dd><c:out value="${team.name}" /></dd>
        <dt><spring:message code="jsp.acceptinvitation.CreatedOn" /></dt>
        <dd class="last"><fmt:formatDate value="${date}" pattern="dd-MM-yyyy"/></dd>
      </dl>
    </div>
  </div>
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
        <input class="button-secondary" type="submit" name="cancelJoinTeam"
               value="<spring:message code='jsp.general.Cancel' />" />
      </p>
    </fieldset>
  </form>
<%-- / Content --%>
</div>
</teams:genericpage>


