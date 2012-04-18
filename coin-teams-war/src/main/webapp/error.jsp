<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ page isErrorPage="true" %>
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
  <%
    String view = request.getParameter("view");
  %>
<html>
<head>
  <title>SURFconext teams</title>
  <% if ("gadget".equals(view)) { %>
  <link rel="stylesheet" href="<c:url value="/css/gadget.css"/>">
  <% } else { %>
  <link rel="stylesheet" href="<c:url value="/css/default.css"/>">
  <% } %>
</head>
<body>
<div class="wrapper">
  <%--  = Header --%>
  <%
    if(!("gadget".equals(view))) {
  %>
  <div class="header">
    <a href="home.shtml"><img class="logo" src="<c:url value="/media/surfnet_logo.png"/>" alt="SURFnet logo"/></a>
    <span>SURFconext teams</span>
    <span class="left"></span><span class="right"></span><img src="<c:url value="/media/header_img.jpg"/>" alt="header img"/>
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
              <c:url value="home.shtml" var="homeUrl"><c:param name="teams" value="my" /><c:param name="view"><c:out value="<%=view%>"/></c:param></c:url>
              <a href="${homeUrl}">My Teams</a></p>
          </div>

        </div>

      </div>
    </div>

  <%
    if (!("gadget".equals(view))) {
  %>

  <%--  = Footer --%>
  <div class="footer" id="Footer">
    <p>&nbsp;</p>
    <address>
      <span><strong>SURFnet bv</strong></span>
      <span>Radboudkwartier 273</span>
      <span>Postbus 19035</span>
      <span>3501 DA Utrecht</span>
      <span>For questions email:&nbsp;<a href="mailto:help@surfteams.nl">help@surfteams.nl</a></span>
    </address>
    <ul>
      <li><a class="extra" href="https://wiki.surfnetlabs.nl/display/conextsupport/Terms+of+Service+%28EN%29" target="_blank">Terms of Service</a></li>
      <li><a class="extra" href="http://www.surfnet.nl/en/pages/copyright.aspx" target="_blank">Copyright</a></li>
    </ul>
  </div>
  <%
    }
  %>
  <%-- / Footer --%>
</div>
</body>

</html>
