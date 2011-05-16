<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams" %>
<teams:genericpage>
  <div id="Header">
    <c:url value="home.shtml" var="backUrl" ><c:param name="teams" value="all" /><c:param name="view" value="app" /></c:url>
    <p class="${back}"><a href="${backUrl}">&lt; <spring:message code='jsp.home.AllTeams' /></a></p>
    <h1><spring:message code='jsp.acceptinvitation.Title' /></h1>
  </div>

  <%-- = Content --%>
  <div id="Content">
    <spring:message code="jsp.invitation${action}.failed" htmlEscape="false" />
  </div>
  <%-- / Content --%>
</teams:genericpage>