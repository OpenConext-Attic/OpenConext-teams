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
		<span class="back"><a href=""><spring:message code='jsp.detailteam.Back' /></a></span>
		<h1><c:out value="${team.name}" /></h1>
		<ul class="team-options">
			<li><a id="LeaveTeam" href="doleaveteam.shtml?team=${team.id}"><spring:message code='jsp.detailteam.Leave' /></a></li>		</ul>
	<!-- / Header -->
	</div>
	<!-- = Content -->
	<div id="Content">
		<p><c:out value="${team.description}" default="<spring:message code='jsp.general.NoDescription' />"/></p>
		<form>
			<table>
				<thead class="teams-table">
					<td><spring:message code='jsp.detailteam.Name' /></td>
					<td><spring:message code='jsp.detailteam.Admin' /></td>
					<td><spring:message code='jsp.detailteam.Manager' /></td>
					<td><spring:message code='jsp.detailteam.Member' /></td>
				</thead>
				<tbody>
				<c:if test="${fn:length(team.members) > 0 }">
					<c:forEach items="${team.members}" var="member">
						<tr>
							<td><c:out value="${member.name}" /></td>
							<td><input id="AdminRole" type="checkbox" name="adminRole" value="" <c:if test="${teamfn:contains(member.roles, admin)}" > checked</c:if> disabled /></td>
							<td><input id="ManagerRole" type="checkbox" name="managerRole" value="" <c:if test="${teamfn:contains(member.roles, manager)}" > checked</c:if> disabled /></td>
							<td>X</td>
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