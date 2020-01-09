<%--
  Created by IntelliJ IDEA.
  User: 余庚鑫
  Date: 2018/3/27
  Time: 15:40
  To change this template use File | Settings | File Templates.
--%>
<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<form id="addAndEditForm">
    <input type="hidden" name="${SUBMIT_TOKEN_NAME}" value="${token}">
    <s:fileInput title="附件"
                 tableId="${tableId}"
                 sdtCode="${fns:trueOrFalse(empty reqParam.sdtCode,Attribute.BUS_FILE_DEFAULT, reqParam.sdtCode)}"
                 tableName="${fns:trueOrFalse(empty reqParam.tableName,TableName.SYS_FILE, reqParam.tableName)}"
                 typeCode="${fns:trueOrFalse(empty reqParam.typeCode,TableName.SYS_FILE, reqParam.typeCode)}"
                 maxFilesNum="${fns:trueOrFalse(empty reqParam.maxFilesNum,99, reqParam.maxFilesNum)}"
                 maxFileCount="${fns:trueOrFalse(empty reqParam.maxFileCount,99, reqParam.maxFileCount)}"
                 multiple="${fns:trueOrFalse(empty reqParam.multiple,false, reqParam.multiple)}"
                 showUpload="${fns:trueOrFalse(empty reqParam.showUpload,true, reqParam.showUpload)}"
                 showRemove="${fns:trueOrFalse(empty reqParam.showRemove,true, reqParam.showRemove)}"
    ></s:fileInput>
</form>

<script>
</script>