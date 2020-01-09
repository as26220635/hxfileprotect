<%--
  Created by IntelliJ IDEA.
  User: 余庚鑫
  Date: 2017/4/16
  Time: 15:50
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>

<c:if test="${empty PJAX}">
    <html>
    <head>
        <%@ include file="/WEB-INF/jsp/common/common_meta.jsp" %>
        <title>404</title>
        <%@ include file="/WEB-INF/jsp/common/common_js.jsp" %>
    </head>
    <body class="index-page">

</c:if>

<div class="weui-msg">
    <div class="weui-msg__icon-area"><i class="weui-icon-info weui-icon_msg"></i></div>
    <div class="weui-msg__text-area">
        <h2 class="weui-msg__title">404</h2>
        <p class="weui-msg__desc">没有找到页面</p>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/mobile/common/common_footer.jspf" %>