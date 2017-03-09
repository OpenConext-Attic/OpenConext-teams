<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ attribute name="baseUrl" required="true" description="base url for the paging" %>
<%@ attribute name="pager" required="true" description="Pager object"
             type="teams.domain.Pager" %>
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

<c:if test="${fn:length(pager.visiblePages) > 1}">
  <ul class="pagination">
    <%-- First --%>
    <c:if test="${not empty pager.firstPage}">
      <c:url value="${baseUrl}" var="first">
        <c:if test="${not empty query}"><c:param name="teamSearch" value="${query}"/></c:if>
        <c:if test="${not empty display}"><c:param name="teams" value="${display}"/></c:if>
        <c:if test="${not empty team}"><c:param name="team" value="${team.id}"/></c:if>
        <c:if test="${not empty groupId}"><c:param name="groupId" value="${groupId}"/></c:if>
        <c:if test="${not empty externalGroupProvider}"><c:param name="groupProviderId" value="${externalGroupProvider.identifier}"/></c:if>
      </c:url>
      <li><a href="<c:out value="${first}"/>" class="page first"><spring:message code="jsp.general.First"/></a></li>
    </c:if>

    <%-- Previous --%>
    <c:if test="${not empty pager.previousPage}">
      <c:url value="${baseUrl}" var="prev">
        <c:param name="offset" value="${pager.previousPage.offset}"/>
        <c:if test="${not empty query}"><c:param name="teamSearch" value="${query}"/></c:if>
        <c:if test="${not empty display}"><c:param name="teams" value="${display}"/></c:if>
        <c:if test="${not empty team}"><c:param name="team" value="${team.id}"/></c:if>
        <c:if test="${not empty groupId}"><c:param name="groupId" value="${groupId}"/></c:if>
        <c:if test="${not empty externalGroupProvider}"><c:param name="groupProviderId" value="${externalGroupProvider.identifier}"/></c:if>
      </c:url>
      <li><a href="<c:out value="${prev}"/>" class="page prev"><spring:message code="jsp.general.Previous"/></a></li>
    </c:if>

    <%-- Sliding pager --%>
    <c:forEach items="${pager.visiblePages}" var="page" varStatus="loop">
      <li>
        <c:url value="${baseUrl}" var="pageUrl">
          <c:param name="offset" value="${page.offset}"/>
          <c:if test="${not empty query}"><c:param name="teamSearch" value="${query}"/></c:if>
          <c:if test="${not empty display}"><c:param name="teams" value="${display}"/></c:if>
          <c:if test="${not empty team}"><c:param name="team" value="${team.id}"/></c:if>
          <c:if test="${not empty groupId}"><c:param name="groupId" value="${groupId}"/></c:if>
          <c:if test="${not empty externalGroupProvider}"><c:param name="groupProviderId" value="${externalGroupProvider.identifier}"/></c:if>
        </c:url>
        <c:choose>
          <c:when test="${page.currentPage eq true}">
            <span class="page <c:if test="${loop.last}">lastpage</c:if>"><fmt:formatNumber pattern="#"
                                                                                           value="${page.pageNumber}"/></span>
          </c:when>
          <c:otherwise>
            <a href="<c:out value="${pageUrl}"/>"
               class="page <c:if test="${loop.last}">lastpage</c:if>"><fmt:formatNumber pattern="#"
                                                                                        value="${page.pageNumber}"/></a>
          </c:otherwise>
        </c:choose>
      </li>
    </c:forEach>

    <%-- Next page --%>
    <c:if test="${not empty pager.nextPage}">
      <c:url value="${baseUrl}" var="next">
        <c:param name="offset" value="${pager.nextPage.offset}"/>
        <c:if test="${not empty query}"><c:param name="teamSearch" value="${query}"/></c:if>
        <c:if test="${not empty display}"><c:param name="teams" value="${display}"/></c:if>
        <c:if test="${not empty team}"><c:param name="team" value="${team.id}"/></c:if>
        <c:if test="${not empty groupId}"><c:param name="groupId" value="${groupId}"/></c:if>
        <c:if test="${not empty externalGroupProvider}"><c:param name="groupProviderId" value="${externalGroupProvider.identifier}"/></c:if>
      </c:url>
      <li><a href="<c:out value="${next}"/>" class="page next"><spring:message code="jsp.general.Next"/></a></li>
    </c:if>

    <%-- Last page --%>
    <c:if test="${not empty pager.lastPage}">
      <c:url value="${baseUrl}" var="last">
        <c:param name="offset" value="${pager.lastPage.offset}"/>
        <c:if test="${not empty query}"><c:param name="teamSearch" value="${query}"/></c:if>
        <c:if test="${not empty display}"><c:param name="teams" value="${display}"/></c:if>
        <c:if test="${not empty team}"><c:param name="team" value="${team.id}"/></c:if>
        <c:if test="${not empty groupId}"><c:param name="groupId" value="${groupId}"/></c:if>
        <c:if test="${not empty externalGroupProvider}"><c:param name="groupProviderId" value="${externalGroupProvider.identifier}"/></c:if>
      </c:url>
      <li><a href="<c:out value="${last}"/>" class="page last"><spring:message code="jsp.general.Last"/></a></li>
    </c:if>
  </ul>
</c:if>
