package cn.kim.service.impl;

import cn.kim.common.attr.*;
import cn.kim.common.eu.NameSpace;
import cn.kim.common.eu.ProcessBranch;
import cn.kim.common.eu.ProcessStatus;
import cn.kim.common.eu.ProcessType;
import cn.kim.dao.BaseDao;
import cn.kim.entity.ActiveUser;
import cn.kim.entity.ProcessRunBean;
import cn.kim.entity.Tree;
import cn.kim.entity.TreeState;
import cn.kim.exception.CustomException;
import cn.kim.service.OperatorService;
import cn.kim.service.ProcessService;
import cn.kim.service.RoleService;
import cn.kim.service.util.ProcessCheck;
import cn.kim.service.util.ProcessExecute;
import cn.kim.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.security.krb5.internal.PAData;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by 余庚鑫 on 2018/5/22
 */
@Service
public class ProcessServiceImpl extends BaseServiceImpl implements ProcessService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private OperatorService operatorService;

    /**
     * 前进校验
     */
    @Autowired
    private ProcessCheck processCheck;
    /**
     * 前进执行
     */
    @Autowired
    private ProcessExecute processExecute;

    /****   流程    ***/

    /**
     * 查询流程项目名称
     *
     * @param tableId
     * @param busProcess
     * @param busProcess2
     * @return
     */
    @Override
    public String selectProcessTableName(String tableId, String busProcess, String busProcess2) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
        paramMap.put("BUS_PROCESS", busProcess);
        paramMap.put("BUS_PROCESS2", busProcess2);
        Map<String, Object> definition = this.selectProcessDefinition(paramMap);

        paramMap.clear();
        paramMap.put("SPD_TABLE_ID", tableId);
        paramMap.put("SPD_UPDATE_TABLE", definition.get("SPD_UPDATE_TABLE"));
        paramMap.put("SPD_UPDATE_NAME", definition.get("SPD_UPDATE_NAME"));

        return baseDao.selectOne(NameSpace.ProcessMapper, "selectProcessTableName", paramMap);
    }

    /**
     * 拼接按钮
     *
     * @param schedule
     * @param processType
     * @return
     */
    public String joinProcessBtn(Map<String, Object> schedule, ProcessType... processType) {
        StringBuilder builder = new StringBuilder();
        String SPS_PARENTID = toString(schedule.get("SPS_PARENTID"));
        if (!"0".equals(SPS_PARENTID)) {
            for (ProcessType type : processType) {
                builder.append(type.toString() + COMPLEX_SPLIT + toString(schedule.get("ID")) + SERVICE_SPLIT);
            }
        } else {
            for (ProcessType type : processType) {
                builder.append(type.toString() + SERVICE_SPLIT);
            }
        }
        return TextUtil.clearFirstAndLastComma(builder.toString());
    }

    /**
     * -1 无 0 提交按钮 1 退回按钮 2 撤回按钮
     *
     * @param id
     * @param process
     * @param process2
     * @return
     */
    @Override
    public String showDataGridProcessBtn(String id, String process, String process2, String SPS_PARENTID) throws Exception {
        if (isEmpty(id) || isEmpty(process) || isEmpty(process2)) {
            throw new CustomException("参数错误!");
        }
        ActiveUser activeUser = getActiveUser();
        String operatorId = activeUser.getId();

        String resultBtn = "";
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(5);
        paramMap.put("BUS_PROCESS", process);
        paramMap.put("BUS_PROCESS2", process2);
        Map<String, Object> definition = this.selectProcessDefinition(paramMap);
        //流程停用就没有按钮
        if (isEmpty(definition) || isDiscontinuation(definition)) {
            return resultBtn;
        }
        String definitionId = toString(definition.get("ID"));

        //1:查询是否流程记录表拥有记录
        paramMap.clear();
        paramMap.put("SPS_TABLE_ID", id);
        paramMap.put("SPD_ID", definitionId);
        paramMap.put("SPS_IS_CANCEL", toString(STATUS_ERROR));
        paramMap.put("SPS_PARENTID", SPS_PARENTID);
        List<Map<String, Object>> scheduleList = baseDao.selectList(NameSpace.ProcessMapper, "selectProcessSchedule", paramMap);

        if (isEmpty(scheduleList)) {
            //3:查询是否是流程启动角色,是的话返回提交按钮
            List<Map<String, Object>> startRoleList = this.selectProcessStartList(paramMap);
            String roleStr = TextUtil.joinValue(startRoleList, "SR_ID", SERVICE_SPLIT);
            if (!isEmpty(roleStr) && containsRole(roleStr)) {
                resultBtn += ProcessType.SUBMIT.toString();
            }
            return resultBtn;
        }

        for (Map<String, Object> schedule : scheduleList) {
            //2:判断是否进度为空或者状态为启动人员，说明没有启动
            if (!isEmpty(schedule) && toInt(schedule.get("SPS_AUDIT_STATUS")) != ProcessStatus.START.getType()) {
                //3:判断是否已完成
                if (ProcessStatus.COMPLETE.getType() == toInt(schedule.get("SPS_AUDIT_STATUS"))) {
//                    resultBtn = ProcessType.NONE.toString();
                    continue;
                }
                //4:判断进度是不是退回状态
                if (toInt(schedule.get("SPS_AUDIT_STATUS")) == ProcessStatus.BACK.getType()) {
                    if (toString(schedule.get("SPS_STEP_TRANSACTOR")).equals(operatorId)) {
                        //查询当前流程步骤是否存在
                        paramMap.clear();
                        paramMap.put("SPD_ID", definitionId);
                        paramMap.put("SPS_PROCESS_STATUS", toString(schedule.get("SPS_BACK_STATUS_TRANSACTOR")));
                        Map<String, Object> step = this.selectProcessStep(paramMap);
                        if (!isEmpty(step)) {
                            //查询是否是第一个节点
                            paramMap.clear();
                            paramMap.put("SPD_ID", definitionId);
//                        paramMap.put("SR_ID", step.get("SR_ID"));
                            paramMap.put("SPS_PROCESS_STATUS", schedule.get("SPS_BACK_STATUS_TRANSACTOR"));
                            paramMap.put("SPS_PARENTID", SPS_PARENTID);
                            List<Map<String, Object>> prevStepList = this.processPrevStepList(paramMap);
                            //不是启动步骤
                            if (prevStepList.size() > 0 || !"0".equals(SPS_PARENTID)) {
                                resultBtn += joinProcessBtn(schedule, ProcessType.SUBMIT, ProcessType.BACK);
                            } else {
                                resultBtn += joinProcessBtn(schedule, ProcessType.SUBMIT);
                            }
                        }
                    }
                } else {
                    //4:进度不为空查询当前角色或人员是否可以提交当前流程
                    if (MagicValue.ONE.equals(toString(schedule.get("SPS_STEP_TYPE")))) {
                        //当前办理为角色
                        if (containsRole(toString(schedule.get("SPS_STEP_TRANSACTOR")))) {
                            //查询流程步骤是否存在
                            paramMap.clear();
                            paramMap.put("SPD_ID", definitionId);
                            paramMap.put("SR_ID", schedule.get("SPS_STEP_TRANSACTOR"));
                            paramMap.put("SPS_PROCESS_STATUS", schedule.get("SPS_AUDIT_STATUS"));
                            Map<String, Object> step = this.selectProcessStep(paramMap);
                            if (!isEmpty(step)) {
                                resultBtn += joinProcessBtn(schedule, ProcessType.SUBMIT, ProcessType.BACK);
                            }
                        }
                    } else if (MagicValue.TWO.equals(toString(schedule.get("SPS_STEP_TYPE")))) {
                        //当前办理为人员
                        if (toString(schedule.get("SPS_STEP_TRANSACTOR")).equals(operatorId)) {
                            //查询当前流程步骤是否存在
                            paramMap.clear();
                            paramMap.put("SPD_ID", definitionId);
                            paramMap.put("SPS_PROCESS_STATUS", schedule.get("SPS_AUDIT_STATUS"));
                            Map<String, Object> step = this.selectProcessStep(paramMap);
                            if (!isEmpty(step)) {
                                resultBtn += joinProcessBtn(schedule, ProcessType.SUBMIT, ProcessType.BACK);
                            }
                        }
                    }
                }

                //不能是退回状态
                if (toInt(schedule.get("SPS_AUDIT_STATUS")) != -1) {
                    //5:上次办理人是自己切 当前步骤之前只有自己出现一次说明是第一次经过
                    if (toString(schedule.get("SPS_PREV_STEP_TRANSACTOR")).equals(operatorId)) {
//                    //查询上一步骤办理人
//                    paramMap.clear();
//                    paramMap.put("ID", schedule.get("SPS_PREV_STEP_ID"));
//                    Map<String, Object> prevStep = this.selectProcessStep(paramMap);
//                    //查询当前办理角色是否是第一个
//                    paramMap.clear();
//                    paramMap.put("SPD_ID", definitionId);
//                    paramMap.put("SR_ID", prevStep.get("SR_ID"));
//                    paramMap.put("SPS_PROCESS_STATUS", schedule.get("SPS_AUDIT_STATUS"));
//                    List<Map<String, Object>> stepList = this.processPrevStepList(paramMap);
//                    if (stepList.size() == 1) {
                        resultBtn += joinProcessBtn(schedule, ProcessType.WITHDRAW);
//                    }
                    }
                }

            } else {
                //3:查询是否是流程启动角色,是的话返回提交按钮
                List<Map<String, Object>> startRoleList = this.selectProcessStartList(paramMap);
                String roleStr = TextUtil.joinValue(startRoleList, "SR_ID", SERVICE_SPLIT);
                if (!isEmpty(roleStr) && containsRole(roleStr)) {
                    resultBtn += joinProcessBtn(schedule, ProcessType.SUBMIT);
                }
            }

            if (!isEmpty(resultBtn)) {
                return resultBtn;
            }

            //递归查询
            resultBtn += SERVICE_SPLIT + showDataGridProcessBtn(id, process, process2, toString(schedule.get("ID")));

        }

        return resultBtn;
    }

    /**
     * 获取当前项目的下一步步骤
     *
     * @param isSkipBranch        是否跳过分支
     * @param definitionId        流程定义ID
     * @param scheduleAuditStatus 流程办理状态
     * @param stepParentId
     * @return
     */
    @Override
    public Map<String, Object> processNextStep(boolean isSkipBranch, String definitionId, String scheduleAuditStatus, String stepParentId) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(4);
        //查询关联进度表
        paramMap.put("SPD_ID", definitionId);
        paramMap.put("SPS_PROCESS_STATUS", scheduleAuditStatus);
        paramMap.put("SPS_PARENTID", stepParentId);
        if (isSkipBranch) {
            paramMap.put("SPS_STEP_BRANCH", ProcessBranch.FIXED.toString());
        }

        return baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessNextStep", paramMap);
    }

    /**
     * 获取当前项目的上一步步骤
     *
     * @param definitionId        流程定义ID
     * @param scheduleAuditStatus 流程办理状态
     * @param stepParentId
     * @return
     */
    @Override
    @Transactional
    public Map<String, Object> processPrevStep(String definitionId, String scheduleAuditStatus, String stepParentId) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        //查询关联进度表
        paramMap.put("SPD_ID", definitionId);
        paramMap.put("SPS_PROCESS_STATUS", scheduleAuditStatus);
        paramMap.put("SPS_PARENTID", stepParentId);

        return baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessPrevStep", paramMap);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> processPrevStepList(String definitionId, String scheduleAuditStatus, String stepParentId) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        //查询关联进度表
        paramMap.put("SPD_ID", definitionId);
        paramMap.put("SPS_PROCESS_STATUS", scheduleAuditStatus);
        paramMap.put("SPS_PARENTID", stepParentId);
        paramMap.put("ALL", true);

        return baseDao.selectList(NameSpace.ProcessFixedMapper, "selectProcessPrevStep", paramMap);
    }

    /**
     * 根据当前角色ids查询流程定义最高的启动角色
     *
     * @param definitionId
     * @param roleIds
     * @return
     */
    @Override
    public Map<String, Object> processStepStartRoleTop(String definitionId, String roleIds) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        //查询关联进度表
        paramMap.put("SPD_ID", definitionId);
        paramMap.put("SR_IDS", roleIds);

        return baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessStepStartRoleTop", paramMap);
    }

    public List<Map<String, Object>> processPrevStepList(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(5);
        //查询关联进度表
        paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
        paramMap.put("SPS_PROCESS_STATUS", toString(mapParam.get("SPS_PROCESS_STATUS")));
        paramMap.put("SR_ID", mapParam.get("SR_ID"));
        paramMap.put("SPS_PARENTID", mapParam.get("SPS_PARENTID"));
        paramMap.put("ALL", true);

        return baseDao.selectList(NameSpace.ProcessFixedMapper, "selectProcessPrevStep", paramMap);
    }

    /**
     * 根据父流程获取所有子流程
     *
     * @param definitionId
     * @param scheduleTableId
     * @param scheduleId
     * @return
     */
    public List<Map<String, Object>> checkChildrenIsStart(String definitionId, String scheduleTableId, String scheduleId) throws Exception {
        List<Map<String, Object>> stepList = Lists.newLinkedList();

        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
        paramMap.clear();
        paramMap.put("SPD_ID", definitionId);
        paramMap.put("SPS_TABLE_ID", scheduleTableId);
        paramMap.put("SPS_PARENTID", scheduleId);
        List<Map<String, Object>> childrenScheduleList = baseDao.selectList(NameSpace.ProcessMapper, "selectProcessSchedule", paramMap);

        for (Map<String, Object> childrenSchedule : childrenScheduleList) {
            paramMap.clear();
            paramMap.put("ID", childrenSchedule.get("SPS_ID"));
            Map<String, Object> step = this.selectProcessStep(paramMap);

            paramMap.clear();
            paramMap.put("SPD_ID", childrenSchedule.get("SPD_ID"));
            paramMap.put("SPS_PARENTID", step.get("SPS_PARENTID"));
            Map<String, Object> firstStep = this.selectProcessStepList(paramMap).get(0);
            stepList.add(firstStep);

            int nowAuditStatus = toInt(childrenSchedule.get("SPS_AUDIT_STATUS"));
            int nowStepStatus = toInt(step.get("SPS_PROCESS_STATUS"));
            int firstStepStatus = toInt(firstStep.get("SPS_PROCESS_STATUS"));

            if (nowAuditStatus != firstStepStatus) {
                if (ProcessStatus.BACK.getType() != nowAuditStatus || nowStepStatus != firstStepStatus) {
                    throw new CustomException("流程验证失败,分支流程已经启动!");
                }
            }
            stepList.addAll(this.checkChildrenIsStart(definitionId, scheduleId, toString(childrenSchedule.get("ID"))));
        }

        return stepList;
    }

    /**
     * 删除子流程
     *
     * @param definitionId
     * @param scheduleTableId
     * @param scheduleId
     * @throws Exception
     */
    public void deleteChildrenSchedule(String definitionId, String scheduleTableId, String scheduleId) throws Exception {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
        paramMap.put("SPD_ID", definitionId);
        paramMap.put("SPS_TABLE_ID", scheduleTableId);
        paramMap.put("SPS_PARENTID", scheduleId);
        List<Map<String, Object>> childrenScheduleList = baseDao.selectList(NameSpace.ProcessMapper, "selectProcessSchedule", paramMap);

        //验证是否可以退回
        this.checkChildrenIsStart(definitionId, scheduleTableId, scheduleId);

        //删除子流程
        Map<String, Object> resultIUMap = null;
        for (Map<String, Object> childrenSchedule : childrenScheduleList) {
//            paramMap.clear();
//            paramMap.put("SPS_ID", childrenSchedule.get("ID"));
//            paramMap.put("SPL_IS_DEFAULT", toString(STATUS_SUCCESS));
//            baseDao.delete(NameSpace.ProcessMapper, "deleteProcessLog", paramMap);

            paramMap.clear();
            paramMap.put("ID", childrenSchedule.get("ID"));
            resultIUMap = this.deleteProcessSchedule(paramMap);
            validateResultMap(resultIUMap);
            //递归删除子流程
            deleteChildrenSchedule(definitionId, scheduleTableId, toString(childrenSchedule.get("ID")));
        }
    }

    /**
     * 递归删除子流程
     *
     * @param definitionId
     * @param scheduleTableId
     * @param scheduleId
     * @throws Exception
     */
    public void deleteScheduleNotCheck(String definitionId, String scheduleTableId, String scheduleId) throws Exception {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
        Map<String, Object> resultIUMap = null;

        //删除自身
        paramMap.clear();
        paramMap.put("ID", scheduleId);
        resultIUMap = this.deleteProcessSchedule(paramMap);
        validateResultMap(resultIUMap);

        //删除子流程
        paramMap.clear();
        paramMap.put("SPD_ID", definitionId);
        paramMap.put("SPS_TABLE_ID", scheduleTableId);
        paramMap.put("SPS_PARENTID", scheduleId);
        List<Map<String, Object>> childrenScheduleList = baseDao.selectList(NameSpace.ProcessMapper, "selectProcessSchedule", paramMap);

        //删除子流程
        for (Map<String, Object> childrenSchedule : childrenScheduleList) {
            paramMap.clear();
            paramMap.put("ID", childrenSchedule.get("ID"));
            resultIUMap = this.deleteProcessSchedule(paramMap);
            validateResultMap(resultIUMap);

            deleteScheduleNotCheck(definitionId, scheduleTableId, toString(childrenSchedule.get("ID")));
        }
    }

    /**
     * 查询日志父类
     *
     * @param scheduleId
     * @return
     */
    public Map<String, Object> selectBranchParentLog(String scheduleId) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.clear();
        paramMap.put("ID", scheduleId);
        Map<String, Object> schedule = this.selectProcessSchedule(paramMap);
        //更新父流程为完成
        paramMap.clear();
        paramMap.put("SPS_ID", scheduleId);
        paramMap.put("SPL_PROCESS_STATUS", schedule.get("SPS_AUDIT_STATUS"));
        paramMap.put("SPL_STATUS", toString(STATUS_ERROR));
        return this.selectProcessLog(paramMap);
    }

    /**
     * 更新父日志状态
     *
     * @param scheduleId
     * @param SPL_STATUS
     * @return
     */
    public Map<String, Object> updateBranchParentLogStatus(String scheduleId, int SPL_STATUS) {
        Map<String, Object> branchLog = this.selectBranchParentLog(scheduleId);

        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.put("ID", branchLog.get("ID"));
        paramMap.put("SPL_STATUS", SPL_STATUS);
        return this.insertProcessLog(paramMap);
    }

    /**
     * 提交流程
     *
     * @param mapParam
     * @return
     */
    @Override
    @Transactional
    public Map<String, Object> processSubmit(Map<String, Object> mapParam) {
        ActiveUser activeUser = getActiveUser();
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        Map<String, Object> resultIUMap = null;

        int status = STATUS_ERROR;
        String desc = Tips.PROCESS_ERROR;
        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(8);

            //查询流程
            paramMap.put("BUS_PROCESS", mapParam.get("BUS_PROCESS"));
            paramMap.put("BUS_PROCESS2", mapParam.get("BUS_PROCESS2"));
            Map<String, Object> definition = this.selectProcessDefinition(paramMap);
            //流程停用就没有按钮
            if (isDiscontinuation(definition)) {
                throw new CustomException("流程已经停用!");
            }

            //流程类型
            int stepBranch = toInt(mapParam.get("SPS_STEP_BRANCH"), ProcessBranch.FIXED.getType());
            //流程办理类型
            String processType = toString(mapParam.get("PROCESS_TYPE"));
            //流程定义ID
            String definitionId = toString(definition.get("ID"));
            //流程办理ID
            String scheduleTableId = toString(mapParam.get("SPS_TABLE_ID"));
            //流程办理表名称
            String scheduleTableName = toString(mapParam.get("SPS_TABLE_NAME"));
            //流程父类ID
            String scheduleParentId = toString(mapParam.get("SPS_PARENTID"), "0");
            //流程当前步骤办理ID
            String stepId = toString(mapParam.get("SPS_ID"));
            //流程下一步骤办理ID
            String nextStepId = toString(mapParam.get("NEXT_SPS_ID"));
            //下一步骤办理人
            String scheduleStepTransactor = toString(mapParam.get("SPS_STEP_TRANSACTOR"));
            String scheduleStepTransactorBranch = toString(mapParam.get("SPS_STEP_TRANSACTOR_BRANCH"));
            //办理意见
            String logOpinion = toString(mapParam.get("SPL_OPINION"));
            String logParentId = toString(mapParam.get("SPL_PARENTID"));
            int logIsDefault = toInt(mapParam.get("SPL_IS_DEFAULT"), 0);
            //父流程ID
            String nowScheduleId = toString(mapParam.get("NOW_SCHEDULE_ID"));

            if (isEmpty(scheduleTableId) || isEmpty(definitionId) || isEmpty(processType)) {
                throw new CustomException("参数错误!");
            }

            //子流程提交
            if (!isEmpty(nowScheduleId)) {
                //设置参数
                paramMap.clear();
                paramMap.put("ID", nowScheduleId);
                Map<String, Object> schedule = this.selectProcessSchedule(paramMap);

                String nowScheduleParentId = toString(schedule.get("SPS_PARENTID"));

                if (!"0".equals(nowScheduleParentId)) {
                    scheduleParentId = nowScheduleParentId;
                    //查询父日志ID
                    Map<String, Object> logParent = selectBranchParentLog(nowScheduleParentId);

                    logParentId = toString(logParent.get("ID"));

                    if (processType.equals(ProcessType.SUBMIT.toString())) {
                        //提交
                    } else if (processType.equals(ProcessType.BACK.toString())) {
                        //退回
                        //判断上一步是不是主流程
                        paramMap.clear();
                        paramMap.put("ID", nextStepId);
                        Map<String, Object> prevStep = this.selectProcessStep(paramMap);

                        paramMap.clear();
                        paramMap.put("ID", stepId);
                        Map<String, Object> nowStep = this.selectProcessStep(paramMap);

                        if (!toString(prevStep.get("SPS_PARENTID")).equals(toString(nowStep.get("SPS_PARENTID")))) {
                            //模拟退回
                            paramMap.clear();
                            paramMap.put("ID", nowScheduleParentId);
                            Map<String, Object> scheduleParent = this.selectProcessSchedule(paramMap);

                            //删除子流程
                            this.deleteChildrenSchedule(definitionId, scheduleTableId, toString(scheduleParent.get("ID")));
                            //更新日志状态
                            resultIUMap = this.updateBranchParentLogStatus(nowScheduleParentId, STATUS_SUCCESS);
                            validateResultMap(resultIUMap);

                            //退回父流程
                            Map<String, Object> copyMap = Maps.newHashMap(mapParam);
                            copyMap.put("SPS_PARENTID", scheduleParent.get("SPS_PARENTID"));
                            copyMap.put("SPS_ID", scheduleParent.get("SPS_ID"));
                            copyMap.put("SPS_STEP_BRANCH", ProcessBranch.FIXED.getType());
                            copyMap.remove("NOW_SCHEDULE_ID");
                            resultIUMap = this.processSubmit(copyMap);
                            validateResultMap(resultIUMap);

                            return resultIUMap;
                        }
                    }
                }
            }

            //错误提示信息
            String error = "";
            Map<String, Object> executeMap = Maps.newHashMapWithExpectedSize(5);
            //流程当前办理状态
            String scheduleAuditStatus = "0";
            //流程当前办理步骤
            String scheduleStepType = "0";
            //流程退回状态
            String scheduleBackStatus = "0";
            //退回到的审核状态
            String scheduleBackStatusTransactor = "-1";
            //查询当前步骤
            paramMap.clear();
            paramMap.put("ID", stepId);
            Map<String, Object> step = this.selectProcessStep(paramMap);
            if (isEmpty(step)) {
                throw new CustomException("流程办理失败,请联系管理员!");
            }
            String stepTab = toString(step.get("SPS_TAB"));

            //提交流程时查询流程办理是否超时
            if (processType.equals(ProcessType.SUBMIT.toString()) && toInt(step.get("SPS_IS_OVER_TIME")) == STATUS_SUCCESS) {
                //超时时间,没有填写默认24小时 1440分钟
                int stepOverTime = isEmpty(step.get("SPS_OVER_TIME")) ? 1440 : toInt(step.get("SPS_OVER_TIME"));
                String nowDateStr = getSqlDate();
                long nowDateTime = DateUtil.getDateTime(DateUtil.FORMAT, nowDateStr).getTime();
                //获取最后可以办理的时间
                Date lastDate = DateUtil.moveMinuteDate(true, DateUtil.getDateTime(DateUtil.FORMAT, nowDateStr), stepOverTime);
                long lastDateTime = lastDate.getTime();

                if (nowDateTime > lastDateTime) {
                    throw new CustomException("超过办理时间,最后办理时间:" + DateUtil.getDate(DateUtil.FORMAT, lastDate) + "!");
                }
            }

            //设置参数
            if (processType.equals(ProcessType.SUBMIT.toString())) {
                //查询下一步骤办理状态
                paramMap.clear();
                paramMap.put("ID", nextStepId);
                Map<String, Object> nextStep = this.selectProcessStep(paramMap);

                scheduleAuditStatus = toString(nextStep.get("SPS_PROCESS_STATUS"));
                scheduleStepType = toString(nextStep.get("SPS_STEP_TYPE"));
            } else if (processType.equals(ProcessType.BACK.toString())) {
                //解析退回办理人
                String[] processHandles = scheduleStepTransactor.split(SERVICE_SPLIT);

                //查询流程步骤，拿到对应的节点
                paramMap.clear();
                paramMap.put("SPD_ID", definitionId);
                paramMap.put("SPS_PROCESS_STATUS", processHandles[0]);
                Map<String, Object> backStep = this.selectProcessStep(paramMap);
                if (isEmpty(backStep)) {
                    throw new CustomException("流程退回失败,请联系管理员!");
                }

                //退回审核状态,人员
                scheduleStepType = "2";
                //退回办理人
                scheduleBackStatusTransactor = processHandles[0];
                scheduleStepTransactor = processHandles[1];
                //退回审核状态
                scheduleAuditStatus = ProcessStatus.BACK.toString();
                scheduleBackStatus = ProcessType.BACK.toString();
                //查询下一步步骤id
                nextStepId = toString(backStep.get("ID"));
            }

            //查询流程进度是否存在
            paramMap.clear();
            paramMap.put("SPD_ID", definitionId);
            paramMap.put("SPS_TABLE_ID", scheduleTableId);
            paramMap.put("SPS_IS_CANCEL", toString(STATUS_ERROR));
            paramMap.put("SPS_PARENTID", scheduleParentId);
            paramMap.put("SPS_ID", stepId);
            Map<String, Object> schedule = this.selectProcessSchedule(paramMap);

            //退回中的步骤
            List<Map<String, Object>> backStepList = null;
            if (processType.equals(ProcessType.BACK.toString())) {
                paramMap.clear();
                paramMap.put("SPD_ID", definitionId);
                paramMap.put("SPS_PARENTID", step.get("SPS_PARENTID"));
                paramMap.put("START_PROCESS_STATUS", scheduleBackStatusTransactor);
                paramMap.put("END_PROCESS_STATUS", schedule.get("SPS_AUDIT_STATUS"));
                backStepList = baseDao.selectList(NameSpace.ProcessFixedMapper, "selectProcessPrevStepList", paramMap);
            }

            //流程运行传递参数
            ProcessRunBean processRunBean = new ProcessRunBean();
            processRunBean.setBaseDao(baseDao);
            processRunBean.setDefinitionId(definitionId);
            processRunBean.setBusTableId(scheduleTableId);
            processRunBean.setBusProcess(toString(definition.get("BUS_PROCESS")));
            processRunBean.setBusProcess2(toString(definition.get("BUS_PROCESS2")));
            processRunBean.setStepTab(stepTab);
            processRunBean.setExecuteMap(executeMap);

            //是否进行前进后退执行或验证
            if (processType.equals(ProcessType.SUBMIT.toString())) {
                //是否前进校验
                if (toInt(step.get("SPS_IS_ADVANCE_CHECK")) == STATUS_SUCCESS) {
                    error = processCheck.advanceCheck(processRunBean);
                }
                //如果有错误就抛出
                if (!isEmpty(error)) {
                    throw new CustomException(error);
                }
            } else if (processType.equals(ProcessType.BACK.toString())) {
                //是否退回校验
                if (toInt(step.get("SPS_IS_RETREAT_CHECK")) == STATUS_SUCCESS) {
                    error = processCheck.retreatCheck(processRunBean);
                }
                //如果有错误就抛出
                if (!isEmpty(error)) {
                    throw new CustomException(error);
                }
                //中间步骤也校验
                for (Map<String, Object> backStep : backStepList) {
                    ProcessRunBean copyBean = new ProcessRunBean();
                    BeanUtil.copyProperties(processRunBean, copyBean);
                    copyBean.setStepTab(toString(backStep.get("SPS_TAB")));
                    if (toInt(step.get("SPS_IS_RETREAT_CHECK")) == STATUS_SUCCESS) {
                        error = processCheck.retreatCheck(copyBean);
                    }
                    //如果有错误就抛出
                    if (!isEmpty(error)) {
                        throw new CustomException(error);
                    }
                }
            }

            //流程进度如果为空就插入
            if (isEmpty(schedule) || ProcessStatus.START.toString().equals(toString(schedule.get("SPS_AUDIT_STATUS")))) {
                if (isEmpty(schedule)) {
                    schedule = Maps.newHashMapWithExpectedSize(16);
                    schedule.put("SPD_ID", definitionId);
                    schedule.put("SPS_TABLE_ID", scheduleTableId);
                    schedule.put("SO_ID", activeUser.getId());
                    schedule.put("SHOW_SO_ID", mapParam.get("SHOW_SO_ID"));
                }
                schedule.put("SPS_PREV_AUDIT_STATUS", ProcessStatus.START.toString());
            } else {
                //判断流程是否重复审核
                if (toInt(schedule.get("SPS_AUDIT_STATUS")) != ProcessStatus.BACK.getType() &&
                        toInt(schedule.get("SPS_PREV_AUDIT_STATUS")) != ProcessStatus.BACK.getType() &&
                        toString(schedule.get("SPS_PREV_AUDIT_STATUS")).equals(scheduleAuditStatus)) {
                    throw new CustomException("流程重复审核!");
                }
                //上一步流程审核状态
                schedule.put("SPS_PREV_AUDIT_STATUS", schedule.get("SPS_AUDIT_STATUS"));
            }
            schedule.put("SPS_PARENTID", scheduleParentId);
            schedule.put("SPS_ID", nextStepId);
            schedule.put("SPS_TABLE_NAME", scheduleTableName);
            schedule.put("SPS_AUDIT_STATUS", scheduleAuditStatus);
            schedule.put("SPS_BACK_STATUS", scheduleBackStatus);
            schedule.put("SPS_BACK_STATUS_TRANSACTOR", scheduleBackStatusTransactor);
            schedule.put("SPS_STEP_TYPE", scheduleStepType);
            schedule.put("SPS_STEP_TRANSACTOR", scheduleStepTransactor);
            //办理类型
            schedule.put("SPS_PREV_STEP_TYPE", processType);
            schedule.put("SPS_PREV_STEP_TRANSACTOR", activeUser.getId());
            schedule.put("SPS_PREV_STEP_ID", stepId);
            schedule.put("SPS_PROCESS_OPERATOR", activeUser.getId());
            resultIUMap = this.insertAndUpdateProcessSchedule(schedule);
            validateResultMap(resultIUMap);

            //流程进度表ID
            String scheduleId = toString(resultIUMap.get("ID"));

            String transactorName = null;
            if (!isEmpty(scheduleStepTransactor)) {
                if ("1".equals(scheduleStepType)) {
                    //角色
                    Map<String, Object> role = selectRoleById(scheduleStepTransactor);
                    if (!isEmpty(role)) {
                        transactorName = toString(role.get("SR_NAME"));
                    }
                } else if ("2".equals(scheduleStepType)) {
                    //人员
                    Map<String, Object> operator = getOperatorById(scheduleStepTransactor);
                    if (!isEmpty(operator)) {
                        transactorName = toString(operator.get("SAI_NAME"));
                    }
                }
            }
            //获取待办日志更新为已办
            paramMap.clear();
            paramMap.put("SPS_ID", scheduleId);
            paramMap.put("SPL_STATUS", toString(STATUS_ERROR));
            paramMap.put("SPL_PARENTID", logParentId);
            Map<String, Object> waitLog = this.selectProcessLog(paramMap);

            if (isEmpty(waitLog)) {
                //插入启动日志
                paramMap.clear();
                paramMap.put("SPL_PARENTID", logParentId);
                paramMap.put("SPD_ID", definitionId);
                paramMap.put("SPS_ID", scheduleId);
                paramMap.put("SPL_STEP_ID", stepId);
                paramMap.put("SPL_TABLE_ID", scheduleTableId);
                paramMap.put("SPL_SO_ID", activeUser.getId());
                paramMap.put("SPL_TRANSACTOR", activeUser.getAccountName());
                paramMap.put("SPL_OPINION", logOpinion);
                paramMap.put("SPL_ENTRY_TIME", getSqlDate());
                paramMap.put("SPL_TYPE", processType);
                paramMap.put("SPL_PROCESS_STATUS", step.get("SPS_PROCESS_STATUS"));
                paramMap.put("SPL_STEP_BRANCH", step.get("SPS_STEP_BRANCH"));
                paramMap.put("SPL_IS_DEFAULT", logIsDefault);
                paramMap.put("SPL_WAIT_STEP_TYPE", "2");
                paramMap.put("SPL_WAIT_ID", activeUser.getId());
                paramMap.put("SPL_WAIT_TRANSACTOR", activeUser.getAccountName());
                paramMap.put("SPL_WAIT_TIME", getSqlDate());
                paramMap.put("SPL_STATUS", STATUS_SUCCESS);
                resultIUMap = this.insertProcessLog(paramMap);
                validateResultMap(resultIUMap);
            } else {
                //更新日志
                paramMap.clear();
                paramMap.put("ID", waitLog.get("ID"));
                paramMap.put("SPL_SO_ID", activeUser.getId());
                paramMap.put("SPL_TRANSACTOR", activeUser.getAccountName());
                paramMap.put("SPL_OPINION", logOpinion);
                paramMap.put("SPL_ENTRY_TIME", getSqlDate());
                paramMap.put("SPL_STATUS", STATUS_SUCCESS);
                paramMap.put("SPL_TYPE", processType);
                resultIUMap = this.insertProcessLog(paramMap);
                validateResultMap(resultIUMap);
            }

            paramMap.clear();
            paramMap.put("ID", nextStepId);
            Map<String, Object> nextStep = this.selectProcessStep(paramMap);

            //插入流程待办日志
            if (!(processType.equals(ProcessType.SUBMIT.toString()) && ProcessStatus.COMPLETE.toString().equals(scheduleAuditStatus))) {
                paramMap.clear();
                paramMap.put("SPL_PARENTID", logParentId);
                paramMap.put("SPD_ID", definitionId);

                paramMap.put("SPS_ID", scheduleId);
                paramMap.put("SPL_STEP_ID", nextStepId);
                paramMap.put("SPL_TABLE_ID", scheduleTableId);
                if (stepBranch != ProcessBranch.FIXED.getType()) {
                    paramMap.put("SPL_OPINION", "分支流程(系统)");
                }
                paramMap.put("SPL_PROCESS_STATUS", nextStep.get("SPS_PROCESS_STATUS"));
                paramMap.put("SPL_STEP_BRANCH", nextStep.get("SPS_STEP_BRANCH"));
                paramMap.put("SPL_STATUS", STATUS_ERROR);
                paramMap.put("SPL_STEP_BRANCH", stepBranch);
                paramMap.put("SPL_WAIT_STEP_TYPE", scheduleStepType);
                paramMap.put("SPL_WAIT_ID", scheduleStepTransactor);
                paramMap.put("SPL_WAIT_TRANSACTOR", transactorName);
                paramMap.put("SPL_WAIT_TIME", getSqlDate());
                resultIUMap = this.insertProcessLog(paramMap);
                validateResultMap(resultIUMap);
            }

            //插入分支或者并发流程
            if (stepBranch != ProcessBranch.FIXED.getType()) {
                if (processType.equals(ProcessType.SUBMIT.toString())) {

                    String SL_ID = toString(resultIUMap.get("ID"));

                    String[] scheduleStepTransactorBranchArray = scheduleStepTransactorBranch.split(Attribute.COMPLEX_SPLIT);
                    for (String transactor : scheduleStepTransactorBranchArray) {
                        transactor = toString(CommonUtil.idDecrypt(transactor));
                        String branchStepId = transactor.split(SERVICE_SPLIT)[0];
                        String branchScheduleStepTransactor = transactor.split(SERVICE_SPLIT)[1];

                        paramMap.clear();
                        paramMap.put("ID", branchStepId);
                        Map<String, Object> branchStep = selectProcessStep(paramMap);

                        Map<String, Object> copyMap = Maps.newHashMap(mapParam);
                        copyMap.put("SPS_PARENTID", scheduleId);
                        copyMap.put("SPL_PARENTID", SL_ID);
                        copyMap.put("SPS_ID", branchStep.get("ID"));
                        copyMap.put("NEXT_SPS_ID", branchStep.get("ID"));
                        copyMap.put("SPS_STEP_BRANCH", ProcessBranch.FIXED.getType());
                        copyMap.put("SPS_STEP_TRANSACTOR", branchScheduleStepTransactor);
                        copyMap.put("SPL_OPINION", Tips.PROCESS_START);
                        copyMap.put("SPL_IS_DEFAULT", STATUS_SUCCESS);
                        copyMap.remove("scheduleStepTransactorBranch");
                        resultIUMap = this.processSubmit(copyMap);

                        validateResultMap(resultIUMap);
                    }
                }
            }

            //流程通过额外插入完成日志
            if (processType.equals(ProcessType.SUBMIT.toString()) && ProcessStatus.COMPLETE.toString().equals(scheduleAuditStatus)) {
                paramMap.clear();
                paramMap.put("SPL_PARENTID", logParentId);
                paramMap.put("SPD_ID", definitionId);
                paramMap.put("SPS_ID", scheduleId);
                paramMap.put("SPL_STEP_ID", nextStepId);
                paramMap.put("SPL_TABLE_ID", scheduleTableId);
                paramMap.put("SPL_SO_ID", activeUser.getId());
                paramMap.put("SPL_TRANSACTOR", activeUser.getUsername());
                paramMap.put("SPL_OPINION", "审核通过(系统)");
                paramMap.put("SPL_ENTRY_TIME", getSqlDate());
                paramMap.put("SPL_TYPE", processType);
                paramMap.put("SPL_PROCESS_STATUS", scheduleAuditStatus);
                paramMap.put("SPL_STATUS", STATUS_SUCCESS);
                paramMap.put("SPL_WAIT_STEP_TYPE", scheduleStepType);
                paramMap.put("SPL_WAIT_ID", activeUser.getId());
                paramMap.put("SPL_WAIT_TRANSACTOR", activeUser.getAccountName());
                paramMap.put("SPL_WAIT_TIME", getSqlDate());

                resultIUMap = this.insertProcessLog(paramMap);
                validateResultMap(resultIUMap);
            }

            //是否进行前进后退执行或验证
            if (processType.equals(ProcessType.SUBMIT.toString())) {
                //是否前进执行
                if (toInt(step.get("SPS_IS_ADVANCE_EXECUTE")) == STATUS_SUCCESS) {
                    error = processExecute.advanceExecute(processRunBean);
                }
            } else if (processType.equals(ProcessType.BACK.toString())) {
                //是否退回执行
                if (toInt(step.get("SPS_IS_RETREAT_EXECUTE")) == STATUS_SUCCESS) {
                    error = processExecute.retreatExecute(processRunBean);
                }
                //中间步骤也执行
                for (Map<String, Object> backStep : backStepList) {
                    ProcessRunBean copyBean = new ProcessRunBean();
                    BeanUtil.copyProperties(processRunBean, copyBean);
                    copyBean.setStepTab(toString(backStep.get("SPS_TAB")));
                    if (toInt(step.get("SPS_IS_RETREAT_CHECK")) == STATUS_SUCCESS) {
                        error += processExecute.retreatExecute(processRunBean);
                    }
                }
            }
            //如果有错误就抛出
            if (!isEmpty(error)) {
                throw new CustomException(error);
            }
            //判断退回中是否有分支流程
            if (processType.equals(ProcessType.BACK.toString())) {
                List<Map<String, Object>> deleteBranchLogList = Lists.newArrayList();
                for (Map<String, Object> backStep : backStepList) {
                    if (toInt(backStep.get("SPS_STEP_BRANCH")) != ProcessBranch.FIXED.getType()) {
                        paramMap.clear();
                        paramMap.put("SPD_ID", definitionId);
                        paramMap.put("SPL_TABLE_ID", scheduleTableId);
                        paramMap.put("SPL_STEP_ID", backStep.get("ID"));
                        List<Map<String, Object>> deleteLogList = this.selectProcessLogList(paramMap);

                        for (Map<String, Object> deleteLog : deleteLogList) {
                            paramMap.clear();
                            paramMap.put("SPL_PARENTID", deleteLog.get("ID"));
                            deleteBranchLogList.addAll(this.selectProcessLogList(paramMap));
                        }

                    }
                }

                if (!isEmpty(deleteBranchLogList)) {
                    Set<String> deleteScheduleParentIdSet = deleteBranchLogList.stream().map(m -> toString(m.get("SPS_ID"))).collect(Collectors.toSet());
                    //递归删除这些流程的子流程
                    for (String deleteScheduleParentId : deleteScheduleParentIdSet) {
                        this.deleteScheduleNotCheck(definitionId, scheduleTableId, deleteScheduleParentId);
                    }
                }
            }

            //父流程是否需要提交
            boolean isSelectParent = toBoolean(mapParam.get("isSelectParent"));
            if (isSelectParent) {
                String stepParentBranch = toString(mapParam.get("SPS_STEP_PARENT_BRANCH"));
                String parentStepId = toString(mapParam.get("PARENT_SPS_ID"));
                String nextParentStepId = toString(mapParam.get("NEXT_PARENT_SPS_ID"));
                String scheduleStepParentTransactor = toString(mapParam.get("SPS_STEP_PARENT_TRANSACTOR"));
                String scheduleStepParentTransactorBranch = toString(mapParam.get("SPS_STEP_TRANSACTOR_PARENT_BRANCH"));
                String nowScheduleParentId = toString(mapParam.get("PARENT_SCHEDULE_ID"));
                //查询父流程
                paramMap.clear();
                paramMap.put("ID", nowScheduleParentId);
                Map<String, Object> scheduleParent = this.selectProcessSchedule(paramMap);
                //查询父日志ID
                Map<String, Object> logParent = this.selectBranchParentLog(nowScheduleParentId);

                logParentId = toString(logParent.get("SPL_PARENTID"));

                //更新状态为完成
                paramMap.clear();
                paramMap.put("ID", logParentId);
                paramMap.put("SPL_ENTRY_TIME", getSqlDate());
                paramMap.put("SPL_STATUS", STATUS_SUCCESS);
                paramMap.put("SPL_TYPE", processType);
                resultIUMap = this.insertProcessLog(paramMap);
                validateResultMap(resultIUMap);

                Map<String, Object> copyMap = Maps.newHashMap(mapParam);
                copyMap.put("NOW_SCHEDULE_ID", nowScheduleParentId);
                copyMap.put("SPS_PARENTID", scheduleParent.get("SPS_PARENTID"));
                copyMap.put("SPL_PARENTID", logParentId);
                copyMap.put("SPS_ID", parentStepId);
                copyMap.put("NEXT_SPS_ID", nextParentStepId);
                copyMap.put("SPS_STEP_BRANCH", stepParentBranch);
                copyMap.put("SPS_STEP_TRANSACTOR", scheduleStepParentTransactor);
                copyMap.put("SPS_STEP_TRANSACTOR_BRANCH", scheduleStepParentTransactorBranch);
                copyMap.put("SPL_OPINION", Tips.PROCESS_BRANCH_END);
                copyMap.remove("isSelectParent");
                resultIUMap = this.processSubmit(copyMap);

                validateResultMap(resultIUMap);
            }

            status = STATUS_SUCCESS;
            desc = Tips.PROCESS_SUCCESS;

        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);

        return resultMap;
    }

    /**
     * 撤回流程
     *
     * @param mapParam
     * @return
     */
    @Override
    @Transactional
    public Map<String, Object> processWithdraw(Map<String, Object> mapParam) {
        ActiveUser activeUser = getActiveUser();
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = Tips.PROCESS_ERROR;

        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
            paramMap.put("BUS_PROCESS", mapParam.get("BUS_PROCESS"));
            paramMap.put("BUS_PROCESS2", mapParam.get("BUS_PROCESS2"));
            Map<String, Object> definition = this.selectProcessDefinition(paramMap);

            //流程定义ID
            String definitionId = toString(definition.get("ID"));
            //流程办理ID
            String scheduleTableId = toString(mapParam.get("SPS_TABLE_ID"));
            //流程ID
            String scheduleId = toString(mapParam.get("SPS_ID"), "0");
            //父类日志ID
            String logParentId = "0";

            if (isEmpty(definitionId) || isEmpty(scheduleTableId)) {
                throw new CustomException("参数错误!");
            }

            Map<String, Object> resultIUMap = null;
            //查询流程进度
            if (!"0".equals(scheduleId)) {
                paramMap.put("ID", scheduleId);
            } else {
                paramMap.put("SPS_PARENTID", scheduleId);
            }
            paramMap.put("SPD_ID", definitionId);
            paramMap.put("SPS_TABLE_ID", scheduleTableId);
            paramMap.put("SPS_IS_CANCEL", toString(STATUS_ERROR));
            Map<String, Object> schedule = this.selectProcessSchedule(paramMap);
            //判断流程是否可以撤回
            if (toInt(schedule.get("SPS_AUDIT_STATUS")) == ProcessStatus.BACK.getType() ||
                    !toString(schedule.get("SPS_PREV_STEP_TRANSACTOR")).equals(activeUser.getId())) {
                throw new CustomException("流程撤回失败,请检查流程状态!");
            }
            String stepId = toString(schedule.get("SPS_ID"));

            //查询当前流程
            paramMap.clear();
            paramMap.put("ID", stepId);
            Map<String, Object> step = this.selectProcessStep(paramMap);

            //判断子流程状态
            if ("0".equals(scheduleId)) {
                int stepBranch = isEmpty(step) ? ProcessBranch.FIXED.getType() : toInt(step.get("SPS_STEP_BRANCH"));
                if (stepBranch != ProcessBranch.FIXED.getType()) {
                    //删除子流程
                    this.deleteChildrenSchedule(definitionId, scheduleTableId, toString(schedule.get("ID")));
                    //更新日志状态
                    resultIUMap = this.updateBranchParentLogStatus(toString(schedule.get("ID")), STATUS_SUCCESS);
                    validateResultMap(resultIUMap);
                }
            } else {
                //查询父日志ID
                Map<String, Object> logParent = selectBranchParentLog(toString(schedule.get("SPS_PARENTID")));

                logParentId = toString(logParent.get("ID"));
            }

            //查询上一步的ID
            paramMap.clear();
            paramMap.put("SPD_ID", definitionId);
            paramMap.put("SPS_PROCESS_STATUS", schedule.get("SPS_AUDIT_STATUS"));
            paramMap.put("SPS_PARENTID", step.get("SPS_PARENTID"));
            Map<String, Object> withdrawStep = this.processPrevStepList(paramMap).get(0);
            //上一步流程审核状态
            schedule.put("SPS_PREV_AUDIT_STATUS", schedule.get("SPS_AUDIT_STATUS"));
            schedule.put("SPS_AUDIT_STATUS", ProcessStatus.BACK.getType());
            schedule.put("SPS_BACK_STATUS", ProcessType.WITHDRAW.toString());
            schedule.put("SPS_BACK_STATUS_TRANSACTOR", withdrawStep.get("SPS_PROCESS_STATUS"));
            schedule.put("SPS_STEP_TYPE", 2);
            schedule.put("SPS_STEP_TRANSACTOR", activeUser.getId());
            //办理类型
            schedule.put("SPS_PREV_STEP_TYPE", ProcessType.WITHDRAW.toString());
            schedule.put("SPS_PREV_STEP_TRANSACTOR", activeUser.getId());
            schedule.put("SPS_PREV_STEP_ID", schedule.get("SPS_ID"));
            schedule.put("SPS_ID", withdrawStep.get("ID"));
            resultIUMap = this.insertAndUpdateProcessSchedule(schedule);
            validateResultMap(resultIUMap);

            //流程进度表ID
            scheduleId = toString(resultIUMap.get("ID"));

            //获取待办日志更新为已办
            paramMap.clear();
            paramMap.put("SPS_ID", scheduleId);
            paramMap.put("SPL_STATUS", toString(STATUS_ERROR));
            paramMap.put("SPL_PARENTID", logParentId);
            Map<String, Object> waitLog = this.selectProcessLog(paramMap);

            //更新日志
            paramMap.clear();
            paramMap.put("ID", waitLog.get("ID"));
            paramMap.put("SPL_SO_ID", activeUser.getId());
            paramMap.put("SPL_TRANSACTOR", activeUser.getAccountName());
            paramMap.put("SPL_OPINION", "用户撤回(系统)");
            paramMap.put("SPL_ENTRY_TIME", getSqlDate());
            paramMap.put("SPL_STATUS", STATUS_SUCCESS);
            paramMap.put("SPL_TYPE", ProcessType.WITHDRAW.toString());
            resultIUMap = this.insertProcessLog(paramMap);
            validateResultMap(resultIUMap);

            //插入流程待办日志
            paramMap.clear();
            paramMap.put("SPD_ID", definitionId);
            paramMap.put("SPS_ID", scheduleId);
            paramMap.put("SPL_PARENTID", logParentId);
            paramMap.put("SPL_STEP_ID", withdrawStep.get("ID"));
            paramMap.put("SPL_TABLE_ID", scheduleTableId);
            paramMap.put("SPL_PROCESS_STATUS", ProcessStatus.BACK.getType());
            paramMap.put("SPL_STATUS", STATUS_ERROR);
            paramMap.put("SPL_WAIT_STEP_TYPE", 2);
            paramMap.put("SPL_WAIT_ID", activeUser.getId());
            paramMap.put("SPL_WAIT_TRANSACTOR", activeUser.getAccountName());
            paramMap.put("SPL_WAIT_TIME", getSqlDate());

            resultIUMap = this.insertProcessLog(paramMap);
            validateResultMap(resultIUMap);


            status = STATUS_SUCCESS;
            desc = Tips.PROCESS_SUCCESS;
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);

        return resultMap;
    }

    /**
     * 获取到下一个固定流程前的所有流程
     *
     * @param list
     * @param step
     * @return
     */
    public List<Map<String, Object>> getProcessAllNextStepFixed(List<Map<String, Object>> list, Map<String, Object> step) {
        List<Map<String, Object>> stepList = Lists.newLinkedList();
        Map<String, Object> nextStep = processNextStep(false, toString(step.get("SPD_ID")), toString(step.get("SPS_PROCESS_STATUS")), toString(step.get("SPS_PARENTID")));
        stepList.add(step);

        int SPS_STEP_BRANCH = isEmpty(nextStep) ? ProcessBranch.FIXED.getType() : toInt(nextStep.get("SPS_STEP_BRANCH"));
        if (SPS_STEP_BRANCH != ProcessBranch.FIXED.getType()) {
            stepList.addAll(getProcessAllNextStepFixed(list, nextStep));
        }
        return stepList;
    }

    /**
     * 获取第一个子步骤
     *
     * @param list
     * @return
     */
    public List<Map<String, Object>> getProcessFirstChildrenStep(List<Map<String, Object>> list) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);

        List<Map<String, Object>> stepList = Lists.newArrayList();
        for (Map<String, Object> step : list) {
            paramMap.clear();
            paramMap.put("SPD_ID", step.get("SPD_ID"));
            paramMap.put("SPS_PARENTID", step.get("ID"));
            stepList.add(selectProcessStepList(paramMap).get(0));
        }
        return stepList;
    }

    @Override
    public Map<String, Object> getProcessTransactor(Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(3);

        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(5);
        List<Map<String, Object>> transactorList = Lists.newArrayList();

        boolean isSkipBranch = toBoolean(mapParam.get("isSkipBranch"));
        int processType = toInt(mapParam.get("processType"), 0);
        String tableId = toString(mapParam.get("tableId"));
        String definitionId = toString(mapParam.get("definitionId"));
        String scheduleId = toString(mapParam.get("scheduleId"));
        String scheduleAuditStatus = toString(mapParam.get("scheduleAuditStatus"));

        paramMap.clear();
        paramMap.put("ID", definitionId);
        Map<String, Object> definition = selectProcessDefinition(paramMap);

        Map<String, Object> schedule = null;
        Map<String, Object> scheduleParent = null;
        if (!isEmpty(scheduleId)) {
            paramMap.clear();
            paramMap.put("ID", scheduleId);
            schedule = selectProcessSchedule(paramMap);
        }

        String SPS_PARENTID = "0";
        if (!isEmpty(schedule)) {
            paramMap.clear();
            paramMap.put("ID", schedule.get("SPS_ID"));
            Map<String, Object> nowStep = selectProcessStep(paramMap);
            SPS_PARENTID = toString(nowStep.get("SPS_PARENTID"));
        }
        String scheduleParentId = !isEmpty(schedule) ? toString(schedule.get("SPS_PARENTID")) : null;

        Map<String, Object> nextStep = null;

        //查询下一步骤
        if (processType == ProcessType.SUBMIT.getType()) {
            //流程为提交
            nextStep = processNextStep(isSkipBranch, definitionId, scheduleAuditStatus, SPS_PARENTID);
            if (isEmpty(nextStep)) {
                throw new CustomException("没有找到下一流程步骤！");
            }

            int SPS_STEP_BRANCH = isEmpty(nextStep) ? ProcessBranch.FIXED.getType() : toInt(nextStep.get("SPS_STEP_BRANCH"));

            String SR_ID = toString(nextStep.get("SR_ID"));
            //下一步是否为结束
            if (toInt(nextStep.get("SPS_PROCESS_STATUS")) != ProcessStatus.COMPLETE.getType()) {

                resultMap.put("SPS_STEP_BRANCH", SPS_STEP_BRANCH);

                if (SPS_STEP_BRANCH == ProcessBranch.FIXED.getType()) {
                    //固定
                    //查询下一步办理人
                    if (MagicValue.ONE.equals(toString(nextStep.get("SPS_STEP_TYPE")))) {
                        //下一步为角色
                        paramMap.clear();
                        paramMap.put("ID", SR_ID);
                        Map<String, Object> role = roleService.selectRole(paramMap);

                        Map<String, Object> transactor = Maps.newHashMapWithExpectedSize(2);
                        transactor.put("KEY", role.get("ID"));
                        transactor.put("VALUE", role.get("SR_NAME"));
                        transactorList.add(transactor);
                    } else if (MagicValue.TWO.equals(toString(nextStep.get("SPS_STEP_TYPE")))) {
                        //下一步为人员
                        paramMap.clear();
                        paramMap.put("ID", nextStep.get("SR_ID"));
                        List<Map<String, Object>> operatorList = operatorService.selectOperatorByRoleId(SR_ID);
                        operatorList.forEach(map -> {
                            Map<String, Object> transactor = Maps.newHashMapWithExpectedSize(2);
                            transactor.put("KEY", map.get("ID"));
                            transactor.put("VALUE", map.get("SAI_NAME"));
                            transactorList.add(transactor);
                        });
                    }
                } else if (SPS_STEP_BRANCH == ProcessBranch.BRANCH.getType() || SPS_STEP_BRANCH == ProcessBranch.CONCURRENT.getType()) {
                    //分支 并发
                    List<Map<String, Object>> childrenStepList = Lists.newArrayList();
                    //获取步骤
                    childrenStepList = getProcessAllNextStepFixed(childrenStepList, nextStep);
                    childrenStepList = getProcessFirstChildrenStep(childrenStepList);

                    List<Map<String, Object>> childrenTransactorList = Lists.newArrayList();
                    for (Map<String, Object> childrenStep : childrenStepList) {
                        Map<String, Object> stepMap = Maps.newHashMapWithExpectedSize(3);
                        List<Map<String, Object>> stepList = Lists.newArrayList();

                        if (MagicValue.ONE.equals(toString(childrenStep.get("SPS_STEP_TYPE")))) {
                            //下一步为角色
                            paramMap.clear();
                            paramMap.put("ID", childrenStep.get("SR_ID"));
                            Map<String, Object> role = roleService.selectRole(paramMap);

                            Map<String, Object> transactor = Maps.newHashMapWithExpectedSize(2);
                            transactor.put("KEY", role.get("ID"));
                            transactor.put("VALUE", role.get("SR_NAME"));
                            stepList.add(transactor);
                        } else if (MagicValue.TWO.equals(toString(childrenStep.get("SPS_STEP_TYPE")))) {
                            //下一步为人员
                            paramMap.clear();
                            paramMap.put("ID", childrenStep.get("SR_ID"));
                            List<Map<String, Object>> operatorList = operatorService.selectOperatorByRoleId(toString(childrenStep.get("SR_ID")));
                            operatorList.forEach(map -> {
                                Map<String, Object> transactor = Maps.newHashMapWithExpectedSize(2);
                                transactor.put("KEY", map.get("ID"));
                                transactor.put("VALUE", map.get("SAI_NAME"));
                                stepList.add(transactor);
                            });
                        }
                        paramMap.clear();
                        paramMap.put("ID", childrenStep.get("SPS_PARENTID"));
                        Map<String, Object> step = this.selectProcessStep(paramMap);

                        for (Map<String, Object> transactor : stepList) {
                            transactor.put("joinTransactor", CommonUtil.idEncrypt(childrenStep.get("ID"), transactor.get("KEY")));
                        }

                        stepMap.put("stepTab", step.get("SPS_TAB"));
                        stepMap.put("step", childrenStep);
                        stepMap.put("transactorList", stepList);
                        childrenTransactorList.add(stepMap);
                    }
                    resultMap.put("childrenTransactorList", childrenTransactorList);
                }
            } else {
                //下一步为结束
                Map<String, Object> transactor = Maps.newHashMapWithExpectedSize(2);
                transactor.put("KEY", "0");
                transactor.put("VALUE", "结束");
                transactorList.add(transactor);
            }

        } else if (processType == ProcessType.BACK.getType()) {
            //是否是退回父类流程
            boolean isParentProcess = false;
            //查询上一步骤办理人
            nextStep = processPrevStep(definitionId, scheduleAuditStatus, SPS_PARENTID);
            if (isEmpty(nextStep)) {
                if (!"0".equals(SPS_PARENTID)) {
                    //验证是否可以测回
                    checkChildrenIsStart(definitionId, tableId, scheduleId);
                    //获取父类退回流程
                    paramMap.clear();
                    paramMap.put("ID", scheduleParentId);
                    scheduleParent = selectProcessSchedule(paramMap);

                    nextStep = processPrevStep(definitionId, toString(scheduleParent.get("SPS_AUDIT_STATUS")), toString(scheduleParent.get("SPS_PARENTID")));
                    nextStep.put("SPS_STEP_BRANCH", ProcessBranch.BRANCH.toString());

                    isParentProcess = true;
                } else {
                    throw new CustomException("没有找到下一流程步骤！");
                }
            }

            //是否允许多级退回
            if (toInt(definition.get("IS_MULTISTAGE_BACK")) == STATUS_ERROR) {
                //查询日志查询流程对应状态办理人
                paramMap.clear();
                if (isParentProcess) {
                    paramMap.put("SPS_ID", scheduleParentId);
                } else {
                    paramMap.put("SPS_ID", scheduleId);
                }
                paramMap.put("SPL_STEP_BRANCH", ProcessBranch.FIXED.toString());
                paramMap.put("SPL_TABLE_ID", tableId);
                paramMap.put("SPS_PROCESS_STATUS", nextStep.get("SPS_PROCESS_STATUS"));
                paramMap.put("SPL_IS_DEFAULT", toString(STATUS_ERROR));
                Map<String, Object> processLog = selectProcessLog(paramMap);
                if (isEmpty(processLog)) {
                    throw new CustomException("流程变动，请联系管理员！");
                }
                Map<String, Object> transactor = Maps.newHashMapWithExpectedSize(2);
                transactor.put("KEY", processLog.get("SPL_PROCESS_STATUS") + SERVICE_SPLIT + processLog.get("SPL_SO_ID"));
                transactor.put("VALUE", processLog.get("SPL_PROCESS_STATUS_NAME") + ":" + processLog.get("SPL_TRANSACTOR"));
                transactorList.add(transactor);
            } else {
                //多级退回
                List<Map<String, Object>> nextSteps = processPrevStepList(definitionId, scheduleAuditStatus, SPS_PARENTID);
                //查询步骤对应日志的办理人,只查询提交的
                StringBuilder SPS_PROCESS_STATUS_ARRAY = new StringBuilder();
                nextSteps.forEach(map -> {
                    SPS_PROCESS_STATUS_ARRAY.append(map.get("SPS_PROCESS_STATUS") + ",");
                });
                paramMap.clear();
                if (isParentProcess) {
                    paramMap.put("SPS_ID", scheduleParentId);
                } else {
                    paramMap.put("SPS_ID", scheduleId);
                }
                paramMap.put("SPL_TABLE_ID", tableId);
                paramMap.put("SPL_PROCESS_STATUS_ARRAY", TextUtil.interceptSymbol(SPS_PROCESS_STATUS_ARRAY.toString(), ","));
                paramMap.put("SPL_TYPE", "0");
                paramMap.put("NOT_SPL_PROCESS_STATUS", schedule.get("SPS_BACK_STATUS_TRANSACTOR"));
                paramMap.put("SPL_STEP_BRANCH", ProcessBranch.FIXED.toString());
                paramMap.put("SPL_IS_DEFAULT", toString(STATUS_ERROR));
                paramMap.put("IS_GROUP", true);
                List<Map<String, Object>> processLogList = selectProcessLogList(paramMap);
                if (isEmpty(processLogList)) {
                    throw new CustomException("流程变动，请联系管理员！");
                }

                processLogList.forEach(processLog -> {
                    Map<String, Object> transactor = Maps.newHashMapWithExpectedSize(2);
                    transactor.put("KEY", processLog.get("SPL_PROCESS_STATUS") + SERVICE_SPLIT + processLog.get("SPL_SO_ID"));
                    transactor.put("VALUE", processLog.get("SPL_PROCESS_STATUS_NAME") + ":" + processLog.get("SPL_TRANSACTOR"));
                    transactorList.add(transactor);
                });
            }
        }

        resultMap.put("nextStep", nextStep);
        resultMap.put("transactorList", transactorList);

        return resultMap;
    }

    @Override
    public Map<String, Object> getParentProcessNextTransactor(Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(3);
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(5);


        int processType = toInt(mapParam.get("processType"), 0);
        String tableId = toString(mapParam.get("tableId"));
        String definitionId = toString(mapParam.get("definitionId"));
        String scheduleId = toString(mapParam.get("scheduleId"));
        String scheduleParentId = toString(mapParam.get("scheduleParentId"));
        String nextStepId = toString(mapParam.get("nextStepId"));

        if (ProcessType.SUBMIT.getType() == processType && !"0".equals(scheduleParentId)) {
            paramMap.clear();
            paramMap.put("ID", nextStepId);
            Map<String, Object> nextStep = this.selectProcessStep(paramMap);
            //下一步是完成
            if (ProcessStatus.COMPLETE.toString().equals(toString(nextStep.get("SPS_PROCESS_STATUS")))) {
                paramMap.clear();
                paramMap.put("ID", scheduleId);
                Map<String, Object> schedule = this.selectProcessSchedule(paramMap);

                scheduleParentId = toString(schedule.get("SPS_PARENTID"));
                if ("0".equals(scheduleParentId)) {
                    resultMap.put("isSelectParent", false);
                    return resultMap;
                }
                //查询除了自身其他子流程是否完成
                paramMap.clear();
                paramMap.put("SPS_PARENTID", scheduleParentId);
                paramMap.put("SPS_TABLE_ID", tableId);
                paramMap.put("NOT_ID", scheduleId);
                List<Map<String, Object>> scheduleList = baseDao.selectList(NameSpace.ProcessMapper, "selectProcessSchedule", paramMap);
                for (Map<String, Object> s : scheduleList) {
                    if (!ProcessStatus.COMPLETE.toString().equals(toString(s.get("SPS_AUDIT_STATUS")))) {
                        resultMap.put("isSelectParent", false);
                        return resultMap;
                    }
                }

                paramMap.clear();
                paramMap.put("ID", scheduleParentId);
                Map<String, Object> scheduleParent = this.selectProcessSchedule(paramMap);

                String scheduleParentStatus = toString(scheduleParent.get("SPS_AUDIT_STATUS"));

                //查询下一步办理人
                paramMap.clear();
                paramMap.put("processType", processType);
                paramMap.put("tableId", tableId);
                paramMap.put("definitionId", definitionId);
                paramMap.put("scheduleId", scheduleParentId);
                paramMap.put("scheduleAuditStatus", scheduleParentStatus);
                paramMap.put("isSkipBranch", true);
                Map<String, Object> transactorMap = this.getProcessTransactor(paramMap);

                int SPS_STEP_BRANCH = toInt(nextStep.get("SPS_STEP_BRANCH"));

                if (SPS_STEP_BRANCH == ProcessBranch.FIXED.getType()) {
                    //固定
                    resultMap.put("transactorParentList", transactorMap.get("transactorList"));
                } else if (SPS_STEP_BRANCH == ProcessBranch.BRANCH.getType() || SPS_STEP_BRANCH == ProcessBranch.CONCURRENT.getType()) {
                    //分支 并发
                    resultMap.put("childrenParentTransactorList", transactorMap.get("childrenTransactorList"));
                }

                resultMap.put("isSelectParent", true);
                resultMap.put("nextParentStep", transactorMap.get("nextStep"));
                resultMap.put("nextParentBranch", SPS_STEP_BRANCH);
                resultMap.put("parentStepId", scheduleParent.get("SPS_ID"));
                resultMap.put("parentScheduleId", scheduleParent.get("ID"));
            }
        }

        return resultMap;
    }


    /****   流程定义    ***/

    @Override
    public Map<String, Object> selectProcessDefinitionById(String ID) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
        paramMap.put("ID", ID);
        return this.selectProcessDefinition(paramMap);
    }

    @Override
    public Map<String, Object> selectProcessDefinition(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
        paramMap.put("ID", mapParam.get("ID"));
        paramMap.put("BUS_PROCESS", mapParam.get("BUS_PROCESS"));
        paramMap.put("BUS_PROCESS2", mapParam.get("BUS_PROCESS2"));
        return baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessDefinition", paramMap);
    }

    @Override
    public List<Tree> selectProcessDefinitionTreeList(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
        paramMap.put("IS_STATUS", STATUS_SUCCESS);
        List<Map<String, Object>> definitionList = baseDao.selectList(NameSpace.ProcessFixedMapper, "selectProcessDefinition", paramMap);

        return getDefinitionTree(definitionList, toString(mapParam.get("ID")));
    }


    @Override
    @Transactional
    public Map<String, Object> insertAndUpdateProcessDefinition(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = SAVE_ERROR;
        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(14);
            String id = toString(mapParam.get("ID"));
            //查询大小类是否重复
            paramMap.put("NOT_ID", id);
            paramMap.put("BUS_PROCESS", mapParam.get("BUS_PROCESS"));
            paramMap.put("BUS_PROCESS2", mapParam.get("BUS_PROCESS2"));
            int count = baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessDefinitionCount", paramMap);
            if (count > 0) {
                throw new CustomException("流程大小类重复!");
            }
            //记录日志
            paramMap.clear();
            paramMap.put(MagicValue.SVR_TABLE_NAME, TableName.SYS_PROCESS_DEFINITION);

            paramMap.put("ID", id);
            paramMap.put("SO_ID", mapParam.get("SO_ID"));
            paramMap.put("SR_ID", mapParam.get("SR_ID"));
            paramMap.put("BUS_PROCESS", mapParam.get("BUS_PROCESS"));
            paramMap.put("BUS_PROCESS2", mapParam.get("BUS_PROCESS2"));
            paramMap.put("SPD_NAME", mapParam.get("SPD_NAME"));
            paramMap.put("SPD_VERSION", mapParam.get("SPD_VERSION"));
            paramMap.put("SPD_UPDATE_TABLE", mapParam.get("SPD_UPDATE_TABLE"));
            paramMap.put("SPD_UPDATE_NAME", mapParam.get("SPD_UPDATE_NAME"));
            paramMap.put("SPD_COLLEGE_FIELD", mapParam.get("SPD_COLLEGE_FIELD"));
            paramMap.put("SPD_DEPARTMENT_FIELD", mapParam.get("SPD_DEPARTMENT_FIELD"));
            paramMap.put("SPD_CLASS_FIELD", mapParam.get("SPD_CLASS_FIELD"));
            paramMap.put("SPD_DESCRIBE", mapParam.get("SPD_DESCRIBE"));
            paramMap.put("SDP_ENTRY_TIME", mapParam.get("SDP_ENTRY_TIME"));
            paramMap.put("IS_STATUS", mapParam.get("IS_STATUS"));
            paramMap.put("IS_MULTISTAGE_BACK", mapParam.get("IS_MULTISTAGE_BACK"));
            paramMap.put("IS_TIME_CONTROL", mapParam.get("IS_TIME_CONTROL"));

            if (isEmpty(id)) {
                id = getId();
                paramMap.put("ID", id);
                paramMap.put("IS_STATUS", Attribute.STATUS_SUCCESS);
                paramMap.put("SO_ID", getActiveUser().getId());
                paramMap.put("SDP_ENTRY_TIME", getSqlDate());

                baseDao.insert(NameSpace.ProcessFixedMapper, "insertProcessDefinition", paramMap);
                resultMap.put(MagicValue.LOG, "添加流程定义:" + formatColumnName(TableName.SYS_PROCESS_DEFINITION, paramMap));
            } else {
                Map<String, Object> oldMap = Maps.newHashMapWithExpectedSize(1);
                oldMap.put("ID", id);
                oldMap = selectProcessDefinition(oldMap);

                baseDao.update(NameSpace.ProcessFixedMapper, "updateProcessDefinition", paramMap);
                resultMap.put(MagicValue.LOG, "更新流程定义,更新前:" + formatColumnName(TableName.SYS_PROCESS_DEFINITION, oldMap) + ",更新后:" + formatColumnName(TableName.SYS_PROCESS_DEFINITION, paramMap));
            }
            status = STATUS_SUCCESS;
            desc = SAVE_SUCCESS;

            resultMap.put("ID", id);
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> changeProcessDefinitionStatus(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = SAVE_ERROR;
        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
            String id = toString(mapParam.get("ID"));
            //记录日志
            paramMap.put(MagicValue.SVR_TABLE_NAME, TableName.SYS_PROCESS_DEFINITION);

            paramMap.put("ID", id);
            paramMap.put("IS_STATUS", mapParam.get("IS_STATUS"));

            Map<String, Object> oldMap = Maps.newHashMapWithExpectedSize(1);
            oldMap.put("ID", id);
            oldMap = selectProcessDefinition(oldMap);

            baseDao.update(NameSpace.ProcessFixedMapper, "updateProcessDefinition", paramMap);
            resultMap.put(MagicValue.LOG, "更新流程定义状态,流程定义名:" + toString(oldMap.get("SPD_NAME")) + ",状态更新为:" + ParamTypeResolve.statusExplain(mapParam.get("IS_STATUS")));

            //清除缓存
            CacheUtil.clear(NameSpace.MenuMapper.getValue());

            status = STATUS_SUCCESS;
            desc = SAVE_SUCCESS;

            resultMap.put("ID", id);
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> copyProcessDefinition(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = SAVE_ERROR;
        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
            String id = toString(mapParam.get("ID"));
            //查询大小类是否重复
            paramMap.put("BUS_PROCESS", mapParam.get("BUS_PROCESS"));
            paramMap.put("BUS_PROCESS2", mapParam.get("BUS_PROCESS2"));
            int count = baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessDefinitionCount", paramMap);
            if (count > 0) {
                throw new CustomException("流程大小类重复!");
            }

            paramMap.clear();
            paramMap.put("ID", id);
            Map<String, Object> definition = this.selectProcessDefinition(paramMap);
            String newDefinitionId = getId();
            //拷贝流程定义
            definition.put("ID", newDefinitionId);
            definition.put("SPD_NAME", mapParam.get("SPD_NAME"));
            definition.put("SPD_VERSION", mapParam.get("SPD_VERSION"));
            definition.put("BUS_PROCESS", mapParam.get("BUS_PROCESS"));
            definition.put("BUS_PROCESS2", mapParam.get("BUS_PROCESS2"));
            definition.put("SPD_UPDATE_TABLE", mapParam.get("SPD_UPDATE_TABLE"));
            definition.put("SPD_UPDATE_NAME", mapParam.get("SPD_UPDATE_NAME"));
            definition.put("SDP_ENTRY_TIME", getDate());
            baseDao.insert(NameSpace.ProcessFixedMapper, "insertProcessDefinition", definition);

            //拷贝流程步骤
            copyProcessStep(baseDao, id, newDefinitionId, "0", "0");

            //拷贝流程启动角色
            paramMap.clear();
            paramMap.put("SPD_ID", id);
            List<Map<String, Object>> startList = this.selectProcessStartList(paramMap);

            for (Map<String, Object> start : startList) {
                start.put("ID", getId());
                start.put("SPD_ID", newDefinitionId);
                baseDao.insert(NameSpace.ProcessFixedMapper, "insertProcessStart", start);
            }

            resultMap.put(MagicValue.LOG, "拷贝流程定义:" + formatColumnName(TableName.SYS_PROCESS_DEFINITION, definition));

            status = STATUS_SUCCESS;
            desc = SAVE_SUCCESS;

            resultMap.put("ID", id);
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    /**
     * 拷贝步骤
     *
     * @param baseDao
     * @param SPD_ID
     * @param newDefinitionId
     * @param SPS_PARENTID
     * @throws Exception
     */
    public void copyProcessStep(BaseDao baseDao, String oldDefinitionId, String newDefinitionId, String oldParentId, String newParentId) throws Exception {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.clear();
        paramMap.put("SPD_ID", oldDefinitionId);
        paramMap.put("SPS_PARENTID", oldParentId);
        List<Map<String, Object>> stepList = this.selectProcessStepList(paramMap);

        if (!isEmpty(stepList)) {
            for (Map<String, Object> step : stepList) {
                String oldId = toString(step.get("ID"));
                String newId = getId();

                step.put("ID", newId);
                step.put("SPD_ID", newDefinitionId);
                step.put("SPS_PARENTID", newParentId);
                baseDao.insert(NameSpace.ProcessFixedMapper, "insertProcessStep", step);

                copyProcessStep(baseDao, oldDefinitionId, newDefinitionId, oldId, newId);
            }
        }
    }

    /****   流程步骤    ***/

    @Override
    public Map<String, Object> selectProcessStep(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(5);
        paramMap.put("ID", mapParam.get("ID"));
        paramMap.put("SPS_PARENTID", mapParam.get("SPS_PARENTID"));
        paramMap.put("SR_ID", mapParam.get("SR_ID"));
        paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
        paramMap.put("SPS_PROCESS_STATUS", mapParam.get("SPS_PROCESS_STATUS"));
        return baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessStep", paramMap);
    }

    @Override
    public List<Map<String, Object>> selectProcessStepList(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
        paramMap.put("SPS_PARENTID", mapParam.get("SPS_PARENTID"));
        return baseDao.selectList(NameSpace.ProcessFixedMapper, "selectProcessStep", paramMap);
    }

    @Override
    public boolean selectProcessStepIsEdit(String roleId, String busProcess, String busProcess2) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(4);
        paramMap.put("SR_ID", roleId);
        paramMap.put("BUS_PROCESS", busProcess);
        paramMap.put("BUS_PROCESS2", busProcess2);
        int count = baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessStepIsEdit", paramMap);

        return count == 0 ? false : true;
    }


    @Override
    @Transactional
    public Map<String, Object> insertAndUpdateProcessStep(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = SAVE_ERROR;
        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(16);
            String id = toString(mapParam.get("ID"));
            //检测流程状态是否重复
            paramMap.put("NOT_ID", id);
            paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
            paramMap.put("SPS_PARENTID", mapParam.get("SPS_PARENTID"));
            paramMap.put("SPS_PROCESS_STATUS", mapParam.get("SPS_PROCESS_STATUS"));
            int count = baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessStepCount", paramMap);
            if (count > 0) {
                throw new CustomException("步骤流程状态重复,请检查!");
            }
            //检测流程排序是否重复
            paramMap.put("NOT_ID", id);
            paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
            paramMap.put("SPS_PARENTID", mapParam.get("SPS_PARENTID"));
            paramMap.put("SPS_ORDER", mapParam.get("SPS_ORDER"));
            count = baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessStepCount", paramMap);
            if (count > 0) {
                throw new CustomException("步骤流程状态重复,请检查!");
            }
            //记录日志
            paramMap.put(MagicValue.SVR_TABLE_NAME, TableName.SYS_PROCESS_STEP);

            paramMap.put("ID", mapParam.get("ID"));
            paramMap.put("SPS_PARENTID", mapParam.get("SPS_PARENTID"));
            paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
            paramMap.put("SR_ID", mapParam.get("SR_ID"));
            paramMap.put("SPS_NAME", mapParam.get("SPS_NAME"));
            paramMap.put("SPS_ORDER", mapParam.get("SPS_ORDER"));
            paramMap.put("SPS_PROCESS_STATUS", mapParam.get("SPS_PROCESS_STATUS"));
            paramMap.put("SPS_IS_OVER_TIME", mapParam.get("SPS_IS_OVER_TIME"));
            paramMap.put("SPS_OVER_TIME", mapParam.get("SPS_OVER_TIME"));
            paramMap.put("SPS_STEP_BRANCH", mapParam.get("SPS_STEP_BRANCH"));
            paramMap.put("SPS_STEP_TYPE", mapParam.get("SPS_STEP_TYPE"));
            paramMap.put("SPS_IS_EDIT", mapParam.get("SPS_IS_EDIT"));
            paramMap.put("SPS_TAB", mapParam.get("SPS_TAB"));
            paramMap.put("SPS_IS_ADVANCE_CHECK", mapParam.get("SPS_IS_ADVANCE_CHECK"));
            paramMap.put("SPS_IS_RETREAT_CHECK", mapParam.get("SPS_IS_RETREAT_CHECK"));
            paramMap.put("SPS_IS_ADVANCE_EXECUTE", mapParam.get("SPS_IS_ADVANCE_EXECUTE"));
            paramMap.put("SPS_IS_RETREAT_EXECUTE", mapParam.get("SPS_IS_RETREAT_EXECUTE"));

            if (isEmpty(id)) {
                id = getId();
                paramMap.put("ID", id);

                baseDao.insert(NameSpace.ProcessFixedMapper, "insertProcessStep", paramMap);
                resultMap.put(MagicValue.LOG, "添加流程步骤:" + formatColumnName(TableName.SYS_PROCESS_STEP, paramMap));
            } else {
                Map<String, Object> oldMap = Maps.newHashMapWithExpectedSize(1);
                oldMap.put("ID", id);
                oldMap = selectProcessStep(oldMap);

                baseDao.update(NameSpace.ProcessFixedMapper, "updateProcessStep", paramMap);
                resultMap.put(MagicValue.LOG, "更新流程步骤,更新前:" + formatColumnName(TableName.SYS_PROCESS_STEP, oldMap) + ",更新后:" + formatColumnName(TableName.SYS_PROCESS_STEP, paramMap));
            }
            status = STATUS_SUCCESS;
            desc = SAVE_SUCCESS;

            resultMap.put("ID", id);
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> deleteProcessStep(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = DELETE_ERROR;
        try {
            if (isEmpty(mapParam.get("ID"))) {
                throw new CustomException(Tips.ID_NULL_ERROR);
            }
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
            String id = toString(mapParam.get("ID"));

            //删除流程步骤表
            paramMap.clear();
            paramMap.put("ID", id);
            Map<String, Object> oldMap = selectProcessStep(paramMap);
            //记录日志
            paramMap.put(MagicValue.SVR_TABLE_NAME, TableName.SYS_PROCESS_STEP);

            baseDao.delete(NameSpace.ProcessFixedMapper, "deleteProcessStep", paramMap);

            resultMap.put(MagicValue.LOG, "删除流程步骤,信息:" + formatColumnName(TableName.SYS_PROCESS_STEP, oldMap));
            status = STATUS_SUCCESS;
            desc = DELETE_SUCCESS;
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    /****   流程开始角色    ***/

    @Override
    public Map<String, Object> selectProcessStart(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
        paramMap.put("ID", mapParam.get("ID"));
        return baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessStart", paramMap);
    }

    @Override
    public List<Map<String, Object>> selectProcessStartList(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.put("ID", mapParam.get("ID"));
        paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
        return baseDao.selectList(NameSpace.ProcessFixedMapper, "selectProcessStart", paramMap);
    }

    @Override
    @Transactional
    public Map<String, Object> insertAndUpdateProcessStart(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = SAVE_ERROR;
        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
            String id = toString(mapParam.get("ID"));

            paramMap.put("ID", mapParam.get("ID"));
            paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
            paramMap.put("SR_ID", mapParam.get("SR_ID"));

            if (isEmpty(id)) {
                id = getId();
                paramMap.put("ID", id);

                baseDao.insert(NameSpace.ProcessFixedMapper, "insertProcessStart", paramMap);
                resultMap.put(MagicValue.LOG, "添加流程开始角色:" + formatColumnName(TableName.SYS_PROCESS_START, paramMap));
            } else {
                Map<String, Object> oldMap = Maps.newHashMapWithExpectedSize(1);
                oldMap.put("ID", id);
                oldMap = selectProcessStart(oldMap);

                baseDao.update(NameSpace.ProcessFixedMapper, "updateProcessStart", paramMap);
                resultMap.put(MagicValue.LOG, "更新流程开始角色,更新前:" + formatColumnName(TableName.SYS_PROCESS_START, oldMap) + ",更新后:" + formatColumnName(TableName.SYS_PROCESS_START, paramMap));
            }
            status = STATUS_SUCCESS;
            desc = SAVE_SUCCESS;

            resultMap.put("ID", id);
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> deleteProcessStart(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = DELETE_ERROR;
        try {
            if (isEmpty(mapParam.get("ID"))) {
                throw new CustomException(Tips.ID_NULL_ERROR);
            }
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
            String id = toString(mapParam.get("ID"));

            //删除流程开始角色表
            paramMap.clear();
            paramMap.put("ID", id);
            Map<String, Object> oldMap = selectProcessStart(paramMap);
            //记录日志
            paramMap.put(MagicValue.SVR_TABLE_NAME, TableName.SYS_PROCESS_START);
            baseDao.delete(NameSpace.ProcessFixedMapper, "deleteProcessStart", paramMap);

            resultMap.put(MagicValue.LOG, "删除流程开始角色,信息:" + formatColumnName(TableName.SYS_PROCESS_START, oldMap));
            status = STATUS_SUCCESS;
            desc = DELETE_SUCCESS;
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }


    /****   流程控制时间    ***/

    @Override
    public Map<String, Object> selectProcessTimeControl(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
        paramMap.put("ID", mapParam.get("ID"));
        paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
        paramMap.put("IS_STATUS", mapParam.get("IS_STATUS"));
        return baseDao.selectOne(NameSpace.ProcessFixedMapper, "selectProcessTimeControl", paramMap);
    }

    @Override
    @Transactional
    public Map<String, Object> insertAndUpdateProcessTimeControl(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = SAVE_ERROR;
        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
            String id = toString(mapParam.get("ID"));

            paramMap.put("ID", id);
            paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
            paramMap.put("SPTC_START_TIME", mapParam.get("SPTC_START_TIME"));
            paramMap.put("SPTC_END_TIME", mapParam.get("SPTC_END_TIME"));

            if (isEmpty(id)) {
                id = getId();
                paramMap.put("ID", id);
                paramMap.put("IS_STATUS", Attribute.STATUS_ERROR);
                paramMap.put("SPTC_ENTRY_TIME", getDate());

                baseDao.insert(NameSpace.ProcessFixedMapper, "insertProcessTimeControl", paramMap);
                resultMap.put(MagicValue.LOG, "添加流程时间控制:" + formatColumnName(TableName.SYS_PROCESS_TIME_CONTROL, paramMap));
            } else {
                Map<String, Object> oldMap = Maps.newHashMapWithExpectedSize(1);
                oldMap.put("ID", id);
                oldMap = selectProcessTimeControl(oldMap);

                baseDao.update(NameSpace.ProcessFixedMapper, "updateProcessTimeControl", paramMap);
                resultMap.put(MagicValue.LOG, "更新流程时间控制,更新前:" + formatColumnName(TableName.SYS_PROCESS_TIME_CONTROL, oldMap) + ",更新后:" + formatColumnName(TableName.SYS_PROCESS_TIME_CONTROL, paramMap));
            }
            status = STATUS_SUCCESS;
            desc = SAVE_SUCCESS;

            resultMap.put("ID", id);
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> changeProcessTimeControlStatus(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = SAVE_ERROR;
        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
            String id = toString(mapParam.get("ID"));
            int IS_STATUS = toInt(mapParam.get("IS_STATUS"));

            paramMap.put("ID", id);
            Map<String, Object> oldMap = selectProcessTimeControl(paramMap);

            if (IS_STATUS == 1) {
                //如果是启用要把正在启用的停止
                paramMap.clear();
                paramMap.put("SDP_ID", oldMap.get("SDP_ID"));
                paramMap.put("IS_STATUS", Attribute.STATUS_SUCCESS);
                Map<String, Object> timeControl = this.selectProcessTimeControl(paramMap);
                if (!isEmpty(timeControl)) {
                    //记录日志
                    paramMap.clear();
                    paramMap.put(MagicValue.SVR_TABLE_NAME, TableName.SYS_PROCESS_TIME_CONTROL);
                    paramMap.put("ID", timeControl.get("ID"));
                    paramMap.put("IS_STATUS", Attribute.STATUS_ERROR);
                    baseDao.update(NameSpace.ProcessFixedMapper, "updateProcessTimeControl", paramMap);
                }
            }

            paramMap.clear();
            //记录日志
            paramMap.put(MagicValue.SVR_TABLE_NAME, TableName.SYS_PROCESS_TIME_CONTROL);

            paramMap.put("ID", id);
            paramMap.put("IS_STATUS", IS_STATUS);

            baseDao.update(NameSpace.ProcessFixedMapper, "updateProcessTimeControl", paramMap);
            resultMap.put(MagicValue.LOG, "更新流程时间控制状态,流程时间控制名:" + toString(oldMap.get("SPD_NAME")) + ",状态更新为:" + ParamTypeResolve.statusExplain(mapParam.get("IS_STATUS")));

            status = STATUS_SUCCESS;
            desc = SAVE_SUCCESS;

            resultMap.put("ID", id);
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> deleteProcessTimeControl(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = DELETE_ERROR;
        try {
            if (isEmpty(mapParam.get("ID"))) {
                throw new CustomException(Tips.ID_NULL_ERROR);
            }
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
            String id = toString(mapParam.get("ID"));

            //删除流程时间控制表
            paramMap.clear();
            paramMap.put("ID", id);
            Map<String, Object> oldMap = selectProcessTimeControl(paramMap);
            //记录日志
            paramMap.put(MagicValue.SVR_TABLE_NAME, TableName.SYS_PROCESS_TIME_CONTROL);
            baseDao.delete(NameSpace.ProcessFixedMapper, "deleteProcessTimeControl", paramMap);

            resultMap.put(MagicValue.LOG, "删除流程时间控制,信息:" + formatColumnName(TableName.SYS_PROCESS_TIME_CONTROL, oldMap));
            status = STATUS_SUCCESS;
            desc = DELETE_SUCCESS;
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    /****   流程进度   ***/
    @Override
    public Map<String, Object> selectProcessSchedule(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(6);
        paramMap.put("ID", mapParam.get("ID"));
        paramMap.put("SPS_TABLE_ID", mapParam.get("SPS_TABLE_ID"));
        paramMap.put("SPS_PARENTID", mapParam.get("SPS_PARENTID"));
        paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
        paramMap.put("SPS_ID", mapParam.get("SPS_ID"));
        paramMap.put("SPS_IS_CANCEL", mapParam.get("SPS_IS_CANCEL"));
        return baseDao.selectOne(NameSpace.ProcessMapper, "selectProcessSchedule", paramMap);
    }


    @Override
    @Transactional
    public Map<String, Object> insertAndUpdateProcessSchedule(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = SAVE_ERROR;
        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(17);
            String id = toString(mapParam.get("ID"));
            String SPS_PROCESS_OPERATOR = toString(mapParam.get("SPS_PROCESS_OPERATOR"));
            //记录日志
            paramMap.put(MagicValue.SVR_TABLE_NAME, TableName.SYS_PROCESS_SCHEDULE);

            paramMap.put("ID", mapParam.get("ID"));
            paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
            paramMap.put("SPS_TABLE_ID", mapParam.get("SPS_TABLE_ID"));
            paramMap.put("SPS_AUDIT_STATUS", mapParam.get("SPS_AUDIT_STATUS"));
            paramMap.put("SPS_BACK_STATUS", mapParam.get("SPS_BACK_STATUS"));
            paramMap.put("SPS_STEP_TYPE", mapParam.get("SPS_STEP_TYPE"));
            paramMap.put("SPS_STEP_TRANSACTOR", mapParam.get("SPS_STEP_TRANSACTOR"));
            paramMap.put("SPS_PREV_AUDIT_STATUS", mapParam.get("SPS_PREV_AUDIT_STATUS"));
            paramMap.put("SPS_PREV_STEP_TYPE", mapParam.get("SPS_PREV_STEP_TYPE"));
            paramMap.put("SPS_PREV_STEP_TRANSACTOR", mapParam.get("SPS_PREV_STEP_TRANSACTOR"));
            paramMap.put("SPS_BACK_STATUS_TRANSACTOR", mapParam.get("SPS_BACK_STATUS_TRANSACTOR"));
            paramMap.put("SPS_TABLE_NAME", mapParam.get("SPS_TABLE_NAME"));
            paramMap.put("SPS_PREV_STEP_ID", mapParam.get("SPS_PREV_STEP_ID"));
            paramMap.put("SPS_ID", mapParam.get("SPS_ID"));

            if (isEmpty(id)) {
                id = getId();
                paramMap.put("ID", id);
                paramMap.put("SO_ID", mapParam.get("SO_ID"));
                paramMap.put("SPS_PARENTID", mapParam.get("SPS_PARENTID"));
                if (!isEmpty(mapParam.get("SHOW_SO_ID"))) {
                    paramMap.put("SHOW_SO_ID", mapParam.get("SHOW_SO_ID"));
                }
                paramMap.put("SPS_PROCESS_OPERATOR", "," + SPS_PROCESS_OPERATOR + ",");

                baseDao.insert(NameSpace.ProcessMapper, "insertProcessSchedule", paramMap);
//                resultMap.put(MagicValue.LOG, "添加流程进度:" + toString(paramMap));
            } else {
                Map<String, Object> oldMap = Maps.newHashMapWithExpectedSize(1);
                oldMap.put("ID", id);
                oldMap = selectProcessSchedule(oldMap);

                String OLD_SPS_PROCESS_OPERATOR = toString(oldMap.get("SPS_PROCESS_OPERATOR"));
                if (!isEmpty(SPS_PROCESS_OPERATOR) && !OLD_SPS_PROCESS_OPERATOR.contains(getOperatorJoin(SPS_PROCESS_OPERATOR))) {
                    paramMap.put("SPS_PROCESS_OPERATOR", OLD_SPS_PROCESS_OPERATOR + getOperatorJoin(SPS_PROCESS_OPERATOR));
                }

                baseDao.update(NameSpace.ProcessMapper, "updateProcessSchedule", paramMap);
//                resultMap.put(MagicValue.LOG, "更新流程进度,更新前:" + toString(oldMap) + ",更新后:" + toString(paramMap));
            }
            status = STATUS_SUCCESS;
            desc = SAVE_SUCCESS;

            resultMap.put("ID", id);
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> cancelProcessSchedule(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = SAVE_ERROR;
        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(5);
            String id = toString(mapParam.get("ID"));
            boolean isNotValidate = toBoolean(mapParam.get("IS_NOT_VALIDATE"));

            paramMap.put("ID", id);
            Map<String, Object> schedule = this.selectProcessSchedule(paramMap);
            if (!isNotValidate && !"0".equals(toString(schedule.get("SPS_PARENTID")))) {
                throw new CustomException("子流程无法单独作废");
            }

            String scheduleCancelReason = toString(mapParam.get("SPSC_REASON"));
            paramMap.clear();
            paramMap.put("ID", id);
            paramMap.put("SPS_IS_CANCEL", STATUS_SUCCESS);

            baseDao.update(NameSpace.ProcessMapper, "updateProcessSchedule", paramMap);
            //插入作废表
            paramMap.clear();
            paramMap.put("ID", getId());
            paramMap.put("SO_ID", getActiveUser().getId());
            paramMap.put("SPS_ID", id);
            paramMap.put("SPSC_REASON", scheduleCancelReason);
            paramMap.put("SPSC_ENTRY_TIME", getSqlDate());
            baseDao.insert(NameSpace.ProcessMapper, "insertProcessScheduleCancel", paramMap);

            //作废子流程
            paramMap.clear();
            paramMap.put("SPS_PARENTID", id);
            paramMap.put("SPS_IS_CANCEL", toString(STATUS_ERROR));
            List<Map<String, Object>> scheduleList = baseDao.selectList(NameSpace.ProcessMapper, "selectProcessSchedule", paramMap);

            for (Map<String, Object> s : scheduleList) {
                mapParam.put("ID", s.get("ID"));
                mapParam.put("IS_NOT_VALIDATE", true);
                this.cancelProcessSchedule(mapParam);
            }
            
            resultMap.put(MagicValue.LOG, "作废流程进度,作废原因:" + scheduleCancelReason);

            status = STATUS_SUCCESS;
            desc = SAVE_SUCCESS;

            resultMap.put("ID", id);
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> deleteProcessSchedule(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = DELETE_ERROR;
        try {
            if (isEmpty(mapParam.get("ID"))) {
                throw new CustomException(Tips.ID_NULL_ERROR);
            }
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
            String id = toString(mapParam.get("ID"));

            //删除流程进度表
            paramMap.clear();
            paramMap.put("ID", id);
            Map<String, Object> oldMap = selectProcessSchedule(paramMap);

            baseDao.delete(NameSpace.ProcessMapper, "deleteProcessSchedule", paramMap);

//            resultMap.put(MagicValue.LOG, "删除流程进度,信息:" + toString(oldMap));
            status = STATUS_SUCCESS;
            desc = DELETE_SUCCESS;
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    /****   流程日志   ***/

    @Override
    public Map<String, Object> selectProcessLog(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(7);
        paramMap.put("ID", mapParam.get("ID"));
        paramMap.put("SPS_ID", mapParam.get("SPS_ID"));
        paramMap.put("SPL_STEP_ID", mapParam.get("SPL_STEP_ID"));
        paramMap.put("SPL_PARENTID", mapParam.get("SPL_PARENTID"));
        paramMap.put("SPL_TABLE_ID", mapParam.get("SPL_TABLE_ID"));
        paramMap.put("SPL_STEP_BRANCH", mapParam.get("SPL_STEP_BRANCH"));
        paramMap.put("SPL_PROCESS_STATUS", mapParam.get("SPL_PROCESS_STATUS"));
        paramMap.put("SPL_STATUS", mapParam.get("SPL_STATUS"));
        paramMap.put("SPL_IS_DEFAULT", mapParam.get("SPL_IS_DEFAULT"));
        return baseDao.selectOne(NameSpace.ProcessMapper, "selectProcessLog", paramMap);
    }

    @Override
    public List<Map<String, Object>> selectProcessLogList(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(11);
        paramMap.put("SPS_ID", mapParam.get("SPS_ID"));
        paramMap.put("SPL_STEP_ID", mapParam.get("SPL_STEP_ID"));
        paramMap.put("SPL_PARENTID", mapParam.get("SPL_PARENTID"));
        paramMap.put("SPL_TABLE_ID", mapParam.get("SPL_TABLE_ID"));
        paramMap.put("SPL_PROCESS_STATUS_ARRAY", mapParam.get("SPL_PROCESS_STATUS_ARRAY_ARRAY"));
        paramMap.put("NOT_SPL_PROCESS_STATUS", mapParam.get("NOT_SPL_PROCESS_STATUS"));
        paramMap.put("SPL_TYPE", mapParam.get("SPL_TYPE"));
        paramMap.put("SPL_STEP_BRANCH", mapParam.get("SPL_STEP_BRANCH"));
        paramMap.put("SPL_STATUS", mapParam.get("SPL_STATUS"));
        paramMap.put("SPL_IS_DEFAULT", mapParam.get("SPL_IS_DEFAULT"));

        paramMap.put("IS_GROUP", mapParam.get("IS_GROUP"));
        return baseDao.selectList(NameSpace.ProcessMapper, "selectProcessLog", paramMap);
    }

    @Override
    public List<Map<String, Object>> selectProcessLogList(int level, String definitionId, String scheduleId, String tableId, String logParentId) {
        List<Map<String, Object>> logList = Lists.newLinkedList();

        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.clear();
        paramMap.put("SPD_ID", definitionId);
        paramMap.put("SPL_TABLE_ID", tableId);
        paramMap.put("SPL_PARENTID", logParentId);
        if (level > 0) {
            paramMap.put("LOG_CHILDREN_ORDER", true);
        } else {
            paramMap.put("LOG_ORDER", true);
            paramMap.put("SPS_ID", scheduleId);
        }
        List<Map<String, Object>> beforeLogList = baseDao.selectList(NameSpace.ProcessMapper, "selectProcessLog", paramMap);
        if (isEmpty(beforeLogList)) {
            return logList;
        }

        String stepParentId = null;

        for (Map<String, Object> log : beforeLogList) {
            logList.add(log);

            paramMap.clear();
            paramMap.put("ID", log.get("SPL_STEP_ID"));
            Map<String, Object> step = selectProcessStep(paramMap);

            if (level > 0 || !"0".equals(logParentId)) {
                log.put("SPL_BRANCH", "分支:" + toString(log.get("SPL_STEP_TAB")));
                log.put("SPL_PROCESS_STATUS_NAME", TextUtil.joinFirstTextSymbol(log.get("SPL_PROCESS_STATUS_NAME"), "&emsp;", level + 1));
            } else {
                log.put("SPL_BRANCH", "");
            }

            int SPS_STEP_BRANCH = toInt(step.get("SPS_STEP_BRANCH"), ProcessBranch.FIXED.getType());
            if (SPS_STEP_BRANCH != ProcessBranch.FIXED.getType()) {
                logList.addAll(selectProcessLogList(level + 1, definitionId, scheduleId, tableId, toString(log.get("ID"))));
            }
        }


        return logList;
    }

    @Override
    @Transactional
    public Map<String, Object> insertProcessLog(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = SAVE_ERROR;
        try {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(16);
            String id = toString(mapParam.get("ID"));

            //查询步骤标记
            Map<String, Object> step = null;
            if (!isEmpty(mapParam.get("SPL_STEP_ID"))) {
                paramMap.put("ID", mapParam.get("SPL_STEP_ID"));
                step = this.selectProcessStep(paramMap);
                if (!"0".equals(toString(step.get("SPS_PARENTID")))) {
                    paramMap.put("ID", step.get("SPS_PARENTID"));
                    step = this.selectProcessStep(paramMap);
                }
            }

            paramMap.clear();
            paramMap.put("ID", id);
            paramMap.put("SPD_ID", mapParam.get("SPD_ID"));
            paramMap.put("SPS_ID", mapParam.get("SPS_ID"));
            paramMap.put("SPL_STEP_ID", mapParam.get("SPL_STEP_ID"));
            if (!isEmpty(step)) {
                paramMap.put("SPL_STEP_TAB", step.get("SPS_TAB"));
            }
            paramMap.put("SPL_TABLE_ID", mapParam.get("SPL_TABLE_ID"));
            paramMap.put("SPL_SO_ID", mapParam.get("SPL_SO_ID"));
            paramMap.put("SPL_TRANSACTOR", mapParam.get("SPL_TRANSACTOR"));
            paramMap.put("SPL_OPINION", mapParam.get("SPL_OPINION"));
            paramMap.put("SPL_ENTRY_TIME", mapParam.get("SPL_ENTRY_TIME"));
            paramMap.put("SPL_TYPE", mapParam.get("SPL_TYPE"));
            paramMap.put("SPL_PROCESS_STATUS", mapParam.get("SPL_PROCESS_STATUS"));
            paramMap.put("SPL_STATUS", mapParam.get("SPL_STATUS"));
            paramMap.put("SPL_STEP_BRANCH", mapParam.get("SPL_STEP_BRANCH"));
            paramMap.put("SPL_IS_DEFAULT", mapParam.get("SPL_IS_DEFAULT"));

            paramMap.put("SPL_WAIT_STEP_TYPE", mapParam.get("SPL_WAIT_STEP_TYPE"));
            paramMap.put("SPL_WAIT_ID", mapParam.get("SPL_WAIT_ID"));
            paramMap.put("SPL_WAIT_TRANSACTOR", mapParam.get("SPL_WAIT_TRANSACTOR"));
            paramMap.put("SPL_WAIT_TIME", mapParam.get("SPL_WAIT_TIME"));

            if (isEmpty(id)) {
                id = getId();
                paramMap.put("ID", id);
                paramMap.put("SPL_PARENTID", toString(mapParam.get("SPL_PARENTID"), "0"));

                baseDao.insert(NameSpace.ProcessMapper, "insertProcessLog", paramMap);
//                resultMap.put(MagicValue.LOG, "添加流程日志:" + toString(paramMap));
            } else {
                Map<String, Object> oldMap = Maps.newHashMapWithExpectedSize(1);
                oldMap.put("ID", id);
                oldMap = selectProcessLog(oldMap);

                baseDao.update(NameSpace.ProcessMapper, "updateProcessLog", paramMap);
//                resultMap.put(MagicValue.LOG, "更新流程日志,更新前:" + toString(oldMap) + ",更新后:" + toString(paramMap));
            }
            status = STATUS_SUCCESS;
            desc = SAVE_SUCCESS;

            resultMap.put("ID", id);
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> deleteProcessLog(Map<String, Object> mapParam) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        int status = STATUS_ERROR;
        String desc = DELETE_ERROR;
        try {
            if (isEmpty(mapParam.get("ID"))) {
                throw new CustomException(Tips.ID_NULL_ERROR);
            }
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
            String id = toString(mapParam.get("ID"));

            //删除流程日志表
            paramMap.clear();
            paramMap.put("ID", id);
            Map<String, Object> oldMap = selectProcessLog(paramMap);

            baseDao.delete(NameSpace.ProcessMapper, "deleteProcessLog", paramMap);

//            resultMap.put(MagicValue.LOG, "删除流程日志,信息:" + toString(oldMap));
            status = STATUS_SUCCESS;
            desc = DELETE_SUCCESS;
        } catch (Exception e) {
            desc = catchException(e, baseDao, resultMap);
        }
        resultMap.put(MagicValue.STATUS, status);
        resultMap.put(MagicValue.DESC, desc);
        return resultMap;
    }

    @Override
    public Map<String, Object> selectProcessScheduleCancel(Map<String, Object> mapParam) {
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
        paramMap.put("ID", mapParam.get("ID"));
        return baseDao.selectOne(NameSpace.ProcessMapper, "selectProcessScheduleCancel", paramMap);
    }

    private List<Tree> getDefinitionTree(List<Map<String, Object>> definitionList, String selectId) {
        List<Tree> definitionTreeList = Lists.newArrayList();
        if (!isEmpty(definitionList)) {
            definitionList.forEach(definition -> {
                String id = toString(definition.get("ID"));

                Tree tree = new Tree();
                tree.setId(id);
                tree.setText(toString(definition.get("SPD_NAME")));
                tree.setTags(new String[]{
                        "更新表名称字段:" + toHtmlBColor(toString(definition.get("SPD_UPDATE_NAME"))),
                        "更新表名:" + toHtmlBColor(toString(definition.get("SPD_UPDATE_TABLE")))
                });

                TreeState state = new TreeState();
                //是否选中
                if (!isEmpty(selectId) && id.equals(selectId)) {
                    state.setChecked(true);
                    //选中的设置打开
                    state.setExpanded(true);
                }

                //设置状态
                tree.setState(state);

                definitionTreeList.add(tree);
            });
        }

        return definitionTreeList;
    }

    /**
     * 流程是否停用
     *
     * @param id
     * @return
     */
    private boolean isProcessDiscontinuation(String id) {
        if (isEmpty(id)) {
            return true;
        }
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
        paramMap.put("ID", id);
        paramMap = this.selectProcessDefinition(paramMap);
        if (isEmpty(paramMap)) {
            return true;
        }

        return isDiscontinuation(paramMap);
    }
}
