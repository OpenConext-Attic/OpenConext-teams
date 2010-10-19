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
			<li><a href="leaveteam.shtml?team=1"><spring:message code='jsp.detailteam.Leave' /></a></li>
		</ul>
	<!-- / Header -->
	</div>
	<!-- = Content -->
	<div id="Content">
		<p>A paragraph of text. A paragraph of text. A paragraph of text. A paragraph of text. A paragraph of text.</p>
		<table>
			<thead class="teams-table">
				<td><spring:message code='jsp.detailteam.Name' /></td>
				<td><spring:message code='jsp.detailteam.Admin' /></td>
				<td><spring:message code='jsp.detailteam.Manager' /></td>
				<td><spring:message code='jsp.detailteam.Member' /></td>
			</thead>
			<tbody>
				<tr class="odd">
					<td>Paul van Dijk</td>
					<td>[x]</td>
					<td>[x]</td>
					<td>[x]</td>
				</tr>
				<tr class="even">
					<td>Okke Harsta</td>
					<td>[ ]</td>
					<td>[x]</td>
					<td>[x]</td>
				</tr>
				<tr class="odd">
					<td>Niels van Dijk</td>
					<td>[ ]</td>
					<td>[ ]</td>
					<td>[x]</td>
				</tr>
				<tr class="even">
					<td>Christiaan Hees</td>
					<td colspan="3"><p class="detailteam-invitation"><spring:message code='jsp.detailteam.InvitationPending' /></p></td>
				</tr>
			</tbody>
		</table>
	<!-- / Content -->
	</div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>