<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="header.jsp">
    <jsp:param name="pageTitle" value="PowerAuth 2.0 Push Server - Push Credentials Setup"/>
</jsp:include>

<ol class="breadcrumb">
    <li><a class="black" href="${pageContext.request.contextPath}/web/admin/app/list">Applications</a></li>
    <li class="active">${application.appName}</li>
</ol>

<div class="row">
    <div class="col-sm-6">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title pull-left">APNs Settings</h3>
                <c:choose>
                    <c:when test="${application.ios}">
                        <span class="green pull-right"><span class="glyphicon glyphicon-ok"></span> Ready</span>
                    </c:when>
                    <c:otherwise>
                        <span class="red pull-right"><span class="glyphicon glyphicon-remove"></span> Not Configured</span>
                    </c:otherwise>
                </c:choose>
                <div class="clearfix"></div>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${application.ios}">
                        <form class="form-inline" action="${pageContext.request.contextPath}/web/admin/app/${application.id}/ios/remove/do.submit" method="POST">
                            <a class="btn btn-success" href="${pageContext.request.contextPath}/web/admin/app/${id}/ios/upload">Re-configure</a>
                            <input type="submit" value="Remove" class="btn btn-danger"/>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <a class="btn btn-success" href="${pageContext.request.contextPath}/web/admin/app/${application.id}/ios/upload">Configure</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
    <div class="col-sm-6">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title pull-left">Firebase Settings</h3>
                <c:choose>
                    <c:when test="${application.android}">
                        <span class="green pull-right"><span class="glyphicon glyphicon-ok"></span> Ready</span>
                    </c:when>
                    <c:otherwise>
                        <span class="red pull-right"><span class="glyphicon glyphicon-remove"></span> Not Configured</span>
                    </c:otherwise>
                </c:choose>
                <div class="clearfix"></div>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${application.android}">
                        <form class="form-inline" action="${pageContext.request.contextPath}/web/admin/app/${application.id}/android/remove/do.submit" method="POST">
                            <a class="btn btn-success" href="${pageContext.request.contextPath}/web/admin/app/${application.id}/android/upload">Re-configure</a>
                            <input type="submit" value="Remove" class="btn btn-danger"/>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <a class="btn btn-success" href="${pageContext.request.contextPath}/web/admin/app/${application.id}/android/upload">Configure</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<jsp:include page="footer.jsp"/>