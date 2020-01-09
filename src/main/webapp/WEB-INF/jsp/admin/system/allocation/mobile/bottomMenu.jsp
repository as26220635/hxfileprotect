<%--
  Created by IntelliJ IDEA.
  User: 余庚鑫
  Date: 2018/5/22
  Time: 23:48
  To change this template use File | Settings | File Templates.
--%>
<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
    .bottom-icon {
        width: 50px;
        height: 50px;
    }
</style>
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
                                        <label>微信baseUrl:</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WECHAT_BASE_URL")}
                                               value="${WECHAT_BASE_URL}">
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>微信scope:</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WECHAT_SCOPE")}
                                               value="${WECHAT_SCOPE}">
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>微信clientId:</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WECHAT_CLIENT_ID")}
                                               value="${WECHAT_CLIENT_ID}">
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>微信clientSecret:</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WECHAT_CLIENT_SECRET")}
                                               value="${WECHAT_CLIENT_SECRET}">
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>微信回调URI:</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "WECHAT_REDIRECT_URI")}
                                               value="${WECHAT_REDIRECT_URI}">
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>公众号用户名:</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "MOBILE_OFFICIAL_USERNAME")}
                                               value="${MOBILE_OFFICIAL_USERNAME}">
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>青春打卡:
                                            <a href="javascript:;" data-id="${fns:AESEncode(1)}"
                                               data-typeCode="MOBILE_BOTTOM_MENU"
                                               data-sdtCode="MOBILE_BOTTOM_MENU">上传图片</a>
                                        </label>
                                        <s:combobox sdtCode="SYS_YES_NO"
                                                    custom='${fns:validField(TableName.SYS_ALLOCATION,"MOBILE_BOTTOM_MENU_CLOCKIN")}'
                                                    value="${MOBILE_BOTTOM_MENU_CLOCKIN}" defaultValue="1"></s:combobox>
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>活动:
                                            <a href="javascript:;" data-id="${fns:AESEncode(2)}"
                                               data-typeCode="MOBILE_BOTTOM_MENU"
                                               data-sdtCode="MOBILE_BOTTOM_MENU">上传图片</a>
                                        </label>
                                        <s:combobox sdtCode="SYS_YES_NO"
                                                    custom='${fns:validField(TableName.SYS_ALLOCATION,"MOBILE_BOTTOM_MENU_ACTIVITY")}'
                                                    value="${MOBILE_BOTTOM_MENU_ACTIVITY}"
                                                    defaultValue="1"></s:combobox>
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>排行榜:
                                            <a href="javascript:;" data-id="${fns:AESEncode(3)}"
                                               data-typeCode="MOBILE_BOTTOM_MENU"
                                               data-sdtCode="MOBILE_BOTTOM_MENU">上传图片</a>
                                        </label>
                                        <s:combobox sdtCode="SYS_YES_NO"
                                                    custom='${fns:validField(TableName.SYS_ALLOCATION,"MOBILE_BOTTOM_MENU_RANK")}'
                                                    value="${MOBILE_BOTTOM_MENU_RANK}" defaultValue="1"></s:combobox>
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>成就墙:
                                            <a href="javascript:;" data-id="${fns:AESEncode(4)}"
                                               data-typeCode="MOBILE_BOTTOM_MENU"
                                               data-sdtCode="MOBILE_BOTTOM_MENU">上传图片</a>
                                        </label>
                                        <s:combobox sdtCode="SYS_YES_NO"
                                                    custom='${fns:validField(TableName.SYS_ALLOCATION,"MOBILE_BOTTOM_MENU_ACHIEVEMENT")}'
                                                    value="${MOBILE_BOTTOM_MENU_ACHIEVEMENT}"
                                                    defaultValue="1"></s:combobox>
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>个人中心:
                                            <a href="javascript:;" data-id="${fns:AESEncode(5)}"
                                               data-typeCode="MOBILE_BOTTOM_MENU"
                                               data-sdtCode="MOBILE_BOTTOM_MENU">上传图片</a>
                                        </label>
                                        <s:combobox sdtCode="SYS_YES_NO"
                                                    custom='${fns:validField(TableName.SYS_ALLOCATION,"MOBILE_BOTTOM_MENU_MY")}'
                                                    value="${MOBILE_BOTTOM_MENU_MY}" defaultValue="1"></s:combobox>
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>打卡是否可以上传图片:</label>
                                        <s:combobox sdtCode="SYS_YES_NO"
                                                    custom='${fns:validField(TableName.SYS_ALLOCATION,"MOBILE_CLOCKIN_UPLOAD_IMG")}'
                                                    value="${MOBILE_CLOCKIN_UPLOAD_IMG}" defaultValue="1"></s:combobox>
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>打卡是否可以上传视频:</label>
                                        <s:combobox sdtCode="SYS_YES_NO"
                                                    custom='${fns:validField(TableName.SYS_ALLOCATION,"MOBILE_CLOCKIN_UPLOAD_VIDEO")}'
                                                    value="${MOBILE_CLOCKIN_UPLOAD_VIDEO}"
                                                    defaultValue="1"></s:combobox>
                                    </div>
                                    <div class="form-group has-feedback">
                                        <label>青春打卡横幅内容:</label>
                                        <input type="text"
                                               class="form-control" ${fns:validField(TableName.SYS_ALLOCATION, "MOBILE_CLOCKIN_BANNER_CONTENT")}
                                               value="${MOBILE_CLOCKIN_BANNER_CONTENT}">
                                    </div>
                                    <div class="row">
                                        <div class="form-group has-feedback form-group-md-6">
                                            <label>点赞开始时间:</label>
                                            <s:datebox type="6" custom='${fns:validField(TableName.SYS_ALLOCATION, "PRAISE_POINT_START_TIME")}'
                                                       value="${PRAISE_POINT_START_TIME}"></s:datebox>
                                        </div>
                                        <div class="form-group has-feedback form-group-md-6">
                                            <label>点赞结束时间:</label>
                                            <s:datebox type="6" custom='${fns:validField(TableName.SYS_ALLOCATION, "PRAISE_POINT_END_TIME")}'
                                                       value="${PRAISE_POINT_END_TIME}"></s:datebox>
                                        </div>
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
    editMenuTitle('前端管理管理');
    //保存
    function save(){
        var $form = $('#addAndEditForm');
        //验证
        if (!validator.formValidate($form)) {
            demo.showNotify(ALERT_WARNING, VALIDATE_FAIL);
            return;
        }
        var params = packFormParams($form);

        ajax.put('${BASE_URL}${Url.MOBILE_BOTTOM_MENU_BASE_URL}', params, function (data) {
            ajaxReturn.data(data, null, null, null);
        })
    }

    $('a[data-typeCode]').on('click', function () {
        var $this = $(this);
        var id = $this.attr('data-id');
        var typeCode = $this.attr('data-typeCode');
        var sdtCode = $this.attr('data-sdtCode');
        var textName = $this.parent().text().replace($this.text(), '');

        ajax.getHtml('${BASE_URL}${Url.FILE_SERVER_UPDATE_URL}/' + id, {
                typeCode: typeCode,
                sdtCode: sdtCode
            }, function (html) {
                model.show({
                    title: '修改图片:' + textName,
                    content: html,
                    footerModel: model.footerModel.ADMIN,
                    cancel: function () {

                    }
                });
            }
        );
    });

    validator.init({
        //验证表单
        form: $('#addAndEditForm'),
    });
</script>