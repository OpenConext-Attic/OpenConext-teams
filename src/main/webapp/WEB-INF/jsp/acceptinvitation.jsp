<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams" %>
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
<c:set var="pageTitle"><spring:message code="jsp.acceptinvitation.Title"/></c:set>
<teams:genericpage pageTitle="${pageTitle}">
  <%-- = Content --%>
  <div id="Content">
    <h2>Invitation to join <c:out value="${team.name}"/></h2>
    <br class="clear"/>
    <table class="invitationDetails">
      <tr class="odd">
        <td class="width200"><spring:message code="jsp.acceptinvitation.DisplayName"/></td>
        <td><c:out value="${sessionScope.person.displayName}"/></td>
      </tr>
      <tr class="even">
        <td><spring:message code="jsp.acceptinvitation.UserID"/></td>
        <td><c:out value="${sessionScope.person.id}"/></td>
      </tr>
      <tr class="odd">
        <td><spring:message code="jsp.acceptinvitation.EmailAddress"/></td>
        <td>
            <c:out value="${sessionScope.person.email}"/>&nbsp;
        </td>
      </tr>
    </table>

    <c:if test="${groupzyEnabled}">
      <br>

      <h2>The following Service Providers will have access to my personal information</h2>
      <table>
        <tbody>
        <c:forEach var="serviceProvider" items="${serviceProviders}" varStatus="index">
          <c:choose>
            <c:when test="${index.count % 2 == 0}">
              <c:set var="clazzName" value="even"/>
            </c:when>
            <c:otherwise>
              <c:set var="clazzName" value="odd"/>
            </c:otherwise>
          </c:choose>

          <tr class="<c:out value="${clazzName}"/>">
            <td><c:out value="${serviceProvider.displayNameEn}"/></td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </c:if>
    <br class="clear"/>

    <form action="doAcceptInvitation.shtml" id="AcceptInvitationForm">
      <fieldset>
        <input type="hidden" name="id" value="<c:out value="${invitation.invitationHash}"/>">

        <p class="label-field-wrapper">
          <input id="TeamConsent" type="checkbox" name="consent"/><label for="TeamConsent"
                                                                         class="consent"><spring:message
          code='jsp.jointeam.Consent'/></label>
        </p>
      </fieldset>
      <fieldset class="center">
        <p class="submit-wrapper">
          <input class="button-disabled" type="submit" disabled="disabled" name="joinTeam"
                 value="<spring:message code='jsp.acceptinvitation.Accept' />"/>
          <input class="button" type="submit" name="cancelJoinTeam"
                 value="<spring:message code='jsp.general.Cancel' />"/>
        </p>
      </fieldset>
    </form>
      <%-- / Content --%>
  </div>
</teams:genericpage>


