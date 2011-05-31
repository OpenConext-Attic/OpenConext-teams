<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<%--
  ~ Copyright 2011 SURFnet bv, The Netherlands
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<teams:genericpage>
<!-- = Header -->
<div id="Header">
  <ul class="team-actions">
    <c:url value="home.shtml" var="myTeamsUrl"><c:param name="teams" value="my" /><c:param name="view" value="${view}" /></c:url>
    <c:url value="home.shtml" var="allTeamsUrl"><c:param name="teams" value="all" /><c:param name="view" value="${view}" /></c:url>
    <c:url value="myinvitations.shtml" var="myInvitationsUrl"><c:param name="view" value="${view}"/></c:url>
    <li class="first"><a class="btn-my-teams<c:if test='${display eq "my"}'> selected</c:if>" href="${myTeamsUrl}"><spring:message code='jsp.home.MyTeams' /></a></li>

    <c:choose>
      <c:when test="${myinvitations eq true}">
        <li class="middle"><a class="btn-all-teams<c:if test='${display eq "all"}'> selected</c:if>" href="${allTeamsUrl}"><spring:message code='jsp.home.AllTeams' /></a></li>
        <li class="last"><a href="${myInvitationsUrl}"><spring:message code="jsp.home.MyInvitations"/></a></li>
      </c:when>
      <c:otherwise>
        <li class="last"><a class="btn-all-teams<c:if test='${display eq "all"}'> selected</c:if>" href="${allTeamsUrl}"><spring:message code='jsp.home.AllTeams' /></a></li>
      </c:otherwise>
    </c:choose>
  </ul>

  <c:url value="home.shtml" var="searchUrl"><c:param name="teams" value="${display}" /><c:param name="view" value="${view}" /></c:url>
  <form action="<c:out value='${searchUrl}' />" method="post">
  <fieldset class="search-fieldset team-search">
      <c:choose>
        <c:when test="${fn:length(query) == 0}">
          <input class="text search-query" type="text" name="teamSearch" value="<spring:message code='jsp.home.SearchTeam' />"  />
        </c:when>
        <c:otherwise>
          <c:url value="home.shtml" var="viewAllUrl"><c:param name="teams" value="${display}" /><c:param name="view" value="${view}" /></c:url>
          <span class="view-all"><a href="${viewAllUrl}"><spring:message code='jsp.home.ViewAll' /></a></span>
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
  <c:if test='${sessionScope.userStatus ne "guest"}'>
    <c:url value="addteam.shtml" var="addTeamUrl"><c:param name="view" value="${view}" /></c:url>
    <p class="add"><a class="button-primary" href="${addTeamUrl}"><spring:message code='jsp.home.AddTeam' /></a></p>
  </c:if>
  <teams:paginate baseUrl="home.shtml" pager="${pager}"/>
  <div class="team-table-wrapper">
    <table class="team-table">
      <thead>
        <tr>
          <th><spring:message code='jsp.home.table.Team' /></th>
          <th><spring:message code='jsp.home.table.Description' /></th>
          <c:if test='${display eq "my"}'>
            <th><spring:message code='jsp.home.table.Role' /></th>
          </c:if>
          <th><spring:message code='jsp.home.table.Members' /></th>
        </tr>
      </thead>
      <tbody>
      <c:choose>
        <c:when test="${fn:length(teams) > 0 }">
          <c:forEach items="${teams}" var="team">
            <tr>
              <c:url value="detailteam.shtml" var="detailUrl"><c:param name="team" value="${team.id}" /><c:param name="view" value="${view}" /></c:url>
              <td><a href="${detailUrl}"><c:out value="${team.name}" /></a></td>
              <td><c:out value="${team.description}" /></td>
              <c:if test='${display eq "my"}'>
                <td><c:out value="${team.viewerRole}" /></td>
              </c:if>
              <td><c:out value="${team.numberOfMembers}" /></td>
            </tr>
          </c:forEach>
        </c:when>
        <c:when test="${fn:length(query) > 0 && fn:length(teams) == 0}">
          <tr><td colspan="4"><spring:message code="jsp.home.NoTeamsFound" /></td></tr>
        </c:when>
        <c:otherwise>
          <tr><td colspan="4"><spring:message code="jsp.home.NoTeams" /></td></tr>
        </c:otherwise>
      </c:choose>
      </tbody>
    </table>
  </div>
<!-- / Content -->
</div>
</teams:genericpage>


