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
		    <h1>Error</h1>
		  </div>
		  
		  <div id="Content">
		  	<p>Something went wrong. Please try to reload the page or go back to 
          <a href="home.shtml?teams=my">My Teams</a></p>
          <%
            if (exception != null) {
              out.print("<p>");
              out.print(exception.getMessage());
              out.print("</p>");
            }
          %>
		  </div>
	
    </div>

  </div>
</body> 

</html>
