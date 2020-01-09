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
        <c:when test="${v eq TableViewName.V_ACHIEVEMENT}">
        //成就墙
        option = {
            title : {
                text: '${title}',
                subtext: '${subtext}',
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: "{a} <br/>{b} : {c} ({d}%)"
            },
            legend: {
                orient : 'vertical',
                x : 'left',
                data:[${xAxisArray}]
            },
            toolbox: {
                show : true,
                feature : {
                    mark : {show: true},
                    dataView : {show: true, readOnly: false},
                    restore : {show: true},
                    saveAsImage : {show: true}
                }
            },
            calculable : true,
            series : [
                {
                    name:'${seriesName}',
                    type:'pie',
                    radius : '55%',
                    center: ['50%', '50%'],
                    data: [${dataArray}].sort(function (a, b) { return a.value - b.value; }),
                    animationType: 'scale',
                    animationEasing: 'elasticOut',
                    animationDelay: function (idx) {
                        return Math.random() * 200;
                    }
                }
            ]
        };
        </c:when>
        </c:choose>

        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);
    });
</script>