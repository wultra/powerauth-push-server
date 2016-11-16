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
    <li class="active">Add Application</li>
</ol>

<div class="panel panel-default">

    <div class="panel-heading">
        <h3 class="panel-title">Add Application</h3>
    </div>

    <div class="panel-body">
        <c:choose>
            <c:when test="${fn:length(applications) == 0}">
                <p>
                    <strong class="green"><span class="glyphicon glyphicon-ok"></span> Well Done!</strong>
                    There are no more applications to be configured for push notifications in your PowerAuth 2.0 Server instance.
                </p>
            </c:when>
            <c:otherwise>
                <p>
                    You can configure push notifications for any application from you PowerAuth 2.0 Server instance.
                    Which application would you like to configure?
                </p>
                <c:if test="${fields.hasErrors('appId')}">
                    <div class="alert alert-danger">
                        Unable to configure application - invalid app ID.
                    </div>
                </c:if>
                <form action="${pageContext.request.contextPath}/web/admin/app/create/do.submit" class="form-inline pull-left" method="POST">
                    <div class="form-group padder10">
                        <select name="appId" class="form-control" style="width: 200px;">
                            <c:forEach items="${applications}" var="item">
                                <option value="<c:out value="${item.id}"/>">
                                    <c:out value="${item.applicationName}"/>
                                </option>
                            </c:forEach>
                        </select>
                        <input type="submit" value="Select" class="btn btn-success"/>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </div>

</div>
	
<jsp:include page="footer.jsp"/>