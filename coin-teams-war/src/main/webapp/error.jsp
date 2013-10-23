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
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ page isErrorPage="true" %>

  <%
    String view = request.getParameter("view");
  %>
<html>
<head>
  <title>SURFconext Teams</title>
  <link rel="stylesheet" href="<c:url value="/css/teams.css"/>">
  <% if (!"gadget".equals(view)) { %>
  <link rel="stylesheet" href="<c:url value="/css/app.css"/>">
  <% } %>
</head>
<body>
<div class="wrapper">
  <%--  = Header --%>
  <%
    if(!("gadget".equals(view))) {
  %>
  <div class="header">
    <a href="home.shtml"><img class="logo" src="<c:url value="/media/surf-conext-logo.png"/>" alt="SURFconext logo"/></a>
  </div>
  <%
    }
  %>
  <%-- / Header --%>
    <div class="component">

      <%-- if not gadget --%>
      <%
        if(!("gadget".equals(view))) {
      %>
        <div class="component-title-bar">
          <h2 class="component-title">Error</h2>
        </div>
      <%
        }
      %>

      <div class="component-content" id="PageContainer">

        <div class="section" id="Main">

          <div id="Header">
            <h1>Error</h1>
          </div>

          <div id="Content">
            <p>Something went wrong. Please try to reload the page or go back to
              <c:url value="/home.shtml" var="homeUrl"><c:param name="teams" value="my" /><c:param name="view"><c:out value="<%=view%>"/></c:param></c:url>
              <a href="${homeUrl}">SURFconext Teams</a></p>
          </div>

        </div>

      </div>
    </div>

  <%
    if (!("gadget".equals(view))) {
  %>

  <%--  = Footer --%>
  <div class="footer" id="Footer">
    <span>SURFnet</span>&nbsp;|&nbsp;</span><span><a href="mailto:help@surfconext.nl">help@surfconext.nl</a></span>&nbsp;|&nbsp;<span><a href="https://wiki.surfnetlabs.nl/display/conextsupport/Terms+of+Service+%28EN%29" target="_blank">Terms of Service</a></span>
  </div>
  <%
    }
  %>
  <%-- / Footer --%>
</div>
</body>

</html>
