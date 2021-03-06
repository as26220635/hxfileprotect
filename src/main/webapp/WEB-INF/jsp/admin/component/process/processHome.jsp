<%--
  Created by IntelliJ IDEA.
  User: 余庚鑫
  Date: 2018/6/11
  Time: 23:40
  To change this template use File | Settings | File Templates.
--%>
<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--错误提示--%>
<c:if test="${not empty message}">
    <style>
        .process-error {
            z-index: 1000;
            position: absolute;
            height: 95%;
            background: rgba(227, 227, 227, 0.91);
            width: 95%;
            padding-top: 25%;
        }

        @media (max-width: 768px) {
            .process-error {
                width: 92%;
            }
        }
    </style>

    <div class="process-error text-center">
        <h1>${message}</h1>
    </div>
    <script>
        model.closeSubmit($('.process-error'));
    </script>
</c:if>


<form id="processForm">
    <input type="hidden" name="IS_DISCONTINUATION" value="${not empty message ? 1 : 0}">
    <input type="hidden" name="${SUBMIT_TOKEN_NAME}" value="${token}">
    <%--<input type="hidden" name="SPS_TABLE_ID" value="${SPS_TABLE_ID}">--%>
    <input type="hidden" name="SPS_TABLE_NAME" value="${SPS_TABLE_NAME}">
    <input type="hidden" name="BUS_PROCESS" value="${SPD.BUS_PROCESS}">
    <input type="hidden" name="BUS_PROCESS2" value="${SPD.BUS_PROCESS2}">
    <input type="hidden" name="SPS_ID" value="${STEP.ID}">
    <input type="hidden" name="NEXT_SPS_ID" value="${NEXT_STEP.ID}">
    <input type="hidden" name="NOW_SCHEDULE_ID" value="${NOW_SCHEDULE_ID}">
    <input type="hidden" name="SPS_STEP_BRANCH" value="${SPS_STEP_BRANCH}">
    <c:if test="${not empty SHOW_SO_ID}">
        <input type="hidden" name="SHOW_SO_ID" value="${SHOW_SO_ID}">
    </c:if>
    <div class="form-group has-feedback">
        <label>流程名称:</label>
        <span>${SPD.SPD_NAME}</span>
        <span>&nbsp;&nbsp;版本:${SPD.SPD_VERSION}</span>
    </div>
    <div class="form-group has-feedback">
        <label>流程步骤:</label>
        <span>${SPS_GROUP_NAME}</span>
    </div>
    <%--<div class="form-group has-feedback">--%>
    <%--<label>当前执行步骤:</label>--%>
    <%--<span>${STEP_NAME}</span>--%>
    <%--</div>--%>
    <div class="form-group has-feedback">
        <label>办理流程:</label>
        <s:combobox id="SPS_TABLE_ID" name="SPS_TABLE_ID" single="false"
                    dataList="${submitProcessList}">
        </s:combobox>
    </div>
    <div class="form-group has-feedback">
        <label>下一步执行:</label>
        <c:choose>
            <c:when test="${ProcessType.BACK.toString() eq processBtnType}">
                <span style="color: red;">退回</span>
            </c:when>
            <c:when test="${ProcessType.WITHDRAW.toString() eq processBtnType}">
                <span style="color: yellow;">撤回</span>
            </c:when>
            <c:otherwise>
                <span style="color: blue;">提交</span>
            </c:otherwise>
        </c:choose>
        <input type="hidden" ${fns:validField("SYS_PROCESS_SCHEDULE","PROCESS_TYPE")} value="${PROCESS_TYPE}">
    </div>

    <%--办理人--%>
    <c:choose>
        <c:when test="${ProcessType.SUBMIT.toString() eq processBtnType and SPS_STEP_BRANCH ne ProcessBranch.FIXED.toString()}">
            <div class="form-group has-feedback">
                <label>下一步分支办理人:</label>
                <br>
                <c:forEach items="${childrenTransactorList}" var="children" varStatus="s1">
                    <label>分支: ${children.stepTab}</label>
                    <select id="SPS_STEP_TRANSACTOR_BRANCH_${s1.index}" name="SPS_STEP_TRANSACTOR_BRANCH"
                            class="form-control select2" required>
                        <option value="" selected>请选择</option>
                        <c:forEach items="${children.transactorList}" var="TRANSACTOR" varStatus="s2">
                            <option value="${TRANSACTOR.get("joinTransactor")}"
                                    <c:if test="${s2.index == 0 && children.transactorList.size() == 1}">selected</c:if>
                            >${TRANSACTOR.get("VALUE")}</option>
                        </c:forEach>
                    </select>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="form-group has-feedback">
                <label>下一步办理人:</label>
                <select ${fns:validField("SYS_PROCESS_SCHEDULE","SPS_STEP_TRANSACTOR")} class="form-control select2">
                    <option value="" selected>请选择</option>
                    <c:forEach items="${TRANSACTOR_LIST}" var="TRANSACTOR" varStatus="status">
                        <option value="${TRANSACTOR.get("KEY")}"
                                <c:if test="${status.index == 0 && TRANSACTOR_LIST.size() == 1}">selected</c:if>
                        >${TRANSACTOR.get("VALUE")}</option>
                    </c:forEach>
                </select>
            </div>
        </c:otherwise>
    </c:choose>
    <%--父流程参数--%>
    <c:if test="${isSelectParent}">
        <input type="hidden" name="isSelectParent" value="${isSelectParent}">
        <input type="hidden" name="SPS_STEP_PARENT_BRANCH" value="${nextParentBranch}">
        <input type="hidden" name="NEXT_PARENT_SPS_ID" value="${nextParentStep.ID}">
        <input type="hidden" name="PARENT_SPS_ID" value="${parentStepId}">
        <input type="hidden" name="PARENT_SCHEDULE_ID" value="${parentScheduleId}">
        <%--办理人--%>
        <c:choose>
            <c:when test="${nextParentBranch ne ProcessBranch.FIXED.toString()}">
                <div class="form-group has-feedback">
                    <label class="text-red">父流程下一步分支办理人:</label>
                    <br>
                    <c:forEach items="${childrenParentTransactorList}" var="children" varStatus="s1">
                        <label>分支: ${children.stepTab}</label>
                        <select id="SPS_STEP_TRANSACTOR_PARENT_BRANCH_${s1.index}"
                                name="SPS_STEP_TRANSACTOR_PARENT_BRANCH"
                                class="form-control select2" required>
                            <option value="" selected>请选择</option>
                            <c:forEach items="${children.transactorList}" var="TRANSACTOR" varStatus="s2">
                                <option value="${TRANSACTOR.get("joinTransactor")}"
                                        <c:if test="${s2.index == 0 && children.transactorList.size() == 1}">selected</c:if>
                                >${TRANSACTOR.get("VALUE")}</option>
                            </c:forEach>
                        </select>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="form-group has-feedback">
                    <label class="text-red">父流程下一步办理人:</label>
                    <select ${fns:validField("SYS_PROCESS_SCHEDULE","SPS_STEP_PARENT_TRANSACTOR")}
                            class="form-control select2">
                        <option value="" selected>请选择</option>
                        <c:forEach items="${transactorParentList}" var="TRANSACTOR" varStatus="status">
                            <option value="${TRANSACTOR.get("KEY")}"
                                    <c:if test="${status.index == 0 && TRANSACTOR_LIST.size() == 1}">selected</c:if>
                            >${TRANSACTOR.get("VALUE")}</option>
                        </c:forEach>
                    </select>
                </div>
            </c:otherwise>
        </c:choose>
    </c:if>
    <div class="form-group has-feedback">
        <label>办理意见:</label>
        <div>
            <textarea ${fns:validField("SYS_PROCESS_LOG","SPL_OPINION")}
                    class="form-control form-textarea"
                    rows="5">${DEFAULT_OPINION}</textarea>
        </div>
    </div>
</form>

<script>
    $("#processForm .select2").select2({language: "zh-CN"});

    validator.init({
        //验证表单
        form: $('#processForm'),
    });
</script>