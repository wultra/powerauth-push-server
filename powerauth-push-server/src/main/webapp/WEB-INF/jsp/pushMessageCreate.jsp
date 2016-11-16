<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="header.jsp">
    <jsp:param name="pageTitle" value="PowerAuth 2.0 Push Server - Setup Application"/>
</jsp:include>

<div class="panel panel-default">

    <div class="panel-heading">
        <h3 class="panel-title">Compose Push Message</h3>
    </div>

    <div class="panel-body">
        <c:choose>
            <c:when test="${fn:length(applications) == 0}">
                <p>
                    You must configure at least one application for the push notifications first.
                </p>
            </c:when>
            <c:otherwise>
                <form action="${pageContext.request.contextPath}/web/admin/message/create/do.submit" method="POST">
                    <div class="row padder10">
                        <div class="col-sm-3">
                            Chose Application<br/>
                            <select name="appId" class="form-control" tabindex="1">
                                <c:forEach items="${applications}" var="item">
                                    <option value="<c:out value="${item.appId}"/>" <c:if test="${form.appId == item.appId}">selected="selected"</c:if>>
                                        <c:out value="${item.appName}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-sm-6">
                            <c:if test="${fields.hasFieldErrors('title')}">
                                <div class="red">You must specify a title of the message.</div>
                            </c:if>
                            Message Title<br/>
                            <input name="title" value="${form.title}" class="form-control" tabindex="3"/>
                        </div>
                        <div class="col-sm-2">
                            <label style="margin-top: 25px;">
                                <input type="checkbox" name="sound" <c:if test="${form == null || form.sound}">checked="checked"</c:if> tabindex="4"/> Play Default Sound?
                            </label>
                        </div>
                    </div>
                    <div class="row padder10">
                        <div class="col-sm-3">
                            <c:if test="${fields.hasFieldErrors('userId')}">
                                <div class="red">Enter a user ID.</div>
                            </c:if>
                            Enter User ID<br/>
                            <input name="userId" value="${form.userId}" class="form-control" tabindex="2"/>
                        </div>
                        <div class="col-sm-9">
                            <c:if test="${fields.hasFieldErrors('body')}">
                                <div class="red">You must specify a message of the notification.</div>
                            </c:if>
                            Message Text<br/>
                            <textarea name="body" class="form-control" rows="5" tabindex="5">${form.body}</textarea>
                        </div>
                    </div>
                    <div class="row padder10">
                        <div class="col-sm-12">
                            <input type="submit" value="Send Message" class="btn btn-success pull-right" tabindex="6"/>
                        </div>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </div>

</div>

<jsp:include page="footer.jsp"/>