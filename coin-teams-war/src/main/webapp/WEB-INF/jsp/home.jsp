<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<teams:genericpage>
<!-- = Header -->
<div id="Header">
  <ul class="team-actions">
     <c:url value="home.shtml" var="myTeamsUrl"><c:param name="teams" value="my" /></c:url>
     <c:url value="home.shtml" var="allTeamsUrl"><c:param name="teams" value="all" /></c:url>
     <li class="first"><a class="btn-my-teams<c:if test='${display eq "my"}'> selected</c:if>" href="<c:out value='${myTeamsUrl}' />"><spring:message code='jsp.home.MyTeams' /></a></li>
   <li class="last"><a class="btn-all-teams<c:if test='${display eq "all"}'> selected</c:if>" href="<c:out value='${allTeamsUrl}' />"><spring:message code='jsp.home.AllTeams' /></a></li>
  </ul>

  <c:url value="home.shtml" var="searchUrl"><c:param name="teams" value="${display}" /></c:url>
  <form action="<c:out value='${searchUrl}' />" method="post">
  <fieldset class="search-fieldset team-search">
      <c:choose>
        <c:when test="${fn:length(query) == 0}">
          <input class="text search-query" type="text" name="teamSearch" value="<spring:message code='jsp.home.SearchTeam' />"  />
        </c:when>
        <c:otherwise>
          <c:url value="home.shtml" var="viewAllUrl"><c:param name="teams" value="${display}" /></c:url>
          <span class="view-all"><a href="<c:out value='${viewAllUrl}' />"><spring:message code='jsp.home.ViewAll' /></a></span>
          <input class="text search-query" type="text" name="teamSearch" value="<c:out value='${query}' />" />
        </c:otherwise>
      </c:choose>
      <input class="submit-search" id="SubmitTeamSearch" type="submit" value="" />
    </fieldset>
  </form>
  <br class="clear" />
<!-- / Header -->
</div>
<!-- = Content -->
<div id="Content">
    <c:url value="addteam.shtml" var="addTeamUrl"></c:url>
  <p class="add"><a class="button-primary" href="<c:out value='${addTeamUrl}' />"><spring:message code='jsp.home.AddTeam' /></a></p>
  <div class="team-table-wrapper">
    <table class="team-table">
      <thead>
        <tr>
          <th><spring:message code='jsp.home.table.Team' /></th>
          <th><spring:message code='jsp.home.table.Description' /></th>
          <c:if test='${display eq "my"}'>
            <th><spring:message code='jsp.home.table.Role' /></th>
            <th><spring:message code='jsp.home.table.Members' /></th>
          </c:if>
        </tr>
      </thead>
      <tbody>
      <c:choose>
        <c:when test="${fn:length(teams) > 0 }">
          <c:forEach items="${teams}" var="team">
            <tr>
              <c:url value="detailteam.shtml" var="detailUrl"><c:param name="team" value="${team.id}" /></c:url>
              <td><a href="<c:out value='${detailUrl}' />"><c:out value="${team.name}" /></a></td>
              <td><c:out value="${team.description}" /></td>
              <c:if test='${display eq "my"}'>
                <td><c:out value="${team.viewerRole}" /></td>
                <td><c:out value="${fn:length(team.members)}" /></td>
              </c:if>
            </tr>
          </c:forEach>
        </c:when>
        <c:when test="${fn:length(query) > 0 && fn:length(teams) == 0}">
          <tr><td colspan="4" /><spring:message code="jsp.home.NoTeamsFound" /></tr>
        </c:when>
        <c:otherwise>
          <tr><td colspan="4" /><spring:message code="jsp.home.NoTeams" /></tr>
        </c:otherwise>
      </c:choose>
      </tbody>
    </table>
  </div>
<!-- / Content -->
</div>
</teams:genericpage>


