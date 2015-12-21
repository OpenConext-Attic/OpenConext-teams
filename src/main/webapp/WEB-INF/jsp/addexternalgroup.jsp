<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
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

<c:set var="pageTitle"><spring:message code='jsp.addexternalgroup.Title'/></c:set>
<teams:genericpage pageTitle="${pageTitle}">
  <%-- = TeamContainer --%>
  <div class="section" id="TeamContainer">
      <%-- = Header --%>
    <div id="Header">
      <h1>${pageTitle}</h1>
      <c:url value="/detailteam.shtml" var="closeUrl">
        <c:param name="team" value="${teamId}"/><c:param name="view" value="${view}"/>
      </c:url>
      <p class="close"><a href="<c:out value="${closeUrl}"/>"><spring:message code='jsp.general.CloseForm'/></a></p>
        <%-- / Header --%>
    </div>
      <%-- = Content --%>
    <div id="Content">
      <form action="${actionUrl}" method="post">
        <p class="label-field-wrapper">
          <input type="hidden" name="token" value="<c:out value='${tokencheck}'/>"/>
          <input type="hidden" name="view" value="<c:out value='${view}' />" />
          <input type="hidden" name="teamId" value="<c:out value='${teamId}' />" />
        </p>
        <c:choose>
          <c:when test="${fn:length(sessionScope.externalGroups) == 0}">
            <p><spring:message code="jsp.addexternalgroup.NoGroups"/></p>
          </c:when>
          <c:otherwise>
            <p><spring:message code="jsp.addexternalgroup.TeamsYouCanAdd" arguments="${team.name}"
                               htmlEscape="true"/></p>

            <c:url value="/doaddexternalgroup.shtml" var="actionUrl"/>
              <ul class="label-field-wrapper nobullets">
                <c:forEach var="externalGroup" items="${sessionScope.externalGroups}" varStatus="loop">
                  <li>
                    <input type="checkbox" name="externalGroups" id="externalGroups${loop.index}"
                           value="<c:out value="${externalGroup.identifier}"/>"/>
                    <label for="externalGroups${loop.index}" class="external-group">
                      <c:out value="${externalGroup.name}"/>
                    </label>
                  </li>
                </c:forEach>
              </ul>

              <p class="submit-wrapper">
                <input class="button" type="submit" name="addMember"
                       value="<spring:message code='jsp.addexternalgroup.Submit' />"/>
              </p>
          </c:otherwise>
        </c:choose>
      </form>
      <div class="clear"></div>
        <%-- / Content --%>
    </div>
      <%-- / TeamContainer --%>
  </div>

</teams:genericpage>
