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
		<span class="back"><a href=""><spring:message code='jsp.detailteam.Back' /></a></span>
		<h1>Team One</h1>
		<ul class="team-options">
			<li><a href="jointeam.shtml"><spring:message code='jsp.detailteam.Join' /></a></li>
		</ul>
	<!-- / Header -->
	</div>
	<!-- = Content -->
	<div id="Content">
		<p>A paragraph of text.</p>
	<!-- / Content -->
	</div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>