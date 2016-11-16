<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>${param.pageTitle}</title>
<link rel="shortcut icon" href="${pageContext.request.contextPath}/resources/images/favicon.png">

<!-- Bootstrap -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/base.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/lime-theme.min.css">

</head>
<body>

	<nav class="navbar navbar-inverse navbar-fixed-top">
		<div class="container">
			<div id="navbar" class="collapse navbar-collapse">
                <ul class="nav navbar-nav">
                    <li <c:if test="${fn:startsWith(requestScope['javax.servlet.forward.servlet_path'], '/web/admin/app/')}">class="active"</c:if>>
                        <a href="${pageContext.request.contextPath}/">Applications</a>
                    </li>
                    <li <c:if test="${fn:startsWith(requestScope['javax.servlet.forward.servlet_path'], '/web/admin/message/')}">class="active"</c:if>>
                        <a href="${pageContext.request.contextPath}/web/admin/message/create">Send Push</a>
                    </li>
                </ul>
			</div>
			<!--/.nav-collapse -->
		</div>
	</nav>

	<div class="container" style="margin-top: 80px;">
		