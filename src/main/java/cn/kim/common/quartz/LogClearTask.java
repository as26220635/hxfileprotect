package cn.kim.common.quartz;

import cn.kim.common.BaseData;
import cn.kim.common.attr.WebConfig;
import cn.kim.service.LogService;
import cn.kim.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by 余庚鑫 on 2020/1/9
 * 日志清除
 */
@Component
public class LogClearTask extends BaseData {

    @Autowired
    private LogService logService;

    /**
     * 每天一次
     */
    @Scheduled(cron = "0 0 0 1/1 * ?")
    public void clearLog() {
        try {
            Date LESS_DATE = DateUtil.moveDate(Calendar.DAY_OF_MONTH, false, DateUtil.getTodayDate(), WebConfig.WEBCONFIG_LOG_RETAIN_TIME);
            logService.clearLog(DateUtil.getDate(DateUtil.FORMAT, LESS_DATE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
