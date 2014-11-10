<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
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

<teams:genericpage>
<!-- = Header -->
<!-- = Content -->
<div id="Content">
  <spring:message code="jsp.landingpage.Content" htmlEscape="false" />
  <c:url context="/Shibboleth.sso" value="/Login" var="loginUrl"><c:param name="target" value="${environment.teamsURL}/" /></c:url>
  <a class="button" id="loginbutton" href="${loginUrl}"><spring:message code="jsp.landingpage.Login" /></a>
  <br>
  <input id="koekje" type="checkbox" onClick="return requestCookie();"/><label for="koekje"><spring:message code="jsp.landingpage.skip" /></label>
<!-- / Content -->
</div>
<script src="js/jquery-1.4.4.min.js"></script>
<script>
  /* AJAX call to retrieve a cookie */
	function requestCookie() {
		console.log('requesting cookie from teams');
		$.ajax({
			  type: 'POST',
			  data: "",
			  success: success,
			  dataType: "text/html",
			  async:false
			});
		return true;
	}
	
	/* nothing to do here */
	function success() {
		console.log("received reply");
	}
	
	/* focus the login button */
	$(function() {
		$("#loginbutton").focus();
	});
</script>
</teams:genericpage>
