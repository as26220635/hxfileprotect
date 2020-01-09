package cn.kim.service.util;

import cn.kim.common.attr.TableViewName;
import cn.kim.entity.ActiveUser;
import cn.kim.service.impl.BaseServiceImpl;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by 余庚鑫 on 2018/6/11
 * 自定义过滤
 */
@Component
public class GridDataFilter extends BaseServiceImpl {
    /**
     * 获取自定义过滤where条件
     *
     * @return
     */
    public String filterWhereSql(Map<String, Object> configure, Map<String, Object> requestMap) {
        StringBuilder resultBuilder = new StringBuilder();
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(10);

        ActiveUser activeUser = getActiveUser();
        //用户类型
        String operatorType = activeUser.getType();
        //用户ID
        String operatorId = activeUser.getId();
        //用户类型关联ID
        String tableId = activeUser.getTableId();

        String configureView = toString(configure.get("SC_VIEW")).toLowerCase();

        if (TableViewName.V_TEST_PROCESS.equals(configureView)) {
            //测试流程
            resultBuilder.append(" AND SO_ID = " + operatorId);
        } else if (TableViewName.V_LOG_SYSTEM.equalsIgnoreCase(configureView) ||
                TableViewName.V_LOG_USE.equalsIgnoreCase(configureView) ||
                TableViewName.V_LOG_PERSONAL.equalsIgnoreCase(configureView) ||
                TableViewName.V_LOG_SEE.equalsIgnoreCase(configureView) ||
                TableViewName.V_LOG_VALUE_RECORD_DETAIL.equalsIgnoreCase(configureView)) {
            //日志管理 管理员可以看到全部
            if (!containsRole("1")) {
                resultBuilder.append(" AND SO_ID =" + operatorId);
                resultBuilder.append(" AND SO_ID !='1'");
            }
//            if (!SystemEnum.MANAGER.toString().equals(operatorType)) {
//                resultBuilder.append(" AND SO_ID =" + operatorId);
//            }
        }


        return resultBuilder.toString();
    }
}
