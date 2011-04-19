<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="teams"%>
<teams:genericpage>
<!-- = TeamContainer -->
<div class="section" id="TeamContainer">
  <!-- = Header -->
  <div id="Header">
    <h1><spring:message code='jsp.resendInvite.Title' /></h1>
    <c:url value="home.shtml" var="closeUrl"><c:param name="teams" value="my" /><c:param name="view" value="${view}" /></c:url>
    <p class="close"><a href="${closeUrl}"><spring:message code='jsp.general.CloseForm' /></a></p>
  <!-- / Header -->
  </div>
  <!-- = Content -->
  <div id="Content">
    <form:form action="doResendInvitation.shtml" commandName="invitation" method="post">
      <p class="label-field-wrapper">
        <input type="hidden" name="view" value="<c:out value='${view}' />" />
        <input type="hidden" name="team" value="<c:out value='${invitation.teamId}' />" />
        <form:label path="email"><spring:message code ="jsp.general.Email"/></form:label>
        <form:input path="email" cssErrorClass="error"/>
        <form:errors path="email" cssClass="error" element="label"/>
      </p>
      <p class="label-field-wrapper">
        <form:label path="message"><spring:message code='jsp.general.Message' /></form:label>
        <form:textarea path="message" cols="5" rows="4"/>
      </p>
      <p class="submit-wrapper">
        <input class="button-primary" type="submit" name="addMember" value="<spring:message code='jsp.addmember.Submit' />" />
        <input class="button-secondary" type="submit" name="cancelAddMember" value="<spring:message code='jsp.general.Cancel' />" />
      </p>
      <br class="clear" />
    </form:form>

  <!-- / Content -->
  </div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>