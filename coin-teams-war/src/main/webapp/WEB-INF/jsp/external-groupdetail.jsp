<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://teamfn" prefix="teamfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams" %>
<%--
  Copyright 2012 SURFnet bv, The Netherlands

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  --%>

<teams:genericpage>
  <%-- = TeamContainer --%>
<div class="section" id="TeamContainer">
    <%-- = Header --%>
  <div id="Header">
    <div class="jquery-notify-bar">
      <p><spring:message code="jsp.detailteam.ExternalGroupInformationWarning" arguments="${groupProvider.name}"/></p>
    </div>
    <c:url value="/home.shtml" var="backUrl" >
      <c:param name="teams" value="externalGroups" />
      <c:param name="groupProviderId" value="${groupProvider.id}"/>
      <c:param name="view" value="${view}" />
    </c:url>
    <p class="${backClass}"><a href="<c:out value="${backUrl}"/>">&lt; <spring:message code='jsp.detailteam.Back' /></a></p>


    <h1 class="team-title"><c:out value="${group20.title}"/> (<c:out value="${groupProvider.name}"/>)</h1>
    <br class="clear"/>
  </div>
  <div id="Content">
    <c:if test="${not empty group20.description}">
      <p class="description"><c:out value="${group20.description}"/></p>
    </c:if>

    <c:choose>
      <c:when test="${empty groupMembersEntry or fn:length(groupMembersEntry.entry) == 0}">

      </c:when>
      <c:otherwise>
        <div class="pagination-wrapper">
          <teams:paginate baseUrl="/externalgroups/groupdetail.shtml" pager="${pager}"/>
        </div>
        <div class="team-table-wrapper">
          <table class="team-table">
            <thead>
            <tr>
              <th class="name"><spring:message code='jsp.detailteam.Name'/></th>
              <th><spring:message code="jsp.general.Email"/></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${groupMembersEntry.entry}" var="member">
              <tr>
                <td>
                  <c:choose>
                    <c:when test="${not empty member.displayName}">
                      <c:out value="${member.displayName}"/>
                    </c:when>
                    <c:when test="${not empty member.name}">
                      <c:out value="${member.name}"/>
                    </c:when>
                    <c:otherwise>
                      <c:out value="${member.id}"/>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:forEach items="${member.emails}" var="email" end="1">
                    <c:out value="${email.value}"/>
                  </c:forEach>
                </td>
              </tr>
            </c:forEach>
            </tbody>

          </table>

        </div>

      </c:otherwise>
    </c:choose>

  </div>
</div>

</teams:genericpage>