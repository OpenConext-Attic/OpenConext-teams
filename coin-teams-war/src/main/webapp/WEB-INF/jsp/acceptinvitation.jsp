<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
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
      <input type="hidden" name="view" value="app"/>
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

      <%--<c:url value="doAcceptInvitation.shtml" var="acceptInvitationUrl"><c:param name="id" value="${invitation.invitationHash}" /><c:param name="view" value="app" /></c:url>--%>
      <%--<c:url value="home.shtml" var="cancelInvitationUrl"><c:param name="view" value="app" /></c:url>--%>
      <%--<p class="accept">--%>
        <%----%>
        <%--<a class="button-primary" href="${acceptInvitationUrl}"><spring:message code='jsp.acceptinvitation.Accept' /></a>--%>
        <%--<a class="button-secondary" href="${cancelInvitationUrl}"><spring:message code='jsp.general.Cancel' /></a>--%>
      <%--</p>--%>
    </fieldset>
  </form>
<%-- / Content --%>
</div>
</teams:genericpage>


