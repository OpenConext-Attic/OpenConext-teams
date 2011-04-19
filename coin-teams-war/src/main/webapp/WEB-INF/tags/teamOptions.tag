<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<c:choose>
  <c:when test="${role eq noRole}">
    <c:set var="teamsTab">all</c:set>
    <c:set var="backClass">back not-member</c:set>
  </c:when>
  <c:otherwise>
    <c:set var="teamsTab">my</c:set>
    <c:set var="backClass">back</c:set>
  </c:otherwise>
</c:choose>
<c:url value="home.shtml" var="backUrl" ><c:param name="teams" value="${teamsTab}" /><c:param name="view" value="${view}" /></c:url>
<p class="${backClass}"><a href="${backUrl}">&lt; <spring:message code='jsp.detailteam.Back' /></a></p>

<c:url value="editteam.shtml" var="editUrl"><c:param name="team" value="${team.id}" /><c:param name="view" value="${view}" /></c:url>
<c:url value="dodeleteteam.shtml" var="deleteUrl"><c:param name="team" value="${team.id}" /><c:param name="view" value="${view}" /></c:url>
<c:url value="doleaveteam.shtml" var="leaveUrl"><c:param name="team" value="${team.id}" /><c:param name="view" value="${view}" /></c:url>

<c:choose>
  <c:when test="${role eq adminRole}">
    <div class="team-options-wrapper">
      <ul class="team-options">
        <li class="first"><a href="${editUrl}"><spring:message code='jsp.detailteam.Edit' /></a></li>
        <c:choose>
          <c:when test="${onlyAdmin eq true}">
            <c:set var="deleteClass">last</c:set>
            <c:set var="leaveClass">last hide</c:set>
          </c:when>
          <c:otherwise>
            <c:set var="deleteClass">middle</c:set>
            <c:set var="leaveClass">last</c:set>
          </c:otherwise>
        </c:choose>
        <c:set var="deleteclass"><c:choose><c:when test="${onlyAdmin eq true}">last</c:when><c:otherwise>middle</c:otherwise></c:choose></c:set>
        <li class="${deleteclass}"><a id="DeleteTeam" href="${deleteUrl}"><spring:message code='jsp.detailteam.Delete' /></a></li>
        <li class="${leaveClass}"><a id="LeaveTeam" href="${leaveUrl}"><spring:message code='jsp.detailteam.Leave' /></a></li>
      </ul>
    </div>
  </c:when>
  <c:when test="${role eq managerRole or role eq memberRole}">
    <p class="team-option">
      <a class="button-secondary" id="LeaveTeam" href="${leaveUrl}"><spring:message code='jsp.detailteam.Leave' /></a>
    </p>
  </c:when>
</c:choose>

