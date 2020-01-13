package cn.kim.service;

import cn.kim.entity.Tree;
import cn.kim.exception.CustomException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 余庚鑫 on 2018/6/5
 */
public interface ProcessService extends BaseService {
    /****   流程    ***/
    /**
     * 查询流程项目名称
     *
     * @param tableId
     * @param busProcess
     * @param busProcess2
     * @return
     */
    String selectProcessTableName(String tableId, String busProcess, String busProcess2);

    /**
     * -1 无 0 提交按钮 1 撤回按钮
     *
     * @param id
     * @param process
     * @param process2
     * @return
     */
    String showDataGridProcessBtn(String id, String process, String process2, String SPS_PARENTID) throws Exception;

    /**
     * 获取当前项目的下一步步骤
     *
     * @param isSkipBranch
     * @param definitionId
     * @param scheduleAuditStatus
     * @param stepParentId
     * @return
     */
    Map<String, Object> processNextStep(boolean isSkipBranch, String definitionId, String scheduleAuditStatus, String stepParentId);

    /**
     * 获取当前项目的上一步步骤
     *
     * @param definitionId        流程定义ID
     * @param scheduleAuditStatus 流程办理状态
     * @param stepParentId
     * @return
     */
    Map<String, Object> processPrevStep(String definitionId, String scheduleAuditStatus, String stepParentId);

    /**
     * 获取当前项目的之前全部步骤
     *
     * @param definitionId
     * @param scheduleAuditStatus
     * @return
     */
    List<Map<String, Object>> processPrevStepList(String definitionId, String scheduleAuditStatus, String stepParentId);

    /**
     * 根据当前角色ids查询流程定义最高的启动角色
     *
     * @param definitionId
     * @param roleIds
     * @return
     */
    Map<String, Object> processStepStartRoleTop(String definitionId, String roleIds);

    /**
     * 提交流程
     *
     * @param mapParam
     * @return
     */
    Map<String, Object> processSubmit(Map<String, Object> mapParam);

    /**
     * 撤回流程
     *
     * @param mapParam
     * @return
     */
    Map<String, Object> processWithdraw(Map<String, Object> mapParam);

    /**
     * 获取下一步办理人
     *
     * @param mapParam
     * @return
     * @throws Exception
     */
    Map<String, Object> getProcessTransactor(Map<String, Object> mapParam) throws Exception;

    /**
     * 获取子流程办理完成下一步选择人
     *
     * @param mapParam
     * @return
     * @throws Exception
     */
    Map<String, Object> getParentProcessNextTransactor(Map<String, Object> mapParam) throws Exception;
    /****   流程定义    ***/

    /**
     * 根据ID查询流程定义
     *
     * @param ID
     * @return
     */
    Map<String, Object> selectProcessDefinitionById(String ID);

    Map<String, Object> selectProcessDefinition(Map<String, Object> mapParam);

    List<Tree> selectProcessDefinitionTreeList(Map<String, Object> mapParam);

    Map<String, Object> insertAndUpdateProcessDefinition(Map<String, Object> mapParam);

    Map<String, Object> changeProcessDefinitionStatus(Map<String, Object> mapParam);

    /**
     * 拷贝流程定义
     *
     * @param mapParam
     * @return
     */
    Map<String, Object> copyProcessDefinition(Map<String, Object> mapParam);

    /****   流程步骤    ***/
    Map<String, Object> selectProcessStep(Map<String, Object> mapParam);

    List<Map<String, Object>> selectProcessStepList(Map<String, Object> mapParam);

    /**
     * 是否拥有随时编辑
     *
     * @param roleId
     * @param busProcess
     * @param busProcess2
     * @return
     */
    boolean selectProcessStepIsEdit(String roleId, String busProcess, String busProcess2);

    Map<String, Object> insertAndUpdateProcessStep(Map<String, Object> mapParam);

    Map<String, Object> deleteProcessStep(Map<String, Object> mapParam);

    /****   流程开始角色    ***/
    Map<String, Object> selectProcessStart(Map<String, Object> mapParam);

    List<Map<String, Object>> selectProcessStartList(Map<String, Object> mapParam);

    Map<String, Object> insertAndUpdateProcessStart(Map<String, Object> mapParam);

    Map<String, Object> deleteProcessStart(Map<String, Object> mapParam);

    /****   流程控制时间    ***/
    Map<String, Object> selectProcessTimeControl(Map<String, Object> mapParam);

    Map<String, Object> insertAndUpdateProcessTimeControl(Map<String, Object> mapParam);

    Map<String, Object> changeProcessTimeControlStatus(Map<String, Object> mapParam);

    Map<String, Object> deleteProcessTimeControl(Map<String, Object> mapParam);

    /****   流程进度    ***/
    Map<String, Object> selectProcessSchedule(Map<String, Object> mapParam);

    Map<String, Object> insertAndUpdateProcessSchedule(Map<String, Object> mapParam);

    Map<String, Object> cancelProcessSchedule(Map<String, Object> mapParam);

    Map<String, Object> deleteProcessSchedule(Map<String, Object> mapParam);

    /****   流程日志    ***/
    Map<String, Object> selectProcessLog(Map<String, Object> mapParam);

    List<Map<String, Object>> selectProcessLogList(Map<String, Object> mapParam);

    /**
     * 查看日志
     *
     * @param level
     * @param definitionId
     * @param scheduleId
     * @param tableId
     * @param logParentId
     * @return
     */
    List<Map<String, Object>> selectProcessLogList(int level, String definitionId, String scheduleId, String tableId, String logParentId);

    Map<String, Object> insertProcessLog(Map<String, Object> mapParam);

    Map<String, Object> deleteProcessLog(Map<String, Object> mapParam);

    /****   流程进度作废    ***/
    Map<String, Object> selectProcessScheduleCancel(Map<String, Object> mapParam);
}
