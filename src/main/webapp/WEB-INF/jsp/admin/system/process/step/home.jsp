<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script>
    //查询额外参数
    function searchParams(param) {
        param.SPD_ID = '${EXTRA.SPD_ID}';
        param.SPS_PARENTID = '${EXTRA.SPS_PARENTID}';
    }
</script>

<%--通用列表--%>
<%@ include file="/WEB-INF/jsp/admin/component/grid/dataGrid.jsp" %>
<script>
    <c:if test="${EXTRA.SPS_PARENTID eq fns:AESEncode('0')}">
    $('#return').remove();
    </c:if>

    //替换返回事件
    $('.tab-content #return').attr('onclick', '').unbind('click').on('click', function (e) {
        //切换标签
        var param = {
            SPD_ID: '${EXTRA.SPD_ID}',
            SPS_PARENTID: '${EXTRA.PREV_PARENTID}',
            PREV_PARENTID: '${EXTRA.PREVS_PARENTID}',
            PREV_TITLE: '${EXTRA.PREVS_TITLE}',
            TITLE: '${EXTRA.PREV_TITLE}',
        };
        switchTab('${BASE_URL}${fns:getUrlByMenuCode("SYSTEM:PROCESS_STEP")}' + urlEncode(param));
    });

    //添加
    $('#addBtn').on('click', function () {
        ajax.getHtml('${BASE_URL}${Url.PROCESS_STEP_ADD_URL}', {SPD_ID: '${EXTRA.SPD_ID}'}, function (html) {
                model.show({
                    title: '添加流程步骤',
                    content: html,
                    footerModel: model.footerModel.ADMIN,
                    isConfirm: true,
                    confirm: function ($model) {
                        var $form = $('#addAndEditForm');
                        //流程步骤
                        if (!validator.formValidate($form)) {
                            demo.showNotify(ALERT_WARNING, VALIDATE_FAIL);
                            return;
                        }

                        var params = packFormParams($form);
                        params['SPS_PARENTID'] = '${EXTRA.SPS_PARENTID}';

                        ajax.post('${BASE_URL}${Url.PROCESS_STEP_ADD_URL}', params, function (data) {
                            ajaxReturn.data(data, $model, $dataGrid, false);
                        })
                    }
                });
            }
        );
    });

    //修改
    $dataGridTable.find('tbody').on('click', '#edit', function () {
        var data = getRowData(this);
        var id = data.ID;

        ajax.getHtml('${BASE_URL}${Url.PROCESS_STEP_UPDATE_URL}/' + id, {}, function (html) {
                model.show({
                    title: '修改流程步骤',
                    content: html,
                    footerModel: model.footerModel.ADMIN,
                    isConfirm: true,
                    confirm: function ($model) {
                        var $form = $('#addAndEditForm');
                        //流程步骤
                        if (!validator.formValidate($form)) {
                            demo.showNotify(ALERT_WARNING, VALIDATE_FAIL);
                            return;
                        }

                        var params = packFormParams($form);
                        params['SPS_PARENTID'] = '${EXTRA.SPS_PARENTID}';

                        ajax.put('${BASE_URL}${Url.PROCESS_STEP_UPDATE_URL}', params, function (data) {
                            ajaxReturn.data(data, $model, $dataGrid, false);
                        });
                    }
                });
            }
        );
    });

    //步骤
    $dataGridTable.find('tbody').on('click', '#step', function () {
        var data = getRowData(this);
        var param = {
            SPD_ID: '${EXTRA.SPD_ID}',
            SPS_PARENTID: data.ID,
            PREV_PARENTID: '${EXTRA.SPS_PARENTID}',
            PREV_TITLE: '${EXTRA.TITLE}',
            TITLE: data.SPS_NAME,
        };
        //切换标签
        switchTab('${BASE_URL}${fns:getUrlByMenuCode("SYSTEM:PROCESS_STEP")}' + urlEncode(param));
    });

    //删除
    $dataGridTable.find('tbody').on('click', '#del', function () {
        var data = getRowData(this);
        var id = data.ID;

        model.show({
            title: '删除流程步骤',
            content: '是否删除流程步骤:' + data.SPS_NAME,
            class: model.class.DANGER,
            okBtnName: model.btnName.DEL,
            footerModel: model.footerModel.ADMIN,
            isConfirm: true,
            confirm: function ($model) {
                ajax.del('${BASE_URL}${Url.PROCESS_STEP_DELETE_URL}/' + id, {}, function (data) {
                    ajaxReturn.data(data, $model, $dataGrid, false);
                })
            }
        });
    });
</script>