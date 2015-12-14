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

<!DOCTYPE html>
<%@ tag language="java" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<%@ attribute name="pageTitle" required="false" description="Optional page title" %>

<html>
<head>
  <meta charset="UTF-8"/>
  <meta content="width=device-width,initial-scale=0.5" name="viewport"/>
  <c:choose>
    <c:when test="${not empty pageTitle}">
      <title><c:out value="${pageTitle} - " escapeXml="false"/><spring:message code="jsp.general.Title" /></title>
    </c:when>
    <c:otherwise>
      <title><spring:message code="jsp.general.Title" /></title>
    </c:otherwise>
  </c:choose>
  <jsp:useBean id="current" class="java.util.Date" />
  <link rel="stylesheet" href="<c:url value="/css/teams.css?v=${commitId}" />">
  <!-- applicationVersion ${applicationVersion} -->
  <c:if test="${view ne 'gadget'}">
    <link rel="stylesheet" href="<c:url value="/css/app.css?v=${commitId}" />">
  </c:if>
  <c:url context="/Shibboleth.sso" value="/Login" var="loginUrl"><c:param name="target" value="${environment.teamsURL}/" /></c:url>
</head>
<body>
  <div class="wrapper">
    <c:if test='${view ne "gadget"}'>
      <!--  = Header -->
      <div class="header">
        <a href="<c:url value="/home.shtml"/>"><img class="logo" src="<c:url value="/media/surf-conext-logo.png"/>" alt="SURFnet logo" /></a>
      </div>
      <!-- / Header -->
    </c:if>
    <div class="component">
    <c:choose>
      <c:when test='${view ne "gadget" && empty sessionScope.person}'>
        <div class="component-title-bar">
          <h2 class="component-title right">
            <teams:language />
            <a href="https://wiki.surfnet.nl/display/conextsupport/SURFconext+teams" target="_blank"><spring:message code="jsp.general.Help" /></a>
            &nbsp;|&nbsp;<a href="${loginUrl}">Log&nbsp;in</a>
          </h2>
          <h2 class="component-title"><spring:message code="jsp.general.Title" /></h2>
        </div>
      </c:when>
      <c:when test='${view ne "gadget"}'>
        <div class="component-title-bar">
          <h2 class="component-title right">
            <spring:message code="jsp.general.Welcome"/>&nbsp;<c:out value="${sessionScope.person.displayName}" />
            <teams:language />
            <a href="https://wiki.surfnet.nl/display/conextsupport/SURFconext+teams" target="_blank"><spring:message code="jsp.general.Help" /></a>
            &nbsp;|&nbsp;<a href="/Shibboleth.sso/Logout?target=/teams"><spring:message code="jsp.general.Logout"/></a>
          </h2>
          <h2 class="component-title"><spring:message code="jsp.general.Title" /></h2>
        </div>
      </c:when>
    </c:choose>
    <c:if test='${view ne "gadget"}'>
    </c:if>
      <div class="component-content" id="PageContainer">

          <!-- = Main -->
          <div class="section" id="Main">
              <jsp:doBody/>
          </div>
          <!-- / Main -->

      </div>
    </div>
    <c:if test='${view ne "gadget"}'>
      <!--  = Footer -->
      <div class="footer" id="Footer">
        <span><spring:message code="jsp.general.surfnet.Name" /></span>&nbsp;|&nbsp;</span><span><a href="mailto:help@surfconext.nl">help@surfconext.nl</a></span>&nbsp;|&nbsp;<span><a href="<spring:message code="jsp.general.TermsOfUse.url" />" target="_blank"><spring:message code="jsp.general.TermsOfUse" /></a></span>
      </div>
      <!-- / Footer -->
    </c:if>

    <!-- = LeaveTeamDialog -->
    <div id="LeaveTeamDialog" class="hide" title="<spring:message code='jsp.dialog.leaveteam.Title' />">
      <p class="h4"><spring:message code='jsp.dialog.leaveteam.Confirmation' /></p>
    </div>
    <!-- / LeaveTeamDialog -->

    <!-- = DeleteTeamDialog -->
    <div id="DeleteTeamDialog" class="hide" title="<spring:message code='jsp.dialog.deleteteam.Title' />">
      <p class="h4"><spring:message code='jsp.dialog.deleteteam.Confirmation' /></p>
    </div>
    <!-- / DeleteTeamDialog -->
  </div>
  <script type="text/javascript" src="<c:url value="/js/lib/jquery-2.1.1.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/lib/jquery.validate.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/lib/jquery-ui.1.11.1.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/lib/typeahead.0.10.5.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/coin-teams.js?v=${commitId}"/>"></script>
  <%--
    We need the view parameter in the search result, which is now Ajax, we use this JavaScipt variable for it
   --%>
   <script language="JavaScript">
     var view = '<c:out value="${view}"/>';
   </script>
  
</body>
</html>
