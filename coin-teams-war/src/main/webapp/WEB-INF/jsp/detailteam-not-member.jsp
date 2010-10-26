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
		<h1><c:out value="${team.name}" /></h1>
		<ul class="team-options">
			<c:url value="jointeam.shtml" var="joinUrl"><c:param name="team" value="${team.id}" /></c:url>
			<li><a href="<c:out value='${joinUrl}' />"><spring:message code='jsp.detailteam.Join' /></a></li>
		</ul>
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