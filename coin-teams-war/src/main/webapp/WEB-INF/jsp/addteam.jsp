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
    <h1><spring:message code='jsp.addteam.Title' /></h1>
    <c:url value="home.shtml" var="closeUrl"><c:param name="teams" value="my" /><c:param name="view" value="${view}" /></c:url>
    <p class="close"><a href="${closeUrl}"><spring:message code='jsp.general.CloseForm' /></a></p>
  <!-- / Header -->
  </div>
  <!-- = Content -->
  <div id="Content">
    <c:url value="doaddteam.shtml" var="doAddTeamUrl"><c:param name="view" value="${view}" /></c:url>
    <form id="AddTeamForm" action="<c:out value='${doAddTeamUrl}' />" method="post">
      <input type="hidden" name="view" value="<c:out value='${view}' />" />
      <p class="label-field-wrapper">
        <label for="TeamName"><spring:message code='jsp.general.TeamName' /></label>
        <input id="TeamName" type="text" name="team" class="required" />
      </p>
      <p class="label-field-wrapper">
        <label for="TeamDescription"><spring:message code='jsp.general.Description' /></label>
        <input id="TeamDescription" name="description" type="text" />
      </p>
      <p class="submit-wrapper">
        <input class="button-primary" type="submit" name="createTeam" value="<spring:message code='jsp.addteam.Submit' />" />
        <input class="button-secondary" type="submit" name="cancelCreateTeam" value="<spring:message code='jsp.general.Cancel' />" />
        <input id="TeamViewability" type="checkbox" name="viewabilityStatus" value="1" />
        <label for="TeamViewability"><spring:message code='jsp.general.TeamViewability' /></label>
      </p>
      <br class="clear" />
    </form>
  <!-- / Content -->
  </div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>