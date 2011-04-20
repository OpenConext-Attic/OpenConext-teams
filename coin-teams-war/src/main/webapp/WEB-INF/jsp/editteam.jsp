<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<teams:genericpage>
<!-- = TeamContainer -->
<div class="section" id="TeamContainer">
  <!-- = Header -->
  <div id="Header">
    <h1><spring:message code='jsp.editteam.Title' /></h1>
    <c:url value="home.shtml" var="closeUrl"><c:param name="teams" value="my" /><c:param name="view" value="${view}" /></c:url>
    <p class="close"><a href="${closeUrl}"><spring:message code='jsp.general.CloseForm' /></a></p>
  <!-- / Header -->
  </div>
  <!-- = Content -->
  <div id="Content">
    <c:url value="doeditteam.shtml" var="doEditTeamUrl"><c:param name="view" value="${view}" /></c:url>
    <form id="EditTeamForm" action="<c:out value='${doEditTeamUrl}' />" method="post">
      <p class="label-field-wrapper">
        <input type="hidden" name="teamId" value="<c:out value='${team.id}' />" />
        <input type="hidden" name="view" value="<c:out value='${view}' />" />
        <label for="TeamName"><spring:message code='jsp.general.TeamName' /></label>
        <input id="TeamName" type="text" name="team" value="<c:out value="${team.name}" />" readonly="readonly" class="required" />
      </p>
      <p class="label-field-wrapper">
        <label for="TeamDescription"><spring:message code='jsp.general.Description' /></label>
        <input id="TeamDescription" name="description" type="text" value="<c:out value="${team.description}" />">
      </p>
      <p class="submit-wrapper">
        <input class="button-primary" type="submit" name="editTeam" value="<spring:message code='jsp.editteam.Submit' />" />
        <input class="button-secondary" type="submit" name="cancelEditTeam" value="<spring:message code='jsp.general.Cancel' />" />
        <input id="TeamViewability" type="checkbox" name="viewabilityStatus" value="1"<c:if test="${team.viewable eq false}"> checked</c:if> />
        <label for="TeamViewability"><spring:message code='jsp.general.TeamViewability' /></label>
      </p>
      <br class="clear" />
    </form>
  <!-- / Content -->
  </div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>