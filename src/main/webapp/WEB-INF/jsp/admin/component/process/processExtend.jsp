<%--
  Created by IntelliJ IDEA.
  User: 余庚鑫
  Date: 2018/6/11
  Time: 23:44
  To change this template use File | Settings | File Templates.
--%>
<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--流程工具--%>
<script>
    var process = {
        /**
         * 显示流程主页
         * @param ID
         * @param SPD_ID
         * @param PROCESS_TYPE 办理类型
         * @param dataGrid 列表
         */
        showProcessHome: throttle(function (option) {
            var okBtnName = model.btnName.SUBMIT;
            if (option.PROCESS_TYPE == '${ProcessType.BACK.toString()}') {
                okBtnName = model.btnName.BACK;
            } else if (option.PROCESS_TYPE == '${ProcessType.WITHDRAW.toString()}') {
                okBtnName = model.btnName.WITHDRAW;
            }

            ajax.getHtml('${BASE_URL}${Url.PROCESS_SHOW_HOME}', {
                ID: option.ID,
                SPS_ID: option.SPS_ID,
                BUS_PROCESS: option.BUS_PROCESS,
                BUS_PROCESS2: option.BUS_PROCESS2,
                PROCESS_TYPE: option.PROCESS_TYPE,
                SHOW_SO_ID: option.SHOW_SO_ID,
            }, function (html) {
                model.show({
                    title: '办理流程',
                    content: html,
                    footerModel: model.footerModel.ADMIN,
                    okBtnName: okBtnName,
                    isConfirm: true,
                    confirm: function ($model) {
                        var $form = $('#processForm');
                        //验证
                        if (!validator.formValidate($form)) {
                            demo.showNotify(ALERT_WARNING, VALIDATE_FAIL);
                            return;
                        }

                        var params = packFormParams($form);
                        var SPS_STEP_BRANCH = params['SPS_STEP_BRANCH'];
                        var SPS_STEP_PARENT_BRANCH = params['SPS_STEP_PARENT_BRANCH'];
                        if (params.IS_DISCONTINUATION == '1') {
                            demo.showNotify(ALERT_WARNING, '流程已经禁用!');
                            return;
                        }
                        if (isEmpty(params.SPS_TABLE_ID)) {
                            demo.showNotify(ALERT_WARNING, '请选择办理流程!');
                            return;
                        }
                        //选中的标题
                        var SPS_TABLE_ID = '';
                        var SPS_TABLE_NAME = '';
                        $("#SPS_TABLE_ID").find("option:selected").each(function () {
                            var $this = $(this);
                            SPS_TABLE_ID += $this.val() + SERVICE_SPLIT;
                            SPS_TABLE_NAME += $this.text() + SERVICE_SPLIT;
                        });
                        params['SPS_TABLE_ID'] = SPS_TABLE_ID;
                        params['SPS_TABLE_NAME'] = SPS_TABLE_NAME;

                        if (!isEmpty(SPS_STEP_BRANCH)) {
                            if (SPS_STEP_BRANCH != '${ProcessBranch.FIXED.toString()}') {
                                var isSelect = true;
                                var SPS_STEP_TRANSACTOR_BRANCH = '';
                                var transactorArray = $('#processForm select[name="SPS_STEP_TRANSACTOR_BRANCH"]');
                                transactorArray.each(function () {
                                    var v = $(this).val();
                                    if (v != '') {
                                        SPS_STEP_TRANSACTOR_BRANCH += v + COMPLEX_SPLIT;
                                    } else {
                                        isSelect = false;
                                    }
                                });
                                if (!isSelect) {
                                    demo.showNotify(ALERT_WARNING, '请选择办理流程!');
                                    return;
                                }
                                params['SPS_STEP_TRANSACTOR_BRANCH'] = SPS_STEP_TRANSACTOR_BRANCH;
                            }
                            if (SPS_STEP_PARENT_BRANCH != '${ProcessBranch.FIXED.toString()}') {
                                var isSelect = true;
                                var SPS_STEP_TRANSACTOR_PARENT_BRANCH = '';
                                var transactorArray = $('#processForm select[name="SPS_STEP_TRANSACTOR_PARENT_BRANCH"]');
                                transactorArray.each(function () {
                                    var v = $(this).val();
                                    if (v != '') {
                                        SPS_STEP_TRANSACTOR_PARENT_BRANCH += v + COMPLEX_SPLIT;
                                    } else {
                                        isSelect = false;
                                    }
                                });
                                if (!isSelect) {
                                    demo.showNotify(ALERT_WARNING, '请选择父流程办理流程!');
                                    return;
                                }
                                params['SPS_STEP_TRANSACTOR_PARENT_BRANCH'] = SPS_STEP_TRANSACTOR_PARENT_BRANCH;
                            }
                        }

                        //弹出确认框
                        model.confirm({
                            message: '是否' + okBtnName + '流程!',
                            callback: function (result) {
                                if (result) {
                                    ajax.put('${BASE_URL}${Url.PROCESS_SUBMIT}', params, function (data) {
                                        ajaxReturn.data(data, $model, option.dataGrid, false);
                                    });
                                }
                            }
                        });
                    }
                });
            });
        }, 0.3),
        /**
         * 流程撤回
         * @param ID
         * @param SPD_ID
         * @param dataGrid 列表
         */
        processWithdraw: throttle(function (option) {
            model.confirm({
                message: '是否撤回流程!',
                callback: function (result) {
                    if (result) {
                        ajax.put('${BASE_URL}${Url.PROCESS_WITHDRAW}', {
                            SPS_TABLE_ID: option.ID,
                            SPS_ID: option.SPS_ID,
                            BUS_PROCESS: option.BUS_PROCESS,
                            BUS_PROCESS2: option.BUS_PROCESS2,
                        }, function (data) {
                            ajaxReturn.data(data, null, option.dataGrid, false);
                        })
                    }
                }
            });
        }, 0.3),
        /**
         * 显示log日志
         */
        processLog: throttle(function (option) {
            ajax.getHtml('${BASE_URL}${Url.PROCESS_LOG}', {
                    ID: option.ID,
                    BUS_PROCESS: option.BUS_PROCESS,
                    BUS_PROCESS2: option.BUS_PROCESS2,
                    SPS_ID: option.SPS_ID
                }, function (html) {
                    model.show({
                        title: '流程日志',
                        content: html,
                        size: model.size.LG,
                        footerModel: model.footerModel.ADMIN,
                    });
                }
            );
        }, 0.3),
    };
</script>