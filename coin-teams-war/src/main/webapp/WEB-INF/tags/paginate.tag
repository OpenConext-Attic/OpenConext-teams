<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:if test="${resultset > pagesize}">
  <ul class="pagination">
    <c:if test="${offset >= pagesize}">
      <c:url value="home.shtml" var="first">
        <c:param name="view" value="${view}"/>
        <c:param name="teamSearch" value="${query}"/>
        <c:param name="teams" value="${display}"/>
      </c:url>
      <li><a href="<c:out value="${first}"/>">&laquo;</a></li>
      <c:url value="home.shtml" var="prev">
        <c:param name="view" value="${view}"/>
        <c:param name="teamSearch" value="${query}"/>
        <c:param name="offset" value="${offset - pagesize}"/>
        <c:param name="teams" value="${display}"/>
      </c:url>
      <li><a href="<c:out value="${prev}"/>" class="page first">&lsaquo;</a></li>
    </c:if>

    <c:set var="begin">
      <c:choose>
        <%-- Less than 5 pages in total --%>
        <c:when test="${(resultset / pagesize) < 5}">0</c:when>
        <%-- First 5 pages --%>
        <c:when test="${(offset / pagesize ) < 5}">0</c:when>
        <%-- Last 5 pages --%>
        <c:when test="${((resultset - offset) / pagesize) < 5}">
          <fmt:formatNumber pattern="#" value="${((resultset) - (5 * pagesize)) / pagesize}" var="tail"/>
          ${tail * pagesize}
        </c:when>
        <%-- Somewhere in the middle --%>
        <c:otherwise><fmt:formatNumber pattern="#" value="${(offset) - (2 * pagesize)}"/></c:otherwise>
      </c:choose>
    </c:set>
    <c:set var="end">
      <c:choose>
        <%-- till the end --%>
        <c:when test="${(begin + (4 * pagesize)) > resultset}">${resultset}</c:when>
        <%-- Next 4 pages --%>
        <c:otherwise>${begin + (4 * pagesize)}</c:otherwise>
      </c:choose>
    </c:set>

    <%--${begin} - ${end} of ${resultset}--%>
    
    <c:forEach begin="${begin}" end="${end}" step="${pagesize}" var="localOffset">
      <li>
        <c:url value="home.shtml" var="pageUrl">
          <c:param name="view" value="${view}"/>
          <c:param name="teamSearch" value="${query}"/>
          <c:param name="offset" value="${localOffset}"/>
          <c:param name="teams" value="${display}"/>
        </c:url>
        <c:choose>
          <c:when test="${localOffset eq offset}"><span class="page"><fmt:formatNumber pattern="#" value="${(localOffset / pagesize) + 1}"/></span></c:when>
          <c:otherwise><a href="<c:out value="${pageUrl}"/>" class="page"><fmt:formatNumber pattern="#" value="${(localOffset / pagesize) + 1}"/></a></c:otherwise>
        </c:choose>
      </li>
    </c:forEach>
    <c:if test="${offset + pagesize < resultset}">
      <c:url value="home.shtml" var="next">
        <c:param name="view" value="${view}"/>
        <c:param name="teamSearch" value="${query}"/>
        <c:param name="offset" value="${offset + pagesize}"/>
        <c:param name="teams" value="${display}"/>
      </c:url>
      <li><a href="<c:out value="${next}"/>" class="page last">&rsaquo;</a></li>
      <c:url value="home.shtml" var="last">
        <c:param name="view" value="${view}"/>
        <c:param name="teamSearch" value="${query}"/>
        <c:param name="teams" value="${display}"/>
        <fmt:formatNumber pattern="#" value="${(resultset / pagesize)}" var="lp"/>
        <fmt:formatNumber pattern="#" value="${(lp - 1) * pagesize}" var="lastPage"/>
        <c:param name="offset" value="${lastPage}"/>
      </c:url>
      <li><a href="<c:out value="${last}"/>">&raquo;</a></li>
    </c:if>
  </ul>
</c:if>