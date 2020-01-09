<%--
  Created by IntelliJ IDEA.
  User: 余庚鑫
  Date: 2018/6/7
  Time: 16:29
  To change this template use File | Settings | File Templates.
--%>
<%@ include file="/WEB-INF/jsp/common/tag.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<section class="content">
    <div class="row">
        <div class="col-xs-12">
            <div class="box" id="boxContentDiv">
                <c:if test="${not empty EXTRA.SM_PARENTID}">
                    <div class="box-header">
                        <div class="col-md-12 btn-group-header">
                            <s:button smId="${MENU.ID}"></s:button>
                        </div>
                    </div>
                </c:if>
                <div class="box-body">
                    <div class="nav-tabs-custom">
                        <ul class="nav nav-tabs">
                            <c:forEach items="${SFD_LIST}" var="SFD" varStatus="status">
                                <li class="<c:if test="${status.index == 0}">active</c:if>">
                                    <a id="${SFD.ID}" href="#${SFD.ID}CONTENT" data-toggle="tab"
                                       onclick="switchTab('${BASE_URL}${SFD.SM_URL}','${SFD.SFD_NAME}')">${SFD.SFD_NAME}</a>
                                </li>
                            </c:forEach>
                        </ul>
                        <div class="tab-content">
                            <div class="" id="tabContent">
                            </div>
                            <c:forEach items="${SFD_LIST}" var="SFD">
                                <div class="tab-pane" id="${SFD.ID}CONTENT">
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<script>
    setTimeout(function () {
        <c:choose>
        <c:when test="${empty EXTRA.SM_PARENTID}">
        editMenuTitle('${EXTRA.TITLE}');
        </c:when>
        <c:otherwise>
        setMenuActive('admin-dataGrid-${MENU.SM_PARENTID}');
        editMenuTitle('${EXTRA.TITLE}-' + getMenuTitle());
        </c:otherwise>
        </c:choose>
    }, 50);

    <%--默认显示第一个--%>
    <c:if test="${SFD_LIST.size() > 0}">
    $('#${SFD_LIST.get(0).ID}').click();
    </c:if>
    <%--切换标签--%>

    function switchTab(url, SFD_NAME) {
        showLoadingContentDiv();
        ajax.getHtml(url, {'X-PJAX': true, SFD_NAME: SFD_NAME}, function (html) {
            $('#tabContent').html(html);
            removeLoadingDiv();
        });
    }
</script>