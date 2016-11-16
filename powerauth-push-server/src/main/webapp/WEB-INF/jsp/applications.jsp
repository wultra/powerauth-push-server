<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="header.jsp">
    <jsp:param name="pageTitle" value="PowerAuth 2.0 Push Server"/>
</jsp:include>

<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title button pull-left">Applications</h3>
        <a href="${pageContext.request.contextPath}/web/admin/app/create" class="pull-right btn btn-success">
            Add Application
        </a>
        <div class="clearfix"></div>
    </div>
    <table class="table table-hover">
        <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th class="text-center">iOS</th>
            <th class="text-center">Android</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${fn:length(applications) == 0}">
                <tr class="code text-center">
                    <p class="w80 center-block">
                    You need to import and configure applications from your PowerAuth 2.0 Server instance.
                    Click "Add Application" to do that.
                    </p>
                </tr>
            </c:when>
            <c:otherwise>
                <c:forEach items="${applications}" var="item">
                    <tr class="code clickable-row" data-href="${pageContext.request.contextPath}/web/admin/app/${item.id}/edit">
                        <td><c:out value="${item.appId}"/></td>
                        <td><c:out value="${item.appName}"/></td>
                        <td width="150" class="text-center">
                            <c:choose>
                                <c:when test="${item.ios}">
                                    <span class="green glyphicon glyphicon-ok"></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="red glyphicon glyphicon-remove"></span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td width="150" class="text-center">
                            <c:choose>
                                <c:when test="${item.android}">
                                    <span class="green glyphicon glyphicon-ok"></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="red glyphicon glyphicon-remove"></span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>
</div>

<jsp:include page="footer.jsp"/>