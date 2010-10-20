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
		<h1><spring:message code='jsp.jointeam.Title' /></h1>
		<span class="close-form"><a href="home.shtml?teams=my"><spring:message code='jsp.general.CloseForm' /></a></span>
	<!-- / Header -->
	</div>
	<!-- = Content -->
	<div id="Content">
		<form id="JoinTeamForm" action="dojointeam.shtml" method="post">
			<input type="hidden" name="teamId" value="${team.id}" />
			<spring:message code='jsp.general.Message' />
			<textarea name="description" rows="4"><spring:message code='jsp.jointeam.Message' /></textarea>
			<input type="submit" name="joinTeam" value="<spring:message code='jsp.jointeam.Submit' />" />
			<input type="submit" name="cancelJoinTeam" value="<spring:message code='jsp.general.Cancel' />" />
		</form>
	<!-- / Content -->
	</div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>