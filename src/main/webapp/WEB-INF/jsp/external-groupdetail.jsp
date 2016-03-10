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
<c:set var="pageTitle"><c:out value="${externalGroup.name}"/> (<c:out value="${groupProvider.name}"/>)</c:set>
<teams:genericpage pageTitle="${pageTitle}">
  <%-- = TeamContainer --%>
<div class="section" id="TeamContainer">
    <%-- = Header --%>
  <div id="Header">
    <div class="jquery-notify-bar">
      <p><spring:message code="jsp.detailteam.ExternalGroupInformationWarning" arguments="${groupProvider.name}"/></p>
    </div>
    <c:url value="/home.shtml" var="backUrl" >
      <c:param name="teams" value="externalGroups" />
      <c:param name="groupProviderId" value="${groupProvider.identifier}"/>
    </c:url>
    <p class="${backClass}"><a href="<c:out value="${backUrl}"/>">&lt; <spring:message code='jsp.detailteam.Back' /></a></p>

    <h1 class="team-title">${pageTitle}</h1>
    <br class="clear"/>
  </div>
  <div id="Content">
    <c:if test="${not empty externalGroup.description}">
      <p class="description"><c:out value="${externalGroup.description}"/></p>
    </c:if>
  </div>
</div>

</teams:genericpage>
