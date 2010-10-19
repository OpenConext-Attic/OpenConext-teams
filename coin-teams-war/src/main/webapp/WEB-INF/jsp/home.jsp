<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<teams:genericpage>
<!-- = Header -->
<div id="Header">
	<ul class="teams-actions">
		<li><a class="btn-my-teams selected" href="home.shtml?teams=my"><spring:message code='jsp.home.MyTeams' /></a></li>
		<li><a class="btn-all-teams" href="home.shtml?teams=all"><spring:message code='jsp.home.AllTeams' /></a></li>
	</ul>
<!-- / Header -->
</div>
<!-- = Content -->
<div id="Content">
	<form>
		<span class="team-search"><input type="text" name="TeamSearch" value="<spring:message code='jsp.home.SearchTeam' />" /></span>
	</form>
	<span class="add-team"><a href="addteam.shtml"></a><spring:message code='jsp.home.AddTeam' /></span>
	<table>
		<thead class="teams-table">
			<td><spring:message code='jsp.home.table.Team' /></td>
			<td><spring:message code='jsp.home.table.Description' /></td>
			<td><spring:message code='jsp.home.table.Role' /></td>
			<td></td>
		</thead>
		<tbody>
			<tr class="odd">
				<td>Coin</td>
				<td>A team description</td>
				<td>Admin</td>
				<td>10</td>
			</tr>
			<tr class="even">
				<td>SURFnet</td>
				<td>A team description</td>
				<td>Manager</td>
				<td>3</td>
			</tr>
			<tr class="odd">
				<td>SURF</td>
				<td>A team description</td>
				<td>Member</td>
				<td>100</td>
			</tr>
		</tbody>
	</table>
<!-- / Content -->
</div>
</teams:genericpage>