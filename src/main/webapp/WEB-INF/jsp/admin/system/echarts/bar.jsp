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
</style>
<section class="content">
    <div class="row">
        <div class="col-xs-12">
            <div id="${chartId}" style="height:450px"></div>
        </div>
    </div>
</section>

<%--图表--%>
<%--<script src="${BASE_URL}resources/static/plugins/echarts/dist/echarts.min.js?ver=${VERSION}"></script>--%>
<script type="text/javascript">
    $(function () {
        var myChart = echarts.init(document.getElementById('${chartId}'));

        var option = {};

        <c:choose>
        <c:when test="${v eq TableViewName.V_WECHAT}">
        //微信用户
        option = {
            title: {
                text: '${title}',
                subtext: '${subtext}'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data:['${seriesName}']
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataZoom: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            dataZoom: {
                show: true,
                realtime: true,
                start: 0,
                end: 100
            },
            calculable: true,
            xAxis: [
                {
                    type: 'category',
                    data: [${xAxisArray}]
                }
            ],
            yAxis: [
                {
                    type: 'value'
                }
            ],
            series: [
                {
                    name: '${seriesName}',
                    type: 'bar',
                    data: [${dataArray}],
                    markPoint: {
                        data: [
                            {type: 'max', name: '最大值'},
                            {type: 'min', name: '最小值'}
                        ]
                    },
                    markLine: {
                        data: [
                            {type: 'average', name: '平均值'}
                        ]
                    }
                },
            ]
        };
        </c:when>
        </c:choose>

        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);
    });
</script>