<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="header.jsp">
    <jsp:param name="pageTitle" value="PowerAuth 2.0 Push Server - Setup Application"/>
</jsp:include>

<ol class="breadcrumb">
    <li><a class="black" href="${pageContext.request.contextPath}/web/admin/app/list">Applications</a></li>
    <li><a class="black" href="${pageContext.request.contextPath}/web/admin/app/${application.id}/edit">${application.appName}</a></li>
    <li class="active">APNs Settings</li>
</ol>

<div class="panel panel-default">

    <div class="panel-heading">
        <h3 class="panel-title">Upload APNs Certificate</h3>
    </div>

    <div class="panel-body">
        <form action="${pageContext.request.contextPath}/web/admin/app/${application.id}/ios/upload/do.submit" method="POST" enctype="multipart/form-data">
            <div class="row padder10">
                <div class="col-sm-6">
                    <c:if test="${fields.hasFieldErrors('teamId')}">
                        <div class="red">Please enter a valid team ID.</div>
                    </c:if>
                    Team ID<br/>
                    <input type="text" name="teamId" value="${teamId}" class="form-control" autocomplete="off"/>
                </div>
                <div class="col-sm-6">
                    <c:if test="${fields.hasFieldErrors('keyId')}">
                        <div class="red">Please enter a valid key ID.</div>
                    </c:if>
                    Key ID<br/>
                    <input type="text" name="keyId" value="${keyId}" class="form-control" autocomplete="off"/>
                </div>
                <div class="col-sm-6">
                    <c:if test="${fields.hasFieldErrors('bundle')}">
                        <div class="red">Please enter a valid iOS bundle ID (for example "io.getlime.app.MyApp").</div>
                    </c:if>
                    Bundle ID<br/>
                    <input type="text" name="bundle" value="${bundle}" class="form-control"/>
                </div>
                <div class="col-sm-6">
                    <c:if test="${fields.hasFieldErrors('privateKey')}">
                        <div class="red">You need to upload a valid private key.</div>
                    </c:if>
                    Private Key File<br/>
                    <input type="file" name="privateKey" class="form-control"/>
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