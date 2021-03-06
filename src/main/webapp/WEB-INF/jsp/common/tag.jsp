<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="cn.kim.common.attr.WebConfig" %>
<%@ page import="cn.kim.common.attr.Attribute " %>
<%@ page import="cn.kim.common.attr.AttributePath" %>
<%@ page import="cn.kim.common.attr.TableName " %>
<%@ page import="cn.kim.common.attr.TableViewName" %>
<%@ page import="cn.kim.common.attr.Tips" %>
<%@ page import="cn.kim.common.attr.Url" %>
<%@ page import="cn.kim.common.attr.DictTypeCode" %>
<%@ page import="cn.kim.common.eu.SystemEnum" %>
<%@ page import="cn.kim.common.eu.ProcessStatus" %>
<%@ page import="cn.kim.common.eu.ProcessType" %>
<%@ page import="cn.kim.common.eu.ProcessBranch" %>
<%@ page import="cn.kim.common.tag.FileInput" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://shiro.apache.org/tags" prefix="shiro" %>
<%--自定义--%>
<%@ taglib uri="/WEB-INF/tlds/fns.tld" prefix="fns" %>
<%@ taglib uri="/WEB-INF/tlds/tag.tld" prefix="s" %>
<%--pjax地址--%>
<c:set var="CONTAINER" value="container"/>
<%--URL地址--%>
<c:set var="BASE_URL" value="${pageContext.request.contextPath}/"/>
<%--文件服务器地址--%>
<c:set var="WEBCONFIG_FILE_SERVER_URL" value="${WebConfig.WEBCONFIG_FILE_SERVER_URL}"/>
<%--百度地图（AK）--%>
<c:set var="WEBCONFIG_BAIDU_MAP_AK" value="${WebConfig.WEBCONFIG_BAIDU_MAP_AK}"/>