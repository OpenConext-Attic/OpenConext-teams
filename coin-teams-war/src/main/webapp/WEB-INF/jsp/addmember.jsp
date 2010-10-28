<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<teams:genericpage>
<!-- = TeamContainer -->
<div class="section" id="TeamContainer">
	<!-- = Header -->
	<div id="Header">
		<h1><spring:message code='jsp.addmember.Title' /></h1>
		<p class="close"><a href="home.shtml?teams=my"><spring:message code='jsp.general.CloseForm' /></a></p>
	<!-- / Header -->
	</div>
	<!-- = Content -->
	<div id="Content">
		<form id="AddMemberForm" action="doaddmember.shtml" method="post">
			<p class="label-field-wrapper">
				<input type="hidden" name="team" value="${team.id}" />
				<input type="hidden" name="testEmail" id="TestEmail" class="email" value="" />
				<label for="MemberEmail"><spring:message code='jsp.general.Email' /></label>
				<input id="MemberEmail" type="text" name="memberEmail" value="<spring:message code='jsp.addmember.Email' />" class="multiemail required" />
			</p>
			<p class="label-field-wrapper">
				<label for="MemberMessage"><spring:message code='jsp.general.Message' /></label>
				<textarea id="MemberMessage" name="message" rows="4"><spring:message code='jsp.addmember.Message' /></textarea>
			</p>
			<p class="submit-wrapper">
				<input class="button-primary" type="submit" name="addMember" value="<spring:message code='jsp.addmember.Submit' />" />
				<input class="button-secondary" type="submit" name="cancelAddMember" value="<spring:message code='jsp.general.Cancel' />" />
			</p>
			<br class="clear" />
		</form>
	<!-- / Content -->
	</div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>