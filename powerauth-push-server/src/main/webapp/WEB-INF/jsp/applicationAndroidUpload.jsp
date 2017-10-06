<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<jsp:include page="header.jsp">
    <jsp:param name="pageTitle" value="PowerAuth 2.0 Push Server - Setup Application"/>
</jsp:include>

<ol class="breadcrumb">
    <li><a class="black" href="${pageContext.request.contextPath}/web/admin/app/list">Applications</a></li>
    <li><a class="black" href="${pageContext.request.contextPath}/web/admin/app/${application.id}/edit">${application.appName}</a></li>
    <li class="active">Firebase Settings</li>
</ol>

<div class="panel panel-default">

    <div class="panel-heading">
        <h3 class="panel-title">Set Firebase Cloud Messaging Token</h3>
    </div>

    <div class="panel-body">
        <form action="${pageContext.request.contextPath}/web/admin/app/${application.id}/android/upload/do.submit" method="POST">
            <div class="row padder10">
                <div class="col-sm-6">
                    <c:if test="${fields.hasFieldErrors('bundle')}">
                        <div class="red">Please enter a valid Android package name (for example "io.getlime.app.MyApp").</div>
                    </c:if>
                    Package name
                    <br/>
                    <input type="text" name="bundle" value="${bundle}" class="form-control"/>
                </div>
                <div class="col-sm-6">
                    <c:if test="${fields.hasFieldErrors('token')}">
                        <div class="red">Please enter a correct FCM token value.</div>
                    </c:if>
                    Firebase Cloud Messaging Token
                    <br/>
                    <textarea name="token" rows="4" class="form-control">${token}</textarea>
                </div>
            </div>
            <div class="row padder10">
                <div class="col-sm-12">
                    <input type="submit" value="Save" class="btn btn-success"/>
                    <a class="btn btn-default" href="${pageContext.request.contextPath}/web/admin/app/${application.id}/edit">Cancel</a>
                </div>
            </div>
        </form>
    </div>

</div>

<jsp:include page="footer.jsp"/>