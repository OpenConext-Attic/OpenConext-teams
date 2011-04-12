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
    <c:if test="${fn:length(message) > 0}"><div id="__notifyBar" class="hide"><spring:message code='${message}' /></div></c:if>

    <c:url value="home.shtml" var="backUrl"><c:param name="teams" value="my" /></c:url>
    <p class="back"><a href="<c:out value='${backUrl}' />">&lt; <spring:message code='jsp.detailteam.Back' /></a></p>
    <div class="team-options-wrapper">
      <c:url value="editteam.shtml" var="editUrl"><c:param name="team" value="${team.id}" /></c:url>
      <c:url value="dodeleteteam.shtml" var="deleteUrl"><c:param name="team" value="${team.id}" /></c:url>
      <c:url value="doleaveteam.shtml" var="leaveUrl"><c:param name="team" value="${team.id}" /></c:url>
      <ul class="team-options">
        <li class="first"><a href="<c:out value='${editUrl}' />"><spring:message code='jsp.detailteam.Edit' /></a></li>
        <li class="middle"><a id="DeleteTeam" href="<c:out value='${deleteUrl}' />"><spring:message code='jsp.detailteam.Delete' /></a></li>
        <li class="last"><a id="LeaveTeam" href="<c:out value='${leaveUrl}' />"><spring:message code='jsp.detailteam.Leave' /></a></li>
      </ul>  
    </div>
    <br class="clear" />
    <h1 class="team-title"><c:out value="${team.name}" /></h1>  
    <p class="add">
      <c:url value="addmember.shtml" var="addmemberUrl"><c:param name="team" value="${team.id}" /></c:url>
      <a class="button-primary" href="<c:out value='${addmemberUrl}' />"><spring:message code='jsp.addmember.Title' /></a>
    </p>
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
      <div class="team-table-wrapper">
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
                <c:when test="${teamfn:contains(member.roles, manager) && (member.id eq sessionScope.person)}"><c:set var="managerRoleStatus" value="checked disabled" /></c:when>
                <c:when test="${teamfn:contains(member.roles, manager) && teamfn:contains(member.roles, admin)}"><c:set var="managerRoleStatus" value="checked disabled" /></c:when>
                <c:when test="${teamfn:contains(member.roles, manager) && !teamfn:contains(member.roles, admin)}"><c:set var="managerRoleStatus" value="checked" /></c:when>
                <c:otherwise><c:set var="managerRoleStatus" value="" /></c:otherwise>
              </c:choose>          
              <tr>
                <td><c:out value="${member.name}" /></td>
                <td><input id="0_${member.id}" type="checkbox" name="adminRole" value="" <c:if test="${teamfn:contains(member.roles, admin)}" > checked</c:if> <c:if test="${onlyAdmin eq 1 && member.id eq sessionScope.person}">disabled</c:if> /></td>
                <td><input id="1_${member.id}" type="checkbox" name="managerRole" value="" <c:out value='${managerRoleStatus}' /> /></td>
                <td>
                  <c:choose>
                    <c:when test="${member.id eq sessionScope.person}"></c:when>
                    <c:otherwise><a href="dodeletemember.shtml?team=${team.id}&member=${member.id}"><spring:message code="jsp.detailteam.RemoveMemberFromTeam"/></a></c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
          </c:if>
          <c:if test="${fn:length(invitations) > 0 }">
            <c:forEach items="${invitations}" var="invite">
              <tr>
                <td>${invite.email}</td>
                <td colspan="3"><spring:message code='jsp.detailteam.InvitationPending' /></td>
              </tr>
            </c:forEach>
          </c:if>
          </tbody>
        </table>
      </div>
    </form>
  <!-- / Content -->
  </div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>