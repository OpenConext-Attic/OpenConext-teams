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
    <h1><spring:message code='jsp.jointeam.Title' /></h1>
    <c:url value="home.shtml" var="closeUrl"><c:param name="teams" value="all" /><c:param name="view" value="${view}" /></c:url>
    <p class="close"><a href="${closeUrl}"><spring:message code='jsp.general.CloseForm' /></a></p>
  <!-- / Header -->
  </div>
  <!-- = Content -->
  <div id="Content">
    <c:url value="dojointeam.shtml" var="doJoinTeamUrl"><c:param name="view" value="${view}" /></c:url>
    <form id="JoinTeamForm" action="${doJoinTeamUrl}" method="post">
      <p class="label-field-wrapper">
        <input type="hidden" name="team" value="${team.id}" />
        <label for="TeamMessage"><spring:message code='jsp.general.Message' /></label>
        <textarea id="TeamMessage" name="message" rows="4"><spring:message code='jsp.jointeam.Message' /></textarea>
      </p>
      <p class="submit-wrapper">
        <input class="button-primary" type="submit" name="joinTeam" value="<spring:message code='jsp.jointeam.Submit' />" />
        <input class="button-secondary" type="submit" name="cancelJoinTeam" value="<spring:message code='jsp.general.Cancel' />" />
      </p>
      <br class="clear" />
    </form>
  <!-- / Content -->
  </div>
<!-- / TeamContainer -->
</div>
</teams:genericpage>