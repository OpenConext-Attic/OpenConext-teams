<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams" %>
<teams:genericpage>
  <%-- = Content --%>
  <div id="Content">
    <c:choose>
      <c:when test="${result eq 'true'}">
        <spring:message code="jsp.invitationdeclined.success" htmlEscape="false" />
      </c:when>
      <c:otherwise>
        <spring:message code="jsp.invitationdeclined.failed" htmlEscape="false" />
      </c:otherwise>
    </c:choose>
  </div>
  <%-- / Content --%>
</teams:genericpage>