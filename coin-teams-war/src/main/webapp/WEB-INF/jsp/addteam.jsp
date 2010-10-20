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
		<h1><spring:message code='jsp.addteam.Title' /></h1>
		<span class="close-form"><a href="home.shtml?teams=my"><spring:message code='jsp.general.CloseForm' /></a></span>
	<!-- / Header -->
	</div>
	<!-- = Content -->
	<div id="Content">
		<form id="AddTeamForm" action="doaddteam.shtml" method="post">
			<label for="TeamName"><spring:message code='jsp.general.TeamName' /></label>
			<input id="TeamName" type="text" name="team" />
			<spring:message code='jsp.general.Description' />
			<textarea name="description" rows="4"></textarea>
			<input type="submit" name="createTeam" value="<spring:message code='jsp.addteam.Submit' />" />
			<input type="submit" name="cancelCreateTeam" value="<spring:message code='jsp.general.Cancel' />" />
			<input id="TeamViewability" type="checkbox" name="viewabilityStatus" value="1" />
			<label for="TeamViewability"><spring:message code='jsp.general.TeamViewability' /></label>
		</form>
	<!-- / Content -->
	</div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>