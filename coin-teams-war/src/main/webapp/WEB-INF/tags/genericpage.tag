<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
    <title><spring:message code="jsp.general.Title" /></title>
    <c:choose>
      <c:when test='${view eq "gadget"}'>
        <link rel="stylesheet" href="css/gadget.css">
      </c:when>
      <c:otherwise>
      	<link rel="stylesheet" href="css/default.css">
      </c:otherwise>
    </c:choose>
    <script type="text/javascript" src="js/jquery-1.4.4.min.js"></script>
    <script type="text/javascript" src="js/jquery.validate.min.js"></script>
</head>
<body>
  <c:if test='${view ne "gadget"}'>
    <!--  = Header -->
    <div class="header" id="Header">
      header
    </div>
    <!-- / Header -->
  </c:if>
  <div class="page-container" id="PageContainer">

      <!-- = Main -->
      <div class="section" id="Main">
          <jsp:doBody/>
      </div>
      <!-- / Main -->

  </div>
  <c:if test='${view ne "gadget"}'>
    <!--  = Footer -->
    <div class="footer" id="Footer">
      footer
    </div>
    <!-- / Footer -->
  </c:if>
  
  <!-- / LeaveTeamDialog -->
  <div id="LeaveTeamDialog" class="hide" title="<spring:message code='jsp.dialog.leaveteam.Title' />">
    <p class="h4"><spring:message code='jsp.dialog.leaveteam.Confirmation' /></p>
  </div>
  <!-- / LeaveTeamDialog -->
  
    <!-- / DeleteTeamDialog -->
  <div id="DeleteTeamDialog" class="hide" title="<spring:message code='jsp.dialog.deleteteam.Title' />">
    <p class="h4"><spring:message code='jsp.dialog.deleteteam.Confirmation' /></p>
  </div>
  <!-- / DeleteTeamDialog -->

  
  <script type="text/javascript" src="js/jquery-ui-1.8.4.custom.min.js"></script>
  <script type="text/javascript" src="js/coin-teams.js"></script>
</body>
</html>