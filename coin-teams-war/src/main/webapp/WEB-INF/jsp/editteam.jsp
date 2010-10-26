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
		<span class="close-form"><a href="detailteam.shtml?team=${team.id}"><spring:message code='jsp.general.CloseForm' /></a></span>
	<!-- / Header -->
	</div>
	<!-- = Content -->
	<div id="Content">
		<form id="EditTeamForm" action="doeditteam.shtml" method="post">
			<input type="hidden" name="teamId" value="${team.id}" />
			<label for="TeamName"><spring:message code='jsp.general.TeamName' /></label>
			<input id="TeamName" type="text" name="team" value="<c:out value="${team.name}" />" class="required" />
			<spring:message code='jsp.general.Description' />
			<textarea name="description" rows="4"><c:out value="${team.description}" /></textarea>
			<input type="submit" name="editTeam" value="<spring:message code='jsp.editteam.Submit' />" />
			<input type="submit" name="cancelEditTeam" value="<spring:message code='jsp.general.Cancel' />" />
			<input id="TeamViewability" type="checkbox" name="viewabilityStatus" value="1"<c:if test="${team.viewable eq false}"> checked</c:if> />
			<label for="TeamViewability"><spring:message code='jsp.general.TeamViewability' /></label>
		</form>
	<!-- / Content -->
	</div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>