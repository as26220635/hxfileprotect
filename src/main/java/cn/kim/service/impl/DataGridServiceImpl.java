package cn.kim.service.impl;

import cn.kim.common.attr.Attribute;
import cn.kim.common.attr.MagicValue;
import cn.kim.common.eu.NameSpace;
import cn.kim.common.eu.ProcessShowStatus;
import cn.kim.entity.*;
import cn.kim.service.DataGridService;
import cn.kim.service.util.GridDataFilter;
import cn.kim.util.CommonUtil;
import cn.kim.util.DictUtil;
import cn.kim.util.TextUtil;
import com.google.common.collect.Maps;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by 余庚鑫 on 2018/3/26
 */
@Service
public class DataGridServiceImpl extends BaseServiceImpl implements DataGridService {

    /**
     * 自定义过滤
     */
    @Autowired
    private GridDataFilter gridDataFilter;

    @Override
    public Map<String, Object> selectConfigureById(String configureId) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(3);
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
        //查询配置列表
        paramMap.clear();
        paramMap.put("ID", configureId);
        Map<String, Object> configure = baseDao.selectOne(NameSpace.ConfigureMapper, "selectConfigure", paramMap);
        //查询字段
        paramMap.clear();
        paramMap.put("SC_ID", configureId);
        List<Map<String, Object>> columnList = baseDao.selectList(NameSpace.ConfigureMapper, "selectConfigureColumn", paramMap);
        //查询搜索
        paramMap.clear();
        paramMap.put("SC_ID", configureId);
        List<Map<String, Object>> searchList = baseDao.selectList(NameSpace.ConfigureMapper, "selectConfigureSearch", paramMap);
        //查询文件
        paramMap.clear();
        paramMap.put("SC_ID", configureId);
        paramMap.put("IS_STATUS", STATUS_SUCCESS);
        List<Map<String, Object>> fileList = baseDao.selectList(NameSpace.ConfigureMapper, "selectConfigureFile", paramMap);

        resultMap.put("configure", configure);
        resultMap.put("columnList", columnList);
        resultMap.put("searchList", searchList);
        resultMap.put("fileList", fileList);
        return resultMap;
    }

    @Override
    public String selectProcessStepGroupByStatus(String busProcess, String busProcess2) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.put("BUS_PROCESS", busProcess);
        paramMap.put("BUS_PROCESS2", busProcess2);
        return baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessStepGroupByStatus", paramMap);
    }

    @Override
    public DataTablesView<Map<String, Object>> selectByMap(Map<String, Object> mapParam) throws Exception {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(10);
        String configureId = toString(mapParam.get("ID"));
        //查询按钮
        paramMap.clear();
        paramMap.put("ID", mapParam.get("SM_ID"));
        Map<String, Object> menu = baseDao.selectOne(NameSpace.MenuMapper, "selectMenu", paramMap);
        //查询配置列表
        Map<String, Object> configureMap = this.selectConfigureById(configureId);
        //配置列表
        Map<String, Object> configure = (Map<String, Object>) configureMap.get("configure");
        //查询字段
        List<Map<String, Object>> columnList = (List<Map<String, Object>>) configureMap.get("columnList");
        //查询搜索字段
        List<Map<String, Object>> searchList = (List<Map<String, Object>>) configureMap.get("searchList");

        DataTablesView<Map<String, Object>> dataTablesView = new DataTablesView<>();
        QuerySet querySet = new QuerySet();
        //拿到查询符号
        DictType methods = DictUtil.getDictType("SYS_SEARCH_METHOD");
        //list转为map查询
        Map<String, String> methodsMap = methods.getInfos().stream().collect(Collectors.toMap(DictInfo::getSdiCode, DictInfo::getSdiInnercode));
        //设置查询条件
        if (!isEmpty(searchList)) {
            searchList.forEach(search -> {
                String field = toString(search.get("SCS_FIELD"));
                if (mapParam.containsKey(field) && !isEmpty(mapParam.get(field))) {
                    //设置查询条件
                    querySet.set(methodsMap.get(toString(search.get("SCS_METHOD_TYPE"))), field, toString(mapParam.get(field)));
                }
            });
        }

        //设置流程
        String processStatus = toString(mapParam.get("processStatus"));
        setProcessWhere(querySet, menu, configure, processStatus);
        boolean isProcess = querySet.isProcess();

        //是否开启自定义过滤
        if (!isProcess && toString(configure.get("SC_IS_FILTER")).equals(toString(STATUS_SUCCESS))) {
            querySet.setWhere(gridDataFilter.filterWhereSql(configure, mapParam));
        }

        int offset = toInt(mapParam.get("start"));
        int limit = toInt(mapParam.get("length"));

        querySet.setView(toString(configure.get("SC_VIEW")));
        if (limit != -1) {
            querySet.setOffset(offset);
            querySet.setLimit(limit);
        }
        querySet.setOrderByClause(toString(configure.get("SC_ORDER_BY")));

//        System.out.println(querySet.getWhereMap());
        long count = baseDao.selectOne(NameSpace.DataGridMapper, "countByMap", querySet.getWhereMap());
        dataTablesView.setRecordsTotal(count);
        if (limit != -1) {
            dataTablesView.setTotalPages(CommonUtil.getPage(count, limit));
        }

        List<Map<String, Object>> dataList = baseDao.selectList(NameSpace.DataGridMapper, "selectByMap", querySet.getWhereMap());
        //字典格式化参数
        if (!isEmpty(columnList)) {
            columnList.forEach(column -> {
                String columnSdtCode = toString(column.get("SCC_SDT_CODE"));
                String columnField = toString(column.get("SCC_FIELD"));
                if (!isEmpty(columnSdtCode)) {
                    dataList.forEach(data -> {
                        if (data.containsKey(columnField)) {
                            //查询字典设置格式化值
                            data.put(columnField, DictUtil.getDictName(columnSdtCode, toString(data.get(columnField))));
                        }
                    });
                }
            });
            //格式化状态
            boolean STATUS_FORMAT = toBoolean(mapParam.get("STATUS_FORMAT"));
            if (STATUS_FORMAT) {
                columnList.forEach(column -> {
                    String columnField = toString(column.get("SCC_FIELD"));
                    String SCC_IS_STATUS = toString(column.get("SCC_IS_STATUS"));
                    if (toString(STATUS_SUCCESS).equals(SCC_IS_STATUS)) {
                        dataList.forEach(data -> {
                            data.put(columnField, DictUtil.getDictName("SYS_YES_NO", toString(data.get(columnField))));
                        });
                    }
                });
            }
        }
        dataTablesView.setData(dataList);

        return dataTablesView;
    }
}
