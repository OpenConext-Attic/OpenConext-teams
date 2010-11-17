<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://teamfn" prefix="teamfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<teams:genericpage>
<!-- = TeamContainer -->
<div class="section" id="TeamContainer">
	<!-- = Header -->
	<div id="Header">
		<p class="back"><a href="home.shtml?teams=my">&lt; <spring:message code='jsp.detailteam.Back' /></a></p>
		<p class="team-option">
			<c:url value="doleaveteam.shtml" var="leaveUrl"><c:param name="team" value="${team.id}" /></c:url>
			<a class="button-secondary" id="LeaveTeam" href="<c:out value='${leaveUrl}' />"><spring:message code='jsp.detailteam.Leave' /></a>
		</p>
		<h1 class="team-title"><c:out value="${team.name}" /></h1>
		<p class="add"><a class="button-primary" href="addmember.shtml?team=${team.id}"><spring:message code='jsp.addmember.Title' /></a></p>
		<br class="clear" />
	<!-- / Header -->
	</div>
	<!-- = Content -->
	<div id="Content">
		<p class="description">
			<c:set var="noDescription"><spring:message code='jsp.general.NoDescription' /></c:set>
			<c:out value="${team.description}" default="${noDescription}"/>
		</p>
		<form>
			<input type="hidden" name="teamId" value="${team.id}" />
			<input type="hidden" name="loggedInUser" value="${sessionScope.person}" />
			<table class="team-table">
				<thead>
					<th><spring:message code='jsp.detailteam.Name' /></th>
					<th><spring:message code='jsp.detailteam.Admin' /></th>
					<th><spring:message code='jsp.detailteam.Manager' /></th>
					<th><spring:message code='jsp.detailteam.Member' /></th>
				</thead>
				<tbody>
				<c:if test="${fn:length(team.members) > 0 }">
					<c:forEach items="${team.members}" var="member">
						<c:choose>
							<c:when test="${teamfn:contains(member.roles, manager) && teamfn:contains(member.roles, admin)}"><c:set var="managerRoleStatus" value="checked disabled" /></c:when>
							<c:when test="${teamfn:contains(member.roles, manager) && !teamfn:contains(member.roles, admin)}"><c:set var="managerRoleStatus" value="checked" /></c:when>
							<c:otherwise><c:set var="managerRoleStatus" value="" /></c:otherwise>
						</c:choose>
						<tr>
							<td><c:out value="${member.name}" /></td>
							<td><input id="0_${member.id}" type="checkbox" name="adminRole" value="1" <c:if test="${teamfn:contains(member.roles, admin)}" > checked</c:if> disabled /></td>
							<td><input id="1_${member.id}" type="checkbox" name="managerRole" value="1" <c:out value='${managerRoleStatus}' /> /></td>
							<td>
								<c:choose>
									<c:when test="${member.id eq sessionScope.person}"></c:when>
									<c:otherwise><a href="dodeletemember.shtml?team=${team.id}&member=${member.id}">Remove member from team</a></c:otherwise>
								</c:choose>
							</td>
						</tr>
					</c:forEach>
				</c:if>
				</tbody>
			</table>
		</form>
	<!-- / Content -->
	</div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>