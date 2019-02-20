<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="header.jsp">
    <jsp:param name="pageTitle" value="PowerAuth Push Server - Error"/>
</jsp:include>

<div class="panel panel-danger">

    <div class="panel-heading">
        <h3 class="panel-title">Error</h3>
    </div>

    <div class="panel-body">
        <p>
            <c:choose>
                <c:when test="${not empty message}">${message}</c:when>
                <c:otherwise>An unknown error occurred.</c:otherwise>
            </c:choose>
        </p>
        <c:if test="${not empty error_details}"><p>${error_details}</p></c:if>
        <c:if test="${not empty stacktrace}">
            <div class="code">
                ${stacktrace}
            </div>
        </c:if>
    </div>

</div>

<jsp:include page="footer.jsp"/>