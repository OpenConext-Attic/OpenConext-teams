<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ attribute name="baseUrl" required="true" description="base url for the paging" %>

<%--
  ~ Copyright 2011 SURFnet bv
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

<c:if test="${resultset > pagesize}">
  <ul class="pagination">
    <c:if test="${offset >= pagesize}">
      <c:url value="${baseUrl}" var="first">
        <c:param name="view" value="${view}"/>
        <c:if test="${not empty query}"><c:param name="teamSearch" value="${query}"/></c:if>
        <c:if test="${not empty display}"><c:param name="teams" value="${display}"/></c:if>
        <c:if test="${not empty team}"><c:param name="team" value="${team.id}"/></c:if>
      </c:url>
      <li><a href="<c:out value="${first}"/>" class="page first"><spring:message code="jsp.general.First"/></a></li>
      <c:url value="${baseUrl}" var="prev">
        <c:param name="view" value="${view}"/>
        <c:param name="offset" value="${offset - pagesize}"/>
        <c:if test="${not empty query}"><c:param name="teamSearch" value="${query}"/></c:if>
        <c:if test="${not empty display}"><c:param name="teams" value="${display}"/></c:if>
        <c:if test="${not empty team}"><c:param name="team" value="${team.id}"/></c:if>
      </c:url>
      <li><a href="<c:out value="${prev}"/>" class="page prev"><spring:message code="jsp.general.Previous" /></a></li>
    </c:if>

    <c:set var="begin">
      <c:choose>
        <%-- Less than 5 pages in total --%>
        <c:when test="${(resultset / pagesize) < 5}">0</c:when>
        <%-- First 5 pages --%>
        <c:when test="${(offset / pagesize ) < 5}">0</c:when>
        <%-- Last 5 pages --%>
        <c:when test="${((resultset - offset) / pagesize) < 4}">
          <fmt:formatNumber pattern="#" value="${((resultset) - (4 * pagesize)) / pagesize}" var="tail"/>
          <%-- formatnumber does nasty rounding --%>
          <c:if test="${(resultset % pagesize) eq 0 or (resultset % pagesize gt 5)}"><c:set var="tail" value="${tail - 1}"/></c:if>
          ${tail * pagesize}
        </c:when>
        <%-- Somewhere in the middle --%>
        <c:otherwise><fmt:formatNumber pattern="#" value="${(offset) - (2 * pagesize)}"/></c:otherwise>
      </c:choose>
    </c:set>
    <c:set var="end">
      <c:choose>
        <c:when test="${resultset eq 0}">0</c:when>
        <%-- till the end --%>
        <c:when test="${(begin + (4 * pagesize)) >= resultset}">${resultset - 1}</c:when>
        <%-- Next 4 pages --%>
        <c:otherwise>${begin + (4 * pagesize)}</c:otherwise>
      </c:choose>
    </c:set>

      <%--${begin} - ${end} of ${resultset}--%>
      <%--${resultset % pagesize}--%>

    <c:forEach begin="${begin}" end="${end}" step="${pagesize}" var="localOffset" varStatus="loop">
      <li>
        <c:url value="${baseUrl}" var="pageUrl">
          <c:param name="view" value="${view}"/>
          <c:param name="offset" value="${localOffset}"/>
          <c:if test="${not empty query}"><c:param name="teamSearch" value="${query}"/></c:if>
          <c:if test="${not empty display}"><c:param name="teams" value="${display}"/></c:if>
          <c:if test="${not empty team}"><c:param name="team" value="${team.id}"/></c:if>
        </c:url>
        <c:choose>
          <c:when test="${localOffset eq offset}">
            <span class="page <c:if test="${loop.last}">lastpage</c:if>"><fmt:formatNumber pattern="#" value="${(localOffset / pagesize) + 1}"/></span>
          </c:when>
          <c:otherwise>
            <a href="<c:out value="${pageUrl}"/>" class="page <c:if test="${loop.last}">lastpage</c:if>"><fmt:formatNumber pattern="#" value="${(localOffset / pagesize) + 1}"/></a>
          </c:otherwise>
        </c:choose>
      </li>
    </c:forEach>
    <c:if test="${offset + pagesize < resultset}">
      <c:url value="${baseUrl}" var="next">
        <c:param name="view" value="${view}"/>
        <c:param name="offset" value="${offset + pagesize}"/>
        <c:if test="${not empty query}"><c:param name="teamSearch" value="${query}"/></c:if>
        <c:if test="${not empty display}"><c:param name="teams" value="${display}"/></c:if>
        <c:if test="${not empty team}"><c:param name="team" value="${team.id}"/></c:if>
      </c:url>
      <li><a href="<c:out value="${next}"/>" class="page next"><spring:message code="jsp.general.Next"/></a></li>
      <c:url value="${baseUrl}" var="last">
        <c:param name="view" value="${view}"/>
        <fmt:formatNumber pattern="#" value="${(resultset / pagesize)}" var="lp"/>
        <c:choose>
          <c:when test="${(resultset % pagesize) eq 0 or (resultset % pagesize gt 5)}">
            <fmt:formatNumber pattern="#" value="${(lp - 1) * pagesize}" var="lastPage"/>
          </c:when>
          <c:otherwise>
            <fmt:formatNumber pattern="#" value="${(lp) * pagesize}" var="lastPage"/>
          </c:otherwise>
        </c:choose>
        <c:param name="offset" value="${lastPage}"/>
        <c:if test="${not empty query}"><c:param name="teamSearch" value="${query}"/></c:if>
        <c:if test="${not empty display}"><c:param name="teams" value="${display}"/></c:if>
        <c:if test="${not empty team}"><c:param name="team" value="${team.id}"/></c:if>
      </c:url>
      <li><a href="<c:out value="${last}"/>" class="page last"><spring:message code="jsp.general.Last"/></a></li>
    </c:if>
  </ul>
</c:if>