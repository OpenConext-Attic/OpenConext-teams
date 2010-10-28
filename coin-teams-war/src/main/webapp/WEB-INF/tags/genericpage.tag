<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
    <title><spring:message code="jsp.general.Title" /></title>
    <link rel="stylesheet" href="css/default.css">
    <script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="js/jquery.validate.min.js"></script>
</head>
<body>
  <div id="PageContainer">
    
    <!-- = Main -->
    <div class="section" id="Main">
        <jsp:doBody/>
    </div>
    <!-- / Main -->

  </div>
  
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