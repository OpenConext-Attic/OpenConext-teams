<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
    <title><spring:message code="jsp.general.Title" /></title>
    <link rel="stylesheet" href="css/ext/jqueryui/1.8.2/themes/base/jquery-ui.css" type="text/css" media="all" />
    <link rel="stylesheet" href="css/default.css">
    <script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
</head>
<body>
  <div id="PageContainer">
    
    <!-- = Main -->
    <div class="section" id="Main">
        <jsp:doBody/>
    </div>
    <!-- / Main -->

  </div>

  <!-- <div id="gadgets-root"></div> -->
    <script type="text/javascript" src="js/jquery-ui-1.8.4.custom.min.js"></script>
    <script type="text/javascript" src="js/jquery.easing.1.3.js"></script>
    <script type="text/javascript" src="js/coin-teams.js"></script>
</body>
</html>