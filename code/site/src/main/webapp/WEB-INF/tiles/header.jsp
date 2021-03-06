<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>
<% response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept"); %>
<% response.setHeader("Access-Control-Allow-Methods", "GET, POST"); %>
<% response.setHeader("Access-Control-Allow-Credentials", "true"); %>

<div id="header" class="header">
	<table width="100%" border="0">
		<tr>
			<td width="50%">
				<h1><a href="<c:url value="/index.html" />">Ikube</a></h1>
			</td>
			<td width="25%" style="float : right;" nowrap="nowrap">
					<form id="search-form" name="search-form" action="<c:url value="/results.html" />">
						<input type="submit" value="Go!">
						<input id="searchString" name="searchString" value="${param.searchString}" width="150px">
					</form>
			</td>
			<td nowrap="nowrap">
				<security:authorize access="isAuthenticated()">
					<a title="<security:authentication property="authorities" />" href="#">
						<spring:message code="security.logged.in.as" />&nbsp;<img src="<c:url value="/img/icons/person_obj.gif" />">&nbsp;<security:authentication property="name" /> 
					</a>
				<a href="<spring:url value="/logout" htmlEscape="true" />" title="<spring:message code="security.logout" />"><spring:message code="security.logout" /></a>
				</security:authorize>
			</td>
		</tr>
	</table>
</div>