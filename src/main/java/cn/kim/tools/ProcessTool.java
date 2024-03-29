package cn.kim.tools;

import cn.kim.common.attr.Attribute;
import cn.kim.common.eu.ProcessType;
import cn.kim.exception.CustomException;
import cn.kim.service.ProcessService;
import cn.kim.util.AuthcUtil;
import cn.kim.util.CommonUtil;
import cn.kim.util.TextUtil;
import cn.kim.util.ValidateUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.kim.common.attr.Attribute.COMPLEX_SPLIT;

/**
 * Created by 余庚鑫 on 2018/6/11
 * 流程工具
 */
@Component
@Log4j2
public class ProcessTool {

    @Autowired
    private ProcessService processService;
    private static ProcessTool processTool;

    public void setProcessService(ProcessService processService) {
        this.processService = processService;
    }

    @PostConstruct
    public void init() {
        processTool = this;
        processTool.processService = this.processService;
    }

    /**
     * -1 无 0 提交按钮 1 撤回按钮
     *
     * @param id
     * @param busProcess
     * @param busProcess2
     * @return
     */
    public static Set<String> showDataGridProcessBtn(String id, String busProcess, String busProcess2) throws Exception {
        return Sets.newHashSet(processTool.processService.showDataGridProcessBtn(id, busProcess, busProcess2, "0").split(Attribute.SERVICE_SPLIT));
    }

    /**
     * 获取按钮名称
     *
     * @param type
     * @return
     */
    public static String getProcessButtonTypeName(String type) {
        if (type.equals(ProcessType.SUBMIT.toString())) {
            return "提交";
        } else if (type.equals(ProcessType.BACK.toString())) {
            return "退回";
        } else if (type.equals(ProcessType.WITHDRAW.toString())) {
            return "撤回";
        }
        return "";
    }

    /**
     * 获取流程按钮HTML
     *
     * @param btnTypes
     * @param isTop    是否顶部按钮
     * @return
     */
    public static String getProcessButtonListHtml(String btnTypes, boolean isTop) throws InvalidKeyException {
        StringBuilder builder = new StringBuilder();
        if (!ValidateUtil.isEmpty(btnTypes)) {
            //去重
            Set<String> btnTypeSet = Sets.newLinkedHashSet(Arrays.asList(btnTypes.split(Attribute.SERVICE_SPLIT)));
            for (String btnTypeJoin : btnTypeSet) {
                if (!ValidateUtil.isEmpty(btnTypeJoin)){
                    if (btnTypeJoin.contains(Attribute.COMPLEX_SPLIT)) {
                        String[] btnTypeArray = btnTypeJoin.split(Attribute.COMPLEX_SPLIT);
                        builder.append(getProcessButtonHtml(TextUtil.toInt(btnTypeArray[0]), btnTypeArray[1], isTop));
                    } else {
                        builder.append(getProcessButtonHtml(TextUtil.toInt(btnTypeJoin), null, isTop));
                    }
                }
            }
        }
        return builder.toString();
    }

    /**
     * 获取流程按钮HTML
     *
     * @param btnType
     * @param isTop   是否顶部按钮
     * @return
     */
    public static String getProcessButtonHtml(int btnType, String SPS_ID, boolean isTop) throws InvalidKeyException {
        String cls = !isTop ? "btn-xs" : "";
        if (btnType == ProcessType.SUBMIT.getType()) {
            return "<button type='button' class='btn btn-info " + cls + "' data-schedule='" + (SPS_ID != null ? TextUtil.toString(CommonUtil.idEncrypt(SPS_ID)) : "") + "' id='PROCESS_SUBMIT'><i class='mdi mdi-arrow-up-thick'></i>提交</button>";
        } else if (btnType == ProcessType.BACK.getType()) {
            return "<button type='button' class='btn btn-danger " + cls + "' data-schedule='" + (SPS_ID != null ? TextUtil.toString(CommonUtil.idEncrypt(SPS_ID)) : "") + "' id='PROCESS_BACK'><i class='mdi mdi-arrow-down-thick'></i>退回</button>";
        } else if (btnType == ProcessType.WITHDRAW.getType()) {
            return "<button type='button' class='btn btn-danger " + cls + "' data-schedule='" + (SPS_ID != null ? TextUtil.toString(CommonUtil.idEncrypt(SPS_ID)) : "") + "' id='PROCESS_WITHDRAW'><i class='mdi mdi-arrow-down-thick'></i>撤回</button>";
        }
        return "";
    }

    /**
     * 检测是否同一流程同一状态
     *
     * @param tableIds
     * @return
     */
    public static boolean checkProcessUnified(String[] tableIds) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);

        //上一个流程
        String prevDefinitionId = null;
        //上一个审核状态
        String prevAuditStatus = null;

        for (int i = 0; i < tableIds.length; i++) {
            paramMap.clear();
            paramMap.put("SPS_TABLE_ID", tableIds[i]);
            paramMap.put("SPS_IS_CANCEL", TextUtil.toString(Attribute.STATUS_ERROR));
            paramMap.put("SPS_PARENTID", "0");
            Map<String, Object> schedule = processTool.processService.selectProcessSchedule(paramMap);

            if (i > 0) {
                //有的找到有的没有找到不是同一个流程
                if (ValidateUtil.isEmpty(prevDefinitionId) && !ValidateUtil.isEmpty(schedule)) {
                    return false;
                }
                //是否是同一个流程
                if (!prevDefinitionId.equals(TextUtil.toString(schedule.get("SPD_ID")))) {
                    return false;
                }
                //是否是同一个审核状态
                if (!prevAuditStatus.equals(TextUtil.toString(schedule.get("SPS_AUDIT_STATUS")))) {
                    return false;
                }
            }

            if (!ValidateUtil.isEmpty(schedule)) {
                prevDefinitionId = TextUtil.toString(schedule.get("SPD_ID"));
                prevAuditStatus = TextUtil.toString(schedule.get("SPS_AUDIT_STATUS"));
            }

        }

        return true;
    }

    /**
     * 是否有权限审核
     *
     * @param PROCESS_TYPE 审核类型
     * @param SPS_TABLE_ID 主键
     * @param BUS_PROCESS  流程大类
     * @param BUS_PROCESS2 流程小类
     * @return
     * @throws Exception
     */
    public static boolean checkProcess(int PROCESS_TYPE, String SPS_TABLE_ID, String BUS_PROCESS, String BUS_PROCESS2) throws Exception {
        boolean isCheck = true;

        //判断当前是否拥有审核权限
        Set<String> processBtn = Sets.newHashSet(Arrays.asList(processTool.processService.showDataGridProcessBtn(SPS_TABLE_ID, BUS_PROCESS, BUS_PROCESS2, "0").split(Attribute.SERVICE_SPLIT)));
        if (ValidateUtil.isEmpty(processBtn)) {
            isCheck = false;
        }
        //是否子流程
        boolean isComplex = false;
        for (String s : processBtn) {
            if (!ValidateUtil.isEmpty(s)){
                if (s.contains(COMPLEX_SPLIT)) {
                    isComplex = true;
                    break;
                }
            }
        }
        if (isComplex) {
            processBtn = processBtn.stream().map(s -> s.split(COMPLEX_SPLIT)[0]).collect(Collectors.toSet());
        }

        if (PROCESS_TYPE == ProcessType.SUBMIT.getType()) {
            if (!processBtn.contains(ProcessType.SUBMIT.toString())) {
                isCheck = false;
            }
        } else if (PROCESS_TYPE == ProcessType.BACK.getType()) {
            if (!processBtn.contains(ProcessType.BACK.toString())) {
                isCheck = false;
            }
        } else if (PROCESS_TYPE == ProcessType.WITHDRAW.getType()) {
            if (!processBtn.contains(ProcessType.WITHDRAW.toString())) {
                isCheck = false;
            }
        }

        return isCheck;
    }

    /**
     * 获取流程审核状态
     *
     * @param tableId
     * @param busProcess
     * @param busProcess2
     * @return
     */
    public static String getProcessAuditStatus(String tableId, String busProcess, String busProcess2) {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(3);
        mapParam.put("BUS_PROCESS", busProcess);
        mapParam.put("BUS_PROCESS2", busProcess2);
        Map<String, Object> definition = processTool.processService.selectProcessDefinition(mapParam);
        mapParam.clear();
        mapParam.put("SPD_ID", definition.get("ID"));
        mapParam.put("SPS_TABLE_ID", tableId);
        mapParam.put("SPS_IS_CANCEL", Attribute.STATUS_ERROR);
        Map<String, Object> schedule = processTool.processService.selectProcessSchedule(mapParam);
        return ValidateUtil.isEmpty(schedule) ? null : TextUtil.toString(schedule.get("SPS_AUDIT_STATUS"));
    }

    /**
     * 当前登录角色是否拥有随时编辑
     *
     * @param busProcess
     * @param busProcess2
     * @return
     */
    public static boolean selectNowActiveProcessStepIsEdit(String busProcess, String busProcess2) {
        return processTool.processService.selectProcessStepIsEdit(AuthcUtil.getCurrentUser().getRoleIds(), busProcess, busProcess2);
    }

    /**
     * 是否拥有随时编辑
     *
     * @param roleId
     * @param busProcess
     * @param busProcess2
     * @return
     */
    public static boolean selectProcessStepIsEdit(String roleId, String busProcess, String busProcess2) {
        return processTool.processService.selectProcessStepIsEdit(roleId, busProcess, busProcess2);
    }

    /**
     * 查询流程名称
     *
     * @param tableId
     * @param busProcess
     * @param busProcess2
     * @return
     */
    public static String selectProcessTableName(String tableId, String busProcess, String busProcess2) {
        return processTool.processService.selectProcessTableName(tableId, busProcess, busProcess2);
    }

    /**
     * 提交流程
     *
     * @param mapParam
     * @return
     */
    public static Map<String, Object> submitProcess(Map<String, Object> mapParam) {
        log.info("提交流程,参数:" + TextUtil.toString(mapParam));
        return processTool.processService.processSubmit(mapParam);
    }

    /**
     * 撤回流程
     *
     * @param mapParam
     * @return
     */
    public static Map<String, Object> withdrawProcess(Map<String, Object> mapParam) {
        log.info("撤回流程,参数:" + TextUtil.toString(mapParam));
        return processTool.processService.processWithdraw(mapParam);
    }

    /**
     * 作废流程
     *
     * @param scheduleId   流程进度ID
     * @param cancelReason 作废原因
     * @return
     */
    public static Map<String, Object> cancelProcess(String scheduleId, String cancelReason) {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(2);
        mapParam.put("ID", scheduleId);
        mapParam.put("SPSC_REASON", cancelReason);

        log.info("作废流程,参数:" + TextUtil.toString(mapParam));
        return processTool.processService.cancelProcessSchedule(mapParam);
    }

}
