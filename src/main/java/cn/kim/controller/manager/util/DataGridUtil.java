package cn.kim.controller.manager.util;

import cn.kim.common.BaseData;
import cn.kim.common.attr.TableViewName;
import cn.kim.service.ProcessService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by 余庚鑫 on 2019/6/14
 * 数据列表设置参数
 */
@Component
public class DataGridUtil extends BaseData {

    @Autowired
    private ProcessService processService;

    /**
     * 配置列表额外添加返回参数
     *
     * @param configureView
     * @param extra
     * @param request
     */
    public void setExtraParams(String configureView, Map<String, Object> extra, HttpServletRequest request) {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(10);

        if (TableViewName.V_PROCESS_STEP.equalsIgnoreCase(configureView)) {
            //流程步骤
            String PREV_PARENTID = toString(extra.get("PREV_PARENTID"));

            if (!isEmpty(PREV_PARENTID) && !"0".equals(PREV_PARENTID)) {
                mapParam.clear();
                mapParam.put("ID", PREV_PARENTID);
                Map<String, Object> step = processService.selectProcessStep(mapParam);

                extra.put("PREVS_PARENTID",step.get("SPS_PARENTID"));
                extra.put("PREVS_TITLE",step.get("SPS_NAME"));
            }
        }
    }
}
