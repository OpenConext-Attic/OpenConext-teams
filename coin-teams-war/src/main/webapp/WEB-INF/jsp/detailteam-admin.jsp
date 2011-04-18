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

    <c:url value="home.shtml" var="backUrl"><c:param name="teams" value="my" /><c:param name="view" value="${view}" /></c:url>
    <p class="back"><a href="${backUrl}">&lt; <spring:message code='jsp.detailteam.Back' /></a></p>
    <div class="team-options-wrapper">
      <c:url value="editteam.shtml" var="editUrl"><c:param name="team" value="${team.id}" /><c:param name="view" value="${view}" /></c:url>
      <c:url value="dodeleteteam.shtml" var="deleteUrl"><c:param name="team" value="${team.id}" /><c:param name="view" value="${view}" /></c:url>
      <c:url value="doleaveteam.shtml" var="leaveUrl"><c:param name="team" value="${team.id}" /><c:param name="view" value="${view}" /></c:url>
      <ul class="team-options">
        <li class="first"><a href="${editUrl}"><spring:message code='jsp.detailteam.Edit' /></a></li>
        <li class="middle"><a id="DeleteTeam" href="${deleteUrl}"><spring:message code='jsp.detailteam.Delete' /></a></li>
        <li class="last"><a id="LeaveTeam" href="${leaveUrl}"><spring:message code='jsp.detailteam.Leave' /></a></li>
      </ul>  
    </div>
    <br class="clear" />
    <h1 class="team-title"><c:out value="${team.name}" /></h1>  
    <p class="add">
      <c:url value="addmember.shtml" var="addmemberUrl"><c:param name="team" value="${team.id}" /><c:param name="view" value="${view}" /></c:url>
      <a class="button-primary" href="${addmemberUrl}"><spring:message code='jsp.addmember.Title' /></a>
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
    <teams:pendingRequests/>
    <form action="">
      <input type="hidden" name="teamId" value="<c:out value='${team.id}' />" />
      <input type="hidden" name="loggedInUser" value="<c:out value='${sessionScope.person}' />" />
      <input type="hidden" name="view" value="<c:out value='${view}' />" />
      <div class="team-table-wrapper">
        <table class="team-table">
          <thead>
            <th></th>
            <th><spring:message code='jsp.detailteam.Name' /></th>
            <th><spring:message code="jsp.general.Email"/> </th>
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
                <td>
                  <c:if test="${member.id ne sessionScope.person}">
                    <c:url var="dodeletemember" value="dodeletemember.shtml">
                      <c:param name="team" value="${team.id}"/>
                      <c:param name="member" value="${member.id}"/>
                      <c:param name="view" value="${view}"/>
                    </c:url>
                    <a href="${dodeletemember}">[X]</a>
                  </c:if>
                </td>
                <td><c:out value="${member.name}" /></td>
                <td><c:out value="${member.email}"/></td>
                <td><input id="0_${member.id}" type="checkbox" name="adminRole" value="" <c:if test="${teamfn:contains(member.roles, admin)}" > checked</c:if> <c:if test="${onlyAdmin eq 1 && member.id eq sessionScope.person}">disabled</c:if> /></td>
                <td><input id="1_${member.id}" type="checkbox" name="managerRole" value="" <c:out value='${managerRoleStatus}' /> /></td>
                <td><input id="2_${member.id}" type="checkbox" name="memberRole" value="" disabled="disabled" checked="checked" /></td>
              </tr>
            </c:forEach>
          </c:if>
          <c:if test="${fn:length(invitations) > 0 }">
            <c:forEach items="${invitations}" var="invite">
              <tr>
                  <c:url var="dodeleteinvite" value="deleteInvitation.shtml">
                    <c:param name="id" value="${invite.invitationHash}"/>
                    <c:param name="view" value="${view}"/>
                  </c:url>
                <td><a href="${dodeleteinvite}">[x]</a></td>
                <td><spring:message code='jsp.detailteam.InvitationPending' /></td>
                <td>${invite.email}</td>
                <td><input type="checkbox" disabled="disabled"/></td>
                <td><input type="checkbox" disabled="disabled"/></td>
                <td><input type="checkbox" disabled="disabled"/></td>
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