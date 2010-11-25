<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head>
    <title>COIN Teams</title>
    <link rel="stylesheet" href="css/default.css">
</head>
<body>
  <div id="PageContainer">

    <!-- = Main -->
    <div class="section" id="Main">
    
	  <!-- = Header -->
	  <div id="Header">
	    <h1><spring:message code='jsp.error.Title' /></h1>
	  <!-- / Header -->
	  </div>
		
	  <!-- = Content -->
	  <div id="Content">
	    <p><spring:message code='jsp.error.Message' /></p>
	  <!-- / Content -->
	  </div>
	
	<!-- / Main -->
    </div>

  </div>
</body> 

</html>
