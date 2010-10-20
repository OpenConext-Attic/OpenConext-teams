<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<teams:genericpage>
<!-- = Header -->
<div id="Header">
	<ul class="teams-actions">
		<li><a class="btn-my-teams<c:if test='${display eq "my"}'> selected</c:if>" href="home.shtml?teams=my"><spring:message code='jsp.home.MyTeams' /></a></li>
		<li><a class="btn-all-teams<c:if test='${display eq "all"}'> selected</c:if>" href="home.shtml?teams=all"><spring:message code='jsp.home.AllTeams' /></a></li>
	</ul>
<!-- / Header -->
</div>
<!-- = Content -->
<div id="Content">
	<form action="home.shtml?teams=${display}" method="post">
		<span class="team-search">
			<c:choose>
				<c:when test="${!query}">
					<input type="text" name="teamSearch" value="<spring:message code='jsp.home.SearchTeam' />"  />
				</c:when>
				<c:otherwise>
					<input type="text" name="teamSearch" value="<c:out value='${query}' />" />
				</c:otherwise>
			</c:choose>
			<input id="SubmitTeamSearch" type="submit" />
		</span>
	</form>
	<span class="add-team"><a href="addteam.shtml"><spring:message code='jsp.home.AddTeam' /></a></span>
	<table>
		<thead class="teams-table">
			<td><spring:message code='jsp.home.table.Team' /></td>
			<td><spring:message code='jsp.home.table.Description' /></td>
			<td><spring:message code='jsp.home.table.Role' /></td>
			<td></td>
		</thead>
		<tbody>
		<c:if test="${fn:length(teams) > 0 }">
			<c:forEach items="${teams}" var="team">
				<tr>
					<td><a href="detailteam.shtml?team=${team.id}"><c:out value="${team.name}" /></a></td>
					<td><c:out value="${team.description}" /></td>
					<td><c:out value="${team.viewerRole}" /></td>
					<td><c:out value="${fn:length(team.members)}" /></td>
				</tr>
			</c:forEach>
		</c:if>
		</tbody>
	</table>
<!-- / Content -->
</div>
</teams:genericpage>