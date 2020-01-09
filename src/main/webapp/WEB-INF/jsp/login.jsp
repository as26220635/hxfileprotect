<%--
  Created by IntelliJ IDEA.
  User: 余庚鑫
  Date: 2017/4/6
  Time: 20:23
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>
<%@ include file="/WEB-INF/jsp/common/common_config.jsp" %>
<%
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
%>
<html>
<head>
    <%@ include file="/WEB-INF/jsp/common/common_meta.jsp" %>
    <title>${WEBCONFIG_FILE_SERVER_URL}</title>
    <%@ include file="/WEB-INF/jsp/common/common_css.jsp" %>
    <%@ include file="/WEB-INF/jsp/common/common_js.jsp" %>
    <style>
        .form-group .help-block {
            display: block;
        }
    </style>
</head>
<body class="hold-transition login-page">

<div class="login-box">
    <div class="login-logo">
        <a href="../../index2.html"><b>${WEBCONFIG_LOGIN_TITLE}</a>
    </div>
    <!-- /.login-logo -->
    <div class="login-box-body">
        <p class="login-box-msg">登录到${WEBCONFIG_LOGIN_TITLE}</p>

        <form class="form" method="post" action="${BASE_URL}login" id="submitForm">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

            <div class="form-group has-feedback">
                <input type="text" id="username" name="username" class="form-control" placeholder="账号..." minlength="2"
                       maxlength="16" value="${username}" required>
                <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
            </div>
            <div class="form-group has-feedback">
                <input type="password" id="password" name="password" class="form-control" placeholder="密码..."
                       minlength="6" maxlength="16" required>
                <span class="glyphicon glyphicon-lock form-control-feedback"></span>
            </div>
            <div class="row">
                <div class="form-group input-group captcha-input-group">
                    <span class="input-group-addon captcha-input-span" style="border: none;">
                        <img id="validateCode" src="${BASE_URL}check" onclick="refresh()">
                    </span>
                    <input type="text" class="form-control captcha-input" placeholder="验证码..." id="captcha"
                           name="captcha"/>
                </div>
            </div>
            <div class="row">
                <div class="col-xs-12">
                    <button type="submit" class="btn btn-primary btn-block btn-flat">登录</button>
                </div>
            </div>
            <c:if test="${loginError != null}">
                <div class="row" style="text-align: center;color: red;margin-left: 15px;"
                     id="errorTipsAuto">
                    <div class="col-xs-12" style="margin-top: 5px;">
                            ${loginError}
                    </div>
                </div>
            </c:if>
            <div class="row" style="text-align: center;color: red;margin-left: 15px;">
                <div class="col-xs-12" style="margin-top: 5px;display:none;" id="errprTips">
                </div>
            </div>
        </form>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/common/common_bottom.jsp" %>
</body>
<script type="text/javascript">

    NProgress.start();

    document.onreadystatechange = subSomething;//当页面加载状态改变的时候执行这个方法.
    function subSomething() {
        if (document.readyState == 'complete') //当页面加载状态
            NProgress.done();
    }

    var $form = $("#submitForm");

    var loadingId = uuid();

    $form.submit(function (e) {
        loading.show({id: loadingId, tips: '登录中'});
    });

    $('#subBtn').on('click', function (e) {
        validate(e);
    });

    $(document).keypress(function (e) {
        // 回车键事件
        if (e.which == 13) {
            validate(e);
        }
    });

    function validate(e) {
        if ($('#captcha').val().length != 5) {
            demo.showNotify(ALERT_WARNING, '请输入5位验证码!');
            if (e && e.preventDefault) {
                e.preventDefault(); //阻止默认浏览器动作(W3C)
            } else {
                window.event.returnValue = false; //IE中阻止函数器默认动作的方式
            }
            return false;
        }
    }

    // 验证码
    function refresh() {
        NProgress.start();
        var url = '${BASE_URL}check?number=' + Math.random();
        $('#validateCode').prop('src', url).on('load', function () {
            NProgress.done();
        });
    }

    validator.init({
        form: $form,
    }).on('error.form.bv', function (e) {
        loading.hide();
    });
</script>
</html>