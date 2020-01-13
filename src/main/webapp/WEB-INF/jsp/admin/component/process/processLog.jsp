<%--
  Created by IntelliJ IDEA.
  User: 余庚鑫
  Date: 2018/6/21
  Time: 9:12
  To change this template use File | Settings | File Templates.
--%>
<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
    @media (min-width: 991px) {
        #processLogDiv .dataTables_scrollBody{
            overflow-x: hidden !important;
        }
    }
</style>

<div id="processLogDiv">
    <c:choose>
        <c:when test="${SPS_STEP_BRANCH ne ProcessBranch.FIXED.type}">
            <table id="processLogTable"
                   class="table table-bordered table-striped">
                <thead>
                <tr>
                    <th style="min-width: 30px;">序号</th>
                    <th style="min-width: 60px;">分支</th>
                    <th style="min-width: 100px;">流程状态</th>
                    <th style="min-width: 70px;">待办人</th>
                    <th style="min-width: 70px;">待办时间</th>
                    <th style="min-width: 70px;">办理人</th>
                    <th style="min-width: 70px;">办理时间</th>
                    <th style="min-width: 180px;">办理意见</th>
                </tr>
                </thead>
            </table>
        </c:when>
        <c:otherwise>
            <table id="processLogTable"
                   class="table table-bordered table-striped">
                <thead>
                <tr>
                    <th style="min-width: 30px;">序号</th>
                    <th style="min-width: 100px;">流程状态</th>
                    <th style="min-width: 90px;">待办人</th>
                    <th style="min-width: 60px;">待办时间</th>
                    <th style="min-width: 90px;">办理人</th>
                    <th style="min-width: 60px;">办理时间</th>
                    <th style="min-width: 200px;">办理意见</th>
                </tr>
                </thead>
            </table>
        </c:otherwise>
    </c:choose>
</div>

<script>
    var $objTable = $('#processLogTable');

    var $logTable = tableView.init({
        //table对象
        object: $objTable,
        scrollY: '400px',
        scrollX: true,
        paging: false,
        pageLength: -1,
        cache: false,
        info: false,
        modal: true,
        //查询URL链接
        url: '${BASE_URL}${Url.PROCESS_LOG_LIST}',
        createdRow: function (row, data, dataIndex) {
            if (data.SPL_STATUS == STATUS_ERROR) {
                //分支待办
                $(row).addClass('text-blue');
            } else if (data.SPL_TYPE == '${ProcessType.SUBMIT.toString()}') {
                //提交
                $(row).addClass('text-green');
            } else if (data.SPL_TYPE == '${ProcessType.BACK.toString()}') {
                //退回
                $(row).addClass('text-red');
            } else if (data.SPL_TYPE == '${ProcessType.WITHDRAW.toString()}') {
                //撤回
                $(row).addClass('text-yellow');
            }
        },
        //对应上面thead里面的序列
        columns: [
            {
                data: null,//此列不绑定数据源，用来显示序号
                title: '序号',
                width: '25px',
                className: 'text-center dataTable-column-min-width-sort'
            },
            <c:if test="${SPS_STEP_BRANCH ne ProcessBranch.FIXED.type}">
            {
                data: 'SPL_BRANCH',
                title: '分支',
                width: '60px',
                className: 'text-left dataTable-column-min-width'
            },
            </c:if>
            {
                data: 'SPL_PROCESS_STATUS_NAME',
                title: '流程办理状态',
                width: '100px',
                className: 'text-left dataTable-column-min-width'
            },
            {
                data: 'SPL_WAIT_TRANSACTOR',
                title: '待办人',
                width: '70px',
                className: 'text-center dataTable-column-min-width'
            },
            {
                data: 'SPL_WAIT_TIME',
                title: '待办时间',
                width: '60px',
                className: 'text-center dataTable-column-min-width'
            },
            {
                data: 'SPL_TRANSACTOR',
                title: '办理人',
                width: '70px',
                className: 'text-center dataTable-column-min-width'
            },
            {
                data: 'SPL_ENTRY_TIME',
                title: '办理时间',
                width: '60px',
                className: 'text-center dataTable-column-min-width'
            },
            {
                data: 'SPL_OPINION',
                title: '办理意见',
                width: '200px',
                className: 'text-left dataTable-column-min-width'
            },
        ],
        fnInitComplete: function (settings, json) {
            setTimeout(function () {
                $logTable.columns.adjust();
            }, 200);
        },
        <%--搜索额外参数--%>
        searchParams: function (params) {
            params['SPS_ID'] = '${SPS_ID}';
            params['SPD_ID'] = '${SPD_ID}';
            params['SPS_TABLE_ID'] = '${SPS_TABLE_ID}';
            params['SPL_PARENTID'] = '${SPL_PARENTID}';
        },
        <%--列表刷新完回调--%>
        endCallback: function (api) {
            <%--序号--%>
            tableView.orderNumber(api, 0);
        }
    });
</script>