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

<teams:genericpage>
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
  <%-- = TeamContainer --%>
  <div class="section" id="TeamContainer">
      <%-- = Header --%>
    <div id="Header">
      <h1><spring:message code='jsp.addexternalgroup.Title'/></h1>
      <c:url value="/detailteam.shtml" var="closeUrl">
        <c:param name="team" value="${team.id}"/><c:param name="view" value="${view}"/>
      </c:url>
      <p class="close"><a href="<c:out value="${closeUrl}"/>"><spring:message code='jsp.general.CloseForm'/></a></p>
        <%-- / Header --%>
    </div>
      <%-- = Content --%>
    <div id="Content">
      <c:choose>
        <c:when test="${fn:length(group20List)==0}">
ERR
        </c:when>
        <c:otherwise>
          <p><spring:message code="jsp.addexternalgroup.TeamsYouCanAdd" arguments="${team.name}"
                          htmlEscape="true"/></p>

          <%-- TODO Insert magic here --%>
          <form action="<c:url value="/doaddexternalgroup.shtml"/>" method="post">
          </form>

        </c:otherwise>
      </c:choose>


      <div class="clear"></div>
        <%-- / Content --%>
    </div>
      <%-- / TeamContainer --%>
  </div>

</teams:genericpage>