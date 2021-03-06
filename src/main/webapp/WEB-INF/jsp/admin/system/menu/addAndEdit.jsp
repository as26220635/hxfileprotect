<%--
      Created by IntelliJ IDEA.
      User: 余庚鑫
      Date: 2017/3/6
      Time: 19:44
      To change this template use File | Settings | File Templates.
--%>
<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<form id="addAndEditForm">
    <input type="hidden" name="${SUBMIT_TOKEN_NAME}" value="${token}">
    <input type="hidden" name="ID" value="${MENU.ID}">
    <input type="hidden" name="SM_TYPE" value="${SystemEnum.MANAGER.toString()}">
    <div class="form-group has-feedback">
        <label>菜单名称:</label>
        <input type="text" class="form-control" ${fns:validField("SYS_MENU","SM_NAME")}
               value="${MENU.SM_NAME}">
    </div>
    <div class="form-group has-feedback">
        <label>父菜单:</label>
        <s:treeBox custom='${fns:validField("SYS_MENU","SM_PARENTID")}'
                   value="${MENU.SM_PARENTID}" nameValue="${MENU.SM_PARENT_NAME}"
                   notId="${MENU.ID}"
                   url="${BASE_URL}${Url.MENU_TREE_DATA_URL}" title="选择父菜单"></s:treeBox>
    </div>
    <div class="form-group has-feedback">
        <label>配置列表:</label>
        <s:treeBox custom='${fns:validField("SYS_MENU","SC_ID")}'
                   value="${MENU.SC_ID}" nameValue="${MENU.SC_NAME}"
                   url="${BASE_URL}${Url.CONFIGURE_TREE_DATA_URL}" title="选择配置列表"></s:treeBox>
    </div>
    <div class="row">
        <div class="form-group has-feedback form-group-md-6">
            <label>流程大类:</label>
            <s:combobox sdtCode="SYS_PROCESS_TYPE" custom='${fns:validField("SYS_MENU","BUS_PROCESS")}'
                        value="${MENU.BUS_PROCESS}"></s:combobox>
        </div>
        <div class="form-group has-feedback form-group-md-6">
            <label>流程小类:</label>
            <s:combobox sdtCode="SYS_PROCESS_TYPE" custom='${fns:validField("SYS_MENU","BUS_PROCESS2")}'
                        value="${MENU.BUS_PROCESS2}" level="1"></s:combobox>
        </div>
    </div>
    <div class="row">
        <div class="form-group has-feedback form-group-md-6">
            <label>是否叶节点:</label>
            <s:combobox sdtCode="SYS_YES_NO" custom='${fns:validField("SYS_MENU","SM_IS_LEAF")}'
                        value="${MENU.SM_IS_LEAF}" defaultValue="0"></s:combobox>
        </div>
        <div class="form-group has-feedback form-group-md-6">
            <label>是否默认展开所有节点:</label>
            <s:combobox sdtCode="SYS_YES_NO" custom='${fns:validField("SYS_MENU","SM_IS_EXPAND")}'
                        value="${MENU.SM_IS_EXPAND}"></s:combobox>
        </div>
    </div>
    <div class="form-group has-feedback">
        <label>权限编码:</label>
        <input type="text" class="form-control" ${fns:validField("SYS_MENU","SM_CODE")} value="${MENU.SM_CODE}">
    </div>
    <div class="form-group has-feedback">
        <label>URL:</label>
        <input type="text" class="form-control" ${fns:validField("SYS_MENU","SM_URL")} value="${MENU.SM_URL}">
    </div>
    <div class="form-group has-feedback">
        <label>URL参数:</label>
        <input type="text" class="form-control" ${fns:validField("SYS_MENU","SM_URL_PARAMS")}
               value="${MENU.SM_URL_PARAMS}">
    </div>
    <div class="form-group has-feedback">
        <label>图标:</label>
        <s:iconSelect id="SM_CLASSICON" name="SM_CLASSICON" value="${MENU.SM_CLASSICON}"></s:iconSelect>
    </div>
    <div class="form-group has-feedback">
        <label>排序:</label>
        <input type="text" class="form-control" ${fns:validField("SYS_MENU","SM_ORDER")} value="${MENU.SM_ORDER}">
    </div>
</form>

<script>
    initCombobxSelectDisabled('BUS_PROCESS', 'BUS_PROCESS2', '${MENU.BUS_PROCESS2}');

    validator.init({
        //验证表单
        form: $('#addAndEditForm'),
    });

</script>