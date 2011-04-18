<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<teams:genericpage>
<!-- = Header -->
<div id="Header">
  <ul class="team-actions">
    <c:url value="home.shtml" var="myTeamsUrl"><c:param name="teams" value="my" /><c:param name="view" value="${view}" /></c:url>
    <c:url value="home.shtml" var="allTeamsUrl"><c:param name="teams" value="all" /><c:param name="view" value="${view}" /></c:url>
    <li class="first"><a class="btn-my-teams" href="${myTeamsUrl}"><spring:message code='jsp.home.MyTeams' /></a></li>
    <li class="last"><a class="btn-all-teams" href="${allTeamsUrl}"><spring:message code='jsp.home.AllTeams' /></a></li>
  </ul>
  <br class="clear" />
<!-- / Header -->
</div>
<!-- = Content -->
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
  <div class="center">
    <c:url value="doAcceptInvitation.shtml" var="acceptInvitationUrl"><c:param name="id" value="${invitation.invitationHash}" /><c:param name="view" value="app" /></c:url>
    <c:url value="home.shtml" var="cancelInvitationUrl"><c:param name="view" value="app" /></c:url>
    <p class="accept">
      <a class="button-primary" href="${acceptInvitationUrl}"><spring:message code='jsp.acceptinvitation.Accept' /></a>
      <a class="button-secondary" href="${cancelInvitationUrl}"><spring:message code='jsp.general.Cancel' /></a>
    </p>
  </div>
<!-- / Content -->
</div>
</teams:genericpage>


