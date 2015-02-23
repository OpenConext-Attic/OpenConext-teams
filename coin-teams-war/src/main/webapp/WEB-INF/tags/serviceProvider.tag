<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ attribute name="existingServiceProvider" required="false" type="nl.surfnet.coin.stoker.StokerEntry" %>
<%@ attribute name="cssClass" required="true" %>

<li class="${cssClass}">
  <span>
    <c:if test="${!empty existingServiceProvider}">
      <c:out value="${existingServiceProvider.displayNameEn}"/>
    </c:if>
  </span>
  <c:choose>
    <c:when test="${!empty existingServiceProvider}">
      <input type="hidden" value="${existingServiceProvider.entityId}" name="services[]">
    </c:when>
    <c:otherwise>
      <input type="hidden" name="services[]">
    </c:otherwise>
  </c:choose>
  <a class="delete-service-provider" href="#"><spring:message code='jsp.addallowedserviceproviders.delete'/></a>
</li>
