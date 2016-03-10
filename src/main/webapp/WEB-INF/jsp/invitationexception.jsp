<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
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
<c:set var="pageTitle"><spring:message code='jsp.acceptinvitation.Title' /></c:set>
<teams:genericpage pageTitle="${pageTitle}">
  <div id="Header">
    <c:url value="home.shtml" var="backUrl" ><c:param name="teams" value="all" /></c:url>
    <p class="back"><a href="${backUrl}">&lt; <spring:message code='jsp.home.AllTeams' /></a></p>
    <h1>${pageTitle}</h1>
  </div>

  <%-- = Content --%>
  <div id="Content">
    <c:choose>
      <c:when test="${not empty teamUrl}">
        <spring:message code="jsp.invitation${action}.failed" htmlEscape="false" arguments="${teamUrl}" />
      </c:when>
      <c:otherwise>
        <spring:message code="jsp.invitation${action}.failed" htmlEscape="false" />
      </c:otherwise>
    </c:choose>
  </div>
  <%-- / Content --%>
</teams:genericpage>
