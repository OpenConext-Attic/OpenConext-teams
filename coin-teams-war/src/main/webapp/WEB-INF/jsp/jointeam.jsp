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
    <h1><spring:message code='jsp.jointeam.Title' /></h1>
    <c:url value="home.shtml" var="closeUrl"><c:param name="teams" value="all" /><c:param name="view" value="${view}" /></c:url>
    <p class="close"><a href="${closeUrl}"><spring:message code='jsp.general.CloseForm' /></a></p>
  <!-- / Header -->
  </div>
  <!-- = Content -->
  <div id="Content">
    <form:form id="JoinTeamForm" method="post" commandName="joinTeamRequest" action="dojointeam.shtml">
      <p class="label-field-wrapper">
        <input type="hidden" name="team" value="<c:out value='${joinTeamRequest.groupId}' />" />
        <input type="hidden" name="view" value="<c:out value='${view}' />" />
        <form:label path="message"><spring:message code='jsp.general.Message' /></form:label>
        <form:textarea path="message" rows="4" cols="5"/>
      </p>
      <p class="submit-wrapper">
        <input class="button-primary" type="submit" name="joinTeam"
               value="<spring:message code='jsp.jointeam.Submit' />" />
        <input class="button-secondary" type="submit" name="cancelJoinTeam"
               value="<spring:message code='jsp.general.Cancel' />" />
      </p>
      <br class="clear" />
    </form:form>
  <!-- / Content -->
  </div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>