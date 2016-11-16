<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="header.jsp">
    <jsp:param name="pageTitle" value="PowerAuth 2.0 Push Server - Error"/>
</jsp:include>

<div class="panel panel-danger">

    <div class="panel-heading">
        <h3 class="panel-title">Error</h3>
    </div>

    <div class="panel-body">
        <p>An unknown error occurred.</p>
        <div class="code">
            ${stacktrace}
        </div>
    </div>

</div>

<jsp:include page="footer.jsp"/>