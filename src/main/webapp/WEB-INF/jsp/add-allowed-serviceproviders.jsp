<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
<c:set var="pageTitle"><spring:message code='jsp.addallowedserviceproviders.Title'/></c:set>
<teams:genericpage pageTitle="${pageTitle}">
  <div class="section add-allowed-sps" id="TeamContainer">
    <div id="Header">
      <div class="team-container">
        <h1>${pageTitle}</h1>
      </div>
      <div class="team-container">
        <h1><spring:message code='jsp.addallowedserviceproviders.teamswithaccess'/></h1>
      </div>
    </div>
    <div id="Content" class="teams-content">
      <div class="team-container" id="search-service-providers-container" data-url="<c:url value='/service-providers.json'/>">
        <input type="text" id="search-service-providers" name="search-service-providers" size="50px;">
      </div>
      <div class="team-container" id="selected-service-providers-container">
        <form action="<c:url value='/${teamId}/service-providers.shtml'/>" method="post">
          <input type="hidden" name="team" value="<c:out value="${teamId}"/>">
          <ul id="selected-service-providers">
            <teams:serviceProvider cssClass="hidden-service-provider"/>

            <c:forEach items="${existingServiceProviders}" var="existingServiceProvider">
              <teams:serviceProvider existingServiceProvider="${existingServiceProvider}" cssClass="existing-service-provider"/>
            </c:forEach>
          </ul>
          <input class="button" type="submit" value="<spring:message code='jsp.addallowedserviceproviders.add'/>">
          <input class="button" type="submit" name="cancel-add-sp" value="<spring:message code='jsp.general.Cancel' />" />
        </form>

      </div>
    </div>
  </div>
</teams:genericpage>
