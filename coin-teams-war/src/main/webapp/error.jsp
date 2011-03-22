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

    <div class="section" id="Main">
    
		  <div id="Header">
		    <h1><spring:message code='jsp.error.Title' /></h1>
		  </div>
		  
		  <div id="Content">
		  	<p><spring:message code='jsp.error.Message' /></p>
		    <p>
		    	<%= exception.getMessage() %>
		    </p>
		  </div>
	
    </div>

  </div>
</body> 

</html>
