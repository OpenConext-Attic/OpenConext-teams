<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<teams:genericpage>
<!-- = Header -->
<div id="Header">
	<ul class="team-actions">
		<li class="first"><a class="btn-my-teams<c:if test='${display eq "my"}'> selected</c:if>" href="home.shtml?teams=my"><spring:message code='jsp.home.MyTeams' /></a></li>
		<li class="last"><a class="btn-all-teams<c:if test='${display eq "all"}'> selected</c:if>" href="home.shtml?teams=all"><spring:message code='jsp.home.AllTeams' /></a></li>
	</ul>
	<form action="home.shtml?teams=${display}" method="post">
		<fieldset class="search-fieldset team-search">
			<c:choose>
				<c:when test="${fn:length(query) == 0}">
					<input class="text search-query" type="text" name="teamSearch" value="<spring:message code='jsp.home.SearchTeam' />"  />
				</c:when>
				<c:otherwise>
					<span class="view-all"><a href="home.shtml?teams=${display}"><spring:message code='jsp.home.ViewAll' /></a></span>
					<input class="text search-query" type="text" name="teamSearch" value="<c:out value='${query}' />" />
				</c:otherwise>
			</c:choose>
			<input class="submit-search" id="SubmitTeamSearch" type="submit" />
		</fieldset>
	</form>
	<br class="clear" />
<!-- / Header -->
</div>
<!-- = Content -->
<div id="Content">
	<p class="add"><a class="button-primary" href="addteam.shtml"><spring:message code='jsp.home.AddTeam' /></a></p>
	<div class="team-table-wrapper">
		<table class="team-table">
			<thead>
				<tr>
					<th><spring:message code='jsp.home.table.Team' /></th>
					<th><spring:message code='jsp.home.table.Description' /></th>
					<th><spring:message code='jsp.home.table.Role' /></th>
					<th>Members</th>
				</tr>
			</thead>
			<tbody>
			<c:choose>
			<c:when test="${fn:length(teams) > 0 }">
				<c:forEach items="${teams}" var="team">
					<tr>
						<td><a href="detailteam.shtml?team=${team.id}"><c:out value="${team.name}" /></a></td>
						<td><c:out value="${team.description}" /></td>
						<td><c:out value="${team.viewerRole}" /></td>
						<td><c:out value="${fn:length(team.members)}" /></td>
					</tr>
				</c:forEach>
			</c:when>
			<c:otherwise>
				<tr><td colspan="4" /><spring:message code="jsp.home.NoTeamsFound" /></tr>
			</c:otherwise>
			</c:choose>
			</tbody>
		</table>
	</div>
<!-- / Content -->
</div>
</teams:genericpage>