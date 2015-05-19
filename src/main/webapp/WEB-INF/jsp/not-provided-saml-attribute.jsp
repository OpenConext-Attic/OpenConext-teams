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
<html>
<head>
  <title>SURFconext Teams</title>
  <link rel="stylesheet" href="<c:url value="/css/teams.css"/>">
  <link rel="stylesheet" href="<c:url value="/css/app.css"/>">
</head>
<body>
<div class="wrapper">
  <div class="header">
    <a href="home.shtml"><img class="logo" src="<c:url value="/media/surf-conext-logo.png"/>" alt="SURFconext logo"/></a>
  </div>
  <%-- / Header --%>
    <div class="component">
        <div class="component-title-bar">
          <h2 class="component-title">Error</h2>
        </div>
      <div class="component-content" id="PageContainer">

        <div class="section" id="Main">

          <div id="Header">
            <h1>Error</h1>
          </div>

          <div id="Content">
            <p>Your organization did not provide all required information in order to teams to function correctly. Not provided information:</p>
            <ul>
            <c:forEach var="samlAttribute" items="${notProvidedSamlAttributes}" varStatus="index">
              <li><c:out value="${samlAttribute}"/></li>
            </c:forEach>
            </ul>
            <p>Please contact SURFnet for more information by sending a mail to <a href="mailto:help@surfconext.nl">help@surfconext.nl</a>.</p>
          </div>

        </div>

      </div>
    </div>

  <%--  = Footer --%>
  <div class="footer" id="Footer">
    <span>SURFnet</span>&nbsp;|&nbsp;</span><span><a href="mailto:help@surfconext.nl">help@surfconext.nl</a></span>&nbsp;|&nbsp;<span><a href="https://wiki.surfnet.nl/display/conextsupport/Terms+of+Service+%28EN%29" target="_blank">Terms of Service</a></span>
  </div>
  <%-- / Footer --%>
</div>
</body>

</html>
