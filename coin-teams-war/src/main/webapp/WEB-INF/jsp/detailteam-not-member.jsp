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
		<h1>Team One</h1>
	<!-- / Header -->
	</div>
	<!-- = Content -->
	<div id="Content">
		<p>
			<c:set var="noDescription"><spring:message code='jsp.general.NoDescription' /></c:set>
			<c:out value="${team.description}" default="${noDescription}"/>
		</p>
		<p class="more">
			<a class="button-primary" href="jointeam.shtml?team=${team.id}"><spring:message code='jsp.detailteam.Join' /></a>
			<a class="button-secondary" href="home.shtml?teams=my"><spring:message code='jsp.general.Cancel' /></a>			
		</p>
		<br class="clear" />
	<!-- / Content -->
	</div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>