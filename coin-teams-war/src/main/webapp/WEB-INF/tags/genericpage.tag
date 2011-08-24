<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%--
  ~ Copyright 2011 SURFnet bv, The Netherlands
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8"/>
  <title><spring:message code="jsp.general.Title" /></title>
    <c:choose>
      <c:when test='${view eq "gadget"}'>
        <link rel="stylesheet" href="<c:url value="/css/gadget.css" />">
      </c:when>
      <c:otherwise>
      	<link rel="stylesheet" href="<c:url value="/css/default.css" />">
      </c:otherwise>
    </c:choose>
</head>
<body>
  <div class="wrapper">
    <c:if test='${view ne "gadget"}'>
      <!--  = Header -->
      <div class="header">
        <a href="home.shtml"><img class="logo" src="<c:url value="/media/surfnet_logo.gif"/>" alt="surfnet logo" /></a> <span><spring:message code="jsp.general.Title" /></span>
        <span class="left"></span><span class="right"></span><img src="<c:url value="/media/header_img.jpg"/>" alt="header img" />
      </div>
      <!-- / Header -->
    </c:if>
    <div class="component">
    <c:choose>
      <c:when test='${view ne "gadget" && empty sessionScope.person}'>
        <div class="component-title-bar">
          <h2 class="component-title"><spring:message code="jsp.general.Welcome" /></h2>
        </div>
      </c:when>
      <c:when test='${view ne "gadget"}'>
        <div class="component-title-bar">
          <h2 class="component-title"><spring:message code="jsp.general.Title" /><span class="right"><spring:message code="jsp.general.Welcome"/> <c:out value="${sessionScope.person.displayName}" /> | <a href="/Shibboleth.sso/Logout?target=/teams"><spring:message code="jsp.general.Logout"/></a> | <a href="https://wiki.surfnetlabs.nl/display/conextsupport/SURFteams" target="_blank"><spring:message code="jsp.general.Help" /></a></span></h2>
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
        <p>&nbsp;</p>

        <address>
          <span><strong><spring:message code="jsp.general.surfnet.Name" /></strong></span><span><spring:message code="jsp.general.surfnet.Address" /></span><span><spring:message code="jsp.general.surfnet.Mailbox" /></span><span><spring:message code="jsp.general.surfnet.Zip" /></span><span><spring:message code="jsp.general.Question" />&nbsp;<a class="extra" href="mailto:help@surfteams.nl">help@surfteams.nl</a></span><a class="extra" href="http://www.surfnet.nl/en/pages/copyright.aspx" target="_blank"><spring:message code="jsp.general.Copyright" /></a><a class="extra" href="https://wiki.surfnetlabs.nl/display/conextsupport/Terms+of+Service+%28EN%29" target="_blank"><spring:message code="jsp.general.TermsOfUse" /></a>
        </address>
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

    <!-- = DeleteMemberDialog -->
    <div id="DeleteMemberDialog" class="hide" title="<spring:message code='jsp.dialog.deletemember.Title' />">
      <p class="h4"><spring:message code='jsp.dialog.deletemember.Confirmation' /></p>
    </div>
    <!-- / DeleteMemberDialog -->
  </div>

  <script type="text/javascript" src="<c:url value="/js/jquery-1.4.4.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/jquery.validate.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/jquery-ui-1.8.13.custom.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/coin-teams.js"/>"></script>
</body>
</html>