<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script>
    //查询额外参数
    function searchParams(param) {
        param.IS_HIDDEN = '0';
    }
</script>
<%--通用列表--%>
<%@ include file="/WEB-INF/jsp/admin/component/grid/dataGrid.jsp" %>
<script>
    //添加
    $('#addBtn').on('click', function () {
        ajax.getHtml('${BASE_URL}${Url.DICT_ADD_URL}', {}, function (html) {
                model.show({
                    title: '添加字典',
                    tips: '下载权限、删除权限作用于附件字典',
                    content: html,
                    footerModel: model.footerModel.ADMIN,
                    isConfirm: true,
                    confirm: function ($model) {
                        var $form = $('#addAndEditForm');
                        //字典
                        if (!validator.formValidate($form)) {
                            demo.showNotify(ALERT_WARNING, VALIDATE_FAIL);
                            return;
                        }

                        var params = packFormParams($form);

                        ajax.post('${BASE_URL}${Url.DICT_ADD_URL}', params, function (data) {
                            ajaxReturn.data(data, $model, $dataGrid, false);
                        })
                    }
                });
            }
        );
    });

    //刷新缓存
    $('#cache').on('click', function () {
        ajax.post('${BASE_URL}${Url.DICT_CACHE_URL}', {}, function (data) {
                ajaxReturn.data(data, null, null, null);
            }
        );
    });

    //修改
    $dataGridTable.find('tbody').on('click', '#edit', function () {
        var data = getRowData(this);
        var id = data.ID;

        ajax.getHtml('${BASE_URL}${Url.DICT_UPDATE_URL}/' + id, {}, function (html) {
                model.show({
                    title: '修改字典',
                    tips: '下载权限、删除权限作用于附件字典',
                    content: html,
                    footerModel: model.footerModel.ADMIN,
                    isConfirm: true,
                    confirm: function ($model) {
                        var $form = $('#addAndEditForm');
                        //字典
                        if (!validator.formValidate($form)) {
                            demo.showNotify(ALERT_WARNING, VALIDATE_FAIL);
                            return;
                        }

                        var params = packFormParams($form);

                        ajax.put('${BASE_URL}${Url.DICT_UPDATE_URL}', params, function (data) {
                            ajaxReturn.data(data, $model, $dataGrid, false);
                        });
                    }
                });
            }
        );
    });

    //设置字段
    $dataGridTable.find('tbody').on('click', '#list', function () {
        var data = getRowData(this);
        var param = {
            SDT_ID: data.ID,
            SDT_NAME: data.SDT_NAME,
            TITLE: data.SDT_NAME,
        };
        //切换主界面
        loadUrl('${BASE_URL}${fns:getUrlByMenuCode("SYSTEM:DICTINFO")}' + urlEncode(param));
    });

    //删除
    $dataGridTable.find('tbody').on('click', '#del', function () {
        var data = getRowData(this);
        var id = data.ID;

        model.show({
            title: '删除字典',
            content: '是否删除字典:' + data.SDT_NAME,
            class: model.class.DANGER,
            okBtnName: model.btnName.DEL,
            footerModel: model.footerModel.ADMIN,
            isConfirm: true,
            confirm: function ($model) {
                ajax.del('${BASE_URL}${Url.DICT_DELETE_URL}/' + id, {}, function (data) {
                    ajaxReturn.data(data, $model, $dataGrid, false);
                })
            }
        });
    });

    //切换状态
    function onSwitchChange($this, field, check, IS_STATUS) {
        showLoadingContentDiv();
        ajax.put('${BASE_URL}${Url.DICT_SWITCH_STATUS_URL}', {ID: $this.val(), IS_STATUS: IS_STATUS}, function (data) {
            if (data.code == STATUS_SUCCESS) {
                demo.showNotify(ALERT_SUCCESS, '状态修改成功!');
            } else {
                $this.bootstrapSwitch('toggleState', true);
                demo.showNotify(ALERT_WARNING, '状态修改失败!');
            }
            removeLoadingDiv();
        });
    }
</script>