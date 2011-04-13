<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<teams:genericpage>
<!-- = Header -->
<!-- = Content -->
<div id="Content">
  <spring:message code="jsp.landingpage.Content" />
  <c:url context="/Shibboleth.sso" value="/Login" var="loginUrl"><c:param name="target" value="${environment.teamsURL}" /></c:url>
  <a href="<c:out value='${loginUrl}' />"><spring:message code="jsp.landingpage.Login" /></a>
<!-- / Content -->
</div>
</teams:genericpage>