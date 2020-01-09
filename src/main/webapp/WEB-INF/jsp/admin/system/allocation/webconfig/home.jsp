<%--
  Created by IntelliJ IDEA.
  User: 余庚鑫
  Date: 2018/5/22
  Time: 23:48
  To change this template use File | Settings | File Templates.
--%>
<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<section class="content">
    <div class="row">
        <div class="col-xs-12">
            <div class="box" id="boxContentDiv">
                <div class="box-header">
                    <div class="col-md-12 btn-group-header">
                        <s:button back="false"></s:button>
                    </div>
                </div>
                <div class="box-body">
                    <div class="col-md-12">
                        <div class="box box-default">
                            <div class="box-body">
                                <form id="addAndEditForm">
                                    <input type="hidden" name="${SUBMIT_TOKEN_NAME}" value="${token}">
                                    <div class="form-group has-feedback">
                                        <label>头标题:</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WEBCONFIG_HEAD_TITLE")}
                                               value="${WEBCONFIG_HEAD_TITLE}">
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>登录标题:</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WEBCONFIG_LOGIN_TITLE")}
                                               value="${WEBCONFIG_LOGIN_TITLE}">
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>后台菜单标题:</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WEBCONFIG_MENU_TITLE")}
                                               value="${WEBCONFIG_MENU_TITLE}">
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>后台菜单小标题:</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WEBCONFIG_MENU_SMALL_TITLE")}
                                               value="${WEBCONFIG_MENU_SMALL_TITLE}">
                                    </div>
<%--                                    <div class="form-group has-feedback">--%>
<%--                                        <label>当前域名:</label>--%>
<%--                                        <input type="text"--%>
<%--                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WEBCONFIG_SERVER_URL")}--%>
<%--                                               value="${WEBCONFIG_SERVER_URL}">--%>
<%--                                    </div>--%>
<%--                                    <div class="form-group has-feedback">--%>
<%--                                        <label>文件服务器地址:</label>--%>
<%--                                        <input type="text"--%>
<%--                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WEBCONFIG_FILE_SERVER_URL")}--%>
<%--                                               value="${WEBCONFIG_FILE_SERVER_URL}">--%>
<%--                                    </div>--%>
                                    <div class="form-group has-feedback">
                                        <label>日志保留时间(天)(默认180天):</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WEBCONFIG_LOG_RETAIN_TIME")}
                                               value="${WEBCONFIG_LOG_RETAIN_TIME}">
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<script>
    editMenuTitle('网站配置');
    //保存
    function save(){
        var $form = $('#addAndEditForm');
        //验证
        if (!validator.formValidate($form)) {
            demo.showNotify(ALERT_WARNING, VALIDATE_FAIL);
            return;
        }
        var params = packFormParams($form);

        ajax.put('${BASE_URL}${Url.WEBCONFIG_BASE_URL}', params, function (data) {
            ajaxReturn.data(data, null, null, null);
        })
    }

    validator.init({
        //验证表单
        form: $('#addAndEditForm'),
    });
</script>