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
                <div class="col-sm-8">
                    Bundle ID<br/>
                    <input type="text" name="bundle" value="${bundle}" class="form-control"/>
                </div>
            </div>
            <div class="row padder10">
                <div class="col-sm-4">
                    Certificate File<br/>
                    <input type="file" name="certificate" class="form-control"/>
                </div>
                <div class="col-sm-4">
                    Certificate Password<br/>
                    <input type="password" name="password" class="form-control" autocomplete="off"/>
                </div>
            </div>
            <div class="row padder10">
                <div class="col-sm-12">
                    <input type="submit" value="Upload Certificate" class="btn btn-success"/>
                    <a class="btn btn-default" href="${pageContext.request.contextPath}/web/admin/app/${application.id}/edit">Cancel</a>
                </div>
            </div>
        </form>
    </div>

</div>

<jsp:include page="footer.jsp"/>