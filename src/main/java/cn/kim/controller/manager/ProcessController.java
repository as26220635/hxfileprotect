package cn.kim.controller.manager;

import cn.kim.common.annotation.NotEmptyLogin;
import cn.kim.common.annotation.SystemControllerLog;
import cn.kim.common.annotation.Token;
import cn.kim.common.annotation.Validate;
import cn.kim.common.attr.MagicValue;
import cn.kim.common.eu.ProcessBranch;
import cn.kim.common.eu.ProcessStatus;
import cn.kim.common.eu.ProcessType;
import cn.kim.common.eu.UseType;
import cn.kim.entity.CustomParam;
import cn.kim.entity.DataTablesView;
import cn.kim.entity.ResultState;
import cn.kim.entity.Tree;
import cn.kim.exception.CustomException;
import cn.kim.service.MenuService;
import cn.kim.service.OperatorService;
import cn.kim.service.ProcessService;
import cn.kim.service.RoleService;
import cn.kim.tools.ProcessTool;
import cn.kim.util.DictUtil;
import cn.kim.util.FuncUtil;
import cn.kim.util.TextUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by 余庚鑫 on 2018/5/22
 * 流程管理
 */
@Controller
@RequestMapping("/admin/process")
public class ProcessController extends BaseController {

    @Autowired
    private ProcessService processService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private OperatorService operatorService;

    /********   流程    ********/

    /**
     * 获取当前流程拥有的按钮
     *
     * @param ID
     * @param BUS_PROCESS
     * @param BUS_PROCESS2
     * @return
     * @throws Exception
     */
    @GetMapping("/showDataGridBtn")
    @NotEmptyLogin
    @ResponseBody
    public Map<String, Object> showDataGridBtn(String ID, String BUS_PROCESS, String BUS_PROCESS2) throws Exception {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(1);
        //查询当前角色拥有的按钮
        String btnTypes = processService.showDataGridProcessBtn(ID, BUS_PROCESS, BUS_PROCESS2, "0");
        resultMap.put("html", ProcessTool.getProcessButtonListHtml(btnTypes, false));
        return resultMap;
    }

    /**
     * 获取流程审核状态
     *
     * @param ID
     * @param BUS_PROCESS
     * @param BUS_PROCESS2
     * @return
     * @throws Exception
     */
    @GetMapping("/getProcessAuditStatus")
    @NotEmptyLogin
    @ResponseBody
    public String getProcessAuditStatus(String ID, String BUS_PROCESS, String BUS_PROCESS2) throws Exception {
        return ProcessTool.getProcessAuditStatus(ID, BUS_PROCESS, BUS_PROCESS2);
    }

    /**
     * 当前登录角色是否拥有随时编辑
     *
     * @param BUS_PROCESS
     * @param BUS_PROCESS2
     * @return
     * @throws Exception
     */
    @GetMapping("/showDataGridIsEdit")
    @NotEmptyLogin
    @ResponseBody
    public boolean showDataGridIsEdit(String BUS_PROCESS, String BUS_PROCESS2) throws Exception {
        return ProcessTool.selectNowActiveProcessStepIsEdit(BUS_PROCESS, BUS_PROCESS2);
    }

    /**
     * 获取流程提交退回界面
     *
     * @param ID
     * @param SHOW_SO_ID
     * @param SPS_ID       流程ID
     * @param BUS_PROCESS
     * @param BUS_PROCESS2
     * @param PROCESS_TYPE
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/showDataGridProcess")
    @NotEmptyLogin
    @Token(save = true)
    public String showDataGridProcess(String ID, String SHOW_SO_ID, String SPS_ID, String BUS_PROCESS, String BUS_PROCESS2, int PROCESS_TYPE, Model model) throws Exception {
        try {
            //取第一个ID拿来查询流程
            String[] tableIds = ID.split(SERVICE_SPLIT);
            String processBtnType = ProcessType.SUBMIT.toString();
            List<Map<String, Object>> transactorList = Lists.newArrayList();

            //判断是否处于同一流程同一状态
            if (!ProcessTool.checkProcessUnified(tableIds)) {
                throw new CustomException("流程或流程状态不统一!");
            }
            //判断当前是否拥有审核权限
            if (!ProcessTool.checkProcess(PROCESS_TYPE, tableIds[0], BUS_PROCESS, BUS_PROCESS2)) {
                throw new CustomException("当前用户没有审核权限!");
            }

            //根据大小类查询流程定义
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(5);
            paramMap.put("BUS_PROCESS", BUS_PROCESS);
            paramMap.put("BUS_PROCESS2", BUS_PROCESS2);
            Map<String, Object> definition = processService.selectProcessDefinition(paramMap);
            String SPD_ID = toString(definition.get("ID"));
            //查询当前流程办理步骤
            Map<String, Object> schedule = null;
            if (!isEmpty(SPS_ID)) {
                paramMap.clear();
                paramMap.put("ID", SPS_ID);
                schedule = processService.selectProcessSchedule(paramMap);
            } else {
                paramMap.clear();
                paramMap.put("SPS_TABLE_ID", tableIds[0]);
                paramMap.put("SPD_ID", SPD_ID);
                paramMap.put("SPS_IS_CANCEL", toString(STATUS_ERROR));
                paramMap.put("SPS_PARENTID", "0");
                schedule = processService.selectProcessSchedule(paramMap);
            }

            String SPS_PARENTID = "0";
            if (!isEmpty(schedule)) {
                SPS_ID = toString(schedule.get("ID"));

                paramMap.clear();
                paramMap.put("ID", schedule.get("SPS_ID"));
                Map<String, Object> nowStep = processService.selectProcessStep(paramMap);
                SPS_PARENTID = toString(nowStep.get("SPS_PARENTID"));
            }

            //审核状态
            String SPS_AUDIT_STATUS;
            if (!isEmpty(schedule) && !"0".equals(toString(schedule.get("SPS_AUDIT_STATUS")))) {
                SPS_AUDIT_STATUS = toString(schedule.get("SPS_AUDIT_STATUS"));
                if (toInt(SPS_AUDIT_STATUS) == ProcessStatus.BACK.getType()) {
                    SPS_AUDIT_STATUS = toString(schedule.get("SPS_BACK_STATUS_TRANSACTOR"));
                }
            } else {
                //查询角色拥有的最高的申报角色
                Map<String, Object> topStep = processService.processStepStartRoleTop(SPD_ID, activeUser().getRoleIds());
                if (!isEmpty(topStep)) {
                    SPS_AUDIT_STATUS = toString(topStep.get("SPS_PROCESS_STATUS"));
                } else {
                    throw new CustomException("当前用户没有提交审核权限!");
                }
            }

            //拼接提交项目
            List<CustomParam> submitProcessList = Lists.newArrayList();
            for (String tableId : tableIds) {
                CustomParam customParam = new CustomParam(ProcessTool.selectProcessTableName(tableId, BUS_PROCESS, BUS_PROCESS2), tableId);
                customParam.setDefaultParam(true);
                customParam.setEncrypt(true);
                submitProcessList.add(customParam);
            }

            paramMap.clear();
            paramMap.put("SPD_ID", SPD_ID);
            paramMap.put("SPS_PROCESS_STATUS", SPS_AUDIT_STATUS);
            if (!isEmpty(schedule) && toInt(schedule.get("SPS_AUDIT_STATUS")) != ProcessStatus.BACK.getType()) {
                paramMap.put("ID", schedule.get("SPS_ID"));
            } else {
                paramMap.put("SPS_PARENTID", SPS_PARENTID);
            }
            Map<String, Object> step = processService.selectProcessStep(paramMap);

            //获取下一步骤办理人
            paramMap.clear();
            paramMap.put("processType", PROCESS_TYPE);
            paramMap.put("tableId", ID);
            paramMap.put("definitionId", SPD_ID);
            paramMap.put("scheduleId", SPS_ID);
            paramMap.put("scheduleAuditStatus", SPS_AUDIT_STATUS);
            Map<String, Object> transactorMap = processService.getProcessTransactor(paramMap);
            Map<String, Object> nextStep = (Map<String, Object>) toMap(transactorMap.get("nextStep"));

            int SPS_STEP_BRANCH = toInt(nextStep.get("SPS_STEP_BRANCH"));
            //设置提交类型
            model.addAttribute("SPS_STEP_BRANCH", SPS_STEP_BRANCH);

            //默认意见
            String DEFAULT_OPINION = "";
            //查询下一步骤
            if (PROCESS_TYPE == ProcessType.SUBMIT.getType()) {
                //流程为提交
                if (SPS_STEP_BRANCH == ProcessBranch.FIXED.getType()) {
                    //固定
                    transactorList = toList(transactorMap.get("transactorList"));
                } else if (SPS_STEP_BRANCH == ProcessBranch.BRANCH.getType() || SPS_STEP_BRANCH == ProcessBranch.CONCURRENT.getType()) {
                    //分支 并发
                    model.addAttribute("childrenTransactorList", transactorMap.get("childrenTransactorList"));
                }

//                DEFAULT_OPINION = "通过";
                processBtnType = ProcessType.SUBMIT.toString();
            } else if (PROCESS_TYPE == ProcessType.BACK.getType()) {
                //查询上一步骤办理人
                transactorList = toList(transactorMap.get("transactorList"));
//                DEFAULT_OPINION = "退回";
                processBtnType = ProcessType.BACK.toString();
            }

            //查询流程步骤
            paramMap.clear();
            paramMap.put("SPD_ID", SPD_ID);
            if (isEmpty(schedule)) {
                paramMap.put("SPS_PARENTID", "0");
            } else {
                paramMap.put("SPS_PARENTID", nextStep.get("SPS_PARENTID"));
            }
            List<Map<String, Object>> stepList = processService.selectProcessStepList(paramMap);
            //设置流程步骤
            String stepId = toString(step.get("ID"));
            String nextStepId = toString(nextStep.get("ID"));
            StringBuilder stepGroupName = new StringBuilder();
            stepGroupName.append("开始" + MagicValue.RIGHT_ARROW);
            FuncUtil.forEach(stepList, (index, map) -> {
                Object SPS_NAME = map.get("SPS_NAME");
                if (toString(map.get("ID")).equals(stepId)) {
                    stepGroupName.append(TextUtil.joinFirstTextSymbol("<span style='color:blue;'>" + SPS_NAME + "(当前步骤)" + MagicValue.RIGHT_ARROW + "</span>", MagicValue.NBSP, 1));
                } else if (toString(map.get("ID")).equals(nextStepId)) {
                    if (index + 1 == stepList.size()) {
                        stepGroupName.append(TextUtil.joinFirstTextSymbol("<span style='color:red;'>" + SPS_NAME + "(下一步骤)" + "</span>", MagicValue.NBSP, 1));
                    } else {
                        stepGroupName.append(TextUtil.joinFirstTextSymbol("<span style='color:red;'>" + SPS_NAME + "(下一步骤)" + MagicValue.RIGHT_ARROW + "</span>", MagicValue.NBSP, 1));
                    }
                } else {
                    stepGroupName.append(TextUtil.joinFirstTextSymbol(SPS_NAME + MagicValue.RIGHT_ARROW, MagicValue.NBSP, 1));
                }
            });

            //是否子流程全部完成 查询进入下一步的步骤
            paramMap.clear();
            paramMap.put("processType", PROCESS_TYPE);
            paramMap.put("tableId", ID);
            paramMap.put("definitionId", SPD_ID);
            paramMap.put("scheduleId", SPS_ID);
            paramMap.put("nextStepId", nextStepId);
            Map<String, Object> parentProcessMap = processService.getParentProcessNextTransactor(paramMap);

            boolean isSelectParent = toBoolean(parentProcessMap.get("isSelectParent"));
            //是要提交父流程 设置参数
            if (isSelectParent) {
                for (Map.Entry<String, Object> entry : parentProcessMap.entrySet()) {
                    model.addAttribute(entry.getKey(), entry.getValue());
                }
            }

            //流程步骤
            model.addAttribute("SPS_GROUP_NAME", TextUtil.interceptSymbol(stepGroupName.toString(), MagicValue.RIGHT_ARROW));
            //办理表ID
            model.addAttribute("SPS_TABLE_ID", ID);
            //查看人SO_ID
            if (!isEmpty(SHOW_SO_ID)) {
                model.addAttribute("SHOW_SO_ID", SHOW_SO_ID);
            }
            //现流程ID
            model.addAttribute("NOW_SCHEDULE_ID", SPS_ID);
            //下一步办理人
            model.addAttribute("TRANSACTOR_LIST", transactorList);
            //流程定义
            model.addAttribute("SPD", definition);
            //当前步骤
            model.addAttribute("STEP", step);
            //下一步步骤
            model.addAttribute("NEXT_STEP", nextStep);
            //流程办理类型
            model.addAttribute("PROCESS_TYPE", PROCESS_TYPE);
            //默认办理意见
            model.addAttribute("DEFAULT_OPINION", DEFAULT_OPINION);
            //提交项目list
            model.addAttribute("submitProcessList", submitProcessList);
            //步骤名称
            model.addAttribute("STEP_NAME", DictUtil.getDictName("SYS_PROCESS_STATUS", SPS_AUDIT_STATUS));
            //下一步步骤
            model.addAttribute("processBtnType", processBtnType);
        } catch (CustomException e) {
            model.addAttribute("message", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            //错误消息
            model.addAttribute("message", "流程查询出错!");
        }
        return "admin/component/process/processHome";
    }

    /**
     * 提交流程
     *
     * @param mapParam
     * @return
     */
    @PutMapping("/submit")
    @NotEmptyLogin
    @Token(remove = true)
    @Validate(value = {"SYS_PROCESS_LOG", "SYS_PROCESS_SCHEDULE"})
    @ResponseBody
    public ResultState submit(@RequestParam Map<String, Object> mapParam) throws Exception {
        //办理类型
        int PROCESS_TYPE = toInt(mapParam.get("PROCESS_TYPE"));
        //流程办理ID
        String[] tableIds = toString(mapParam.get("SPS_TABLE_ID")).split(SERVICE_SPLIT);
        String[] tableNames = toString(mapParam.get("SPS_TABLE_NAME")).split(SERVICE_SPLIT);
        //拿到大小类
        String BUS_PROCESS = toString(mapParam.get("BUS_PROCESS"));
        String BUS_PROCESS2 = toString(mapParam.get("BUS_PROCESS2"));

        //判断是否处于同一流程同一状态
        if (!ProcessTool.checkProcessUnified(tableIds)) {
            throw new CustomException("流程或流程状态不统一!");
        }

        if (!ProcessTool.checkProcess(PROCESS_TYPE, tableIds[0], BUS_PROCESS, BUS_PROCESS2)) {
            throw new CustomException("当前用户没有审核权限!");
        }

        //错误消息
        StringBuilder errorBuilder = new StringBuilder();
        //循环提交
        for (int i = 0; i < tableIds.length; i++) {
            String tableId = tableIds[i];
            String tableName = tableNames[i];
            if (isEmpty(tableId)){
                continue;
            }

            mapParam.put("SPS_TABLE_NAME", tableName);
            mapParam.put("SPS_TABLE_ID", tableId);
            //公平锁
            ResultState state = fairLock(tableId, () -> {
                Map<String, Object> resultMap = ProcessTool.submitProcess(mapParam);

                return resultState(resultMap);
            });
            //只有1个流程直接返回
            if (tableIds.length == 1) {
                return state;
            }
            //判断返回结果
            if (state.getCode() == STATUS_ERROR) {
                errorBuilder.append("流程:" + tableName + ",异常原因:" + state.getMessage() + ",请修改后再次尝试!<br/>");
            }
        }
        if (isEmpty(errorBuilder)) {
            return resultSuccess("流程批量提交成功!", "流程批量提交成功,SPS_TABLE_IDS:" + toString(tableIds));
        } else {
            return resultError("异常流程:<br/>" + errorBuilder.toString(), "流程提交成功!出现问题流程:" + errorBuilder.toString());
        }
    }

    /**
     * 撤回流程
     *
     * @param mapParam
     * @return
     */
    @PutMapping("/withdraw")
    @NotEmptyLogin
    @Validate(value = {"SYS_PROCESS_LOG", "SYS_PROCESS_SCHEDULE"})
    @ResponseBody
    public ResultState withdraw(@RequestParam Map<String, Object> mapParam) throws Exception {
        //流程办理ID
        String SPS_TABLE_ID = toString(mapParam.get("SPS_TABLE_ID"));
        //拿到大小类
        String BUS_PROCESS = toString(mapParam.get("BUS_PROCESS"));
        String BUS_PROCESS2 = toString(mapParam.get("BUS_PROCESS2"));
        if (!ProcessTool.checkProcess(ProcessType.WITHDRAW.getType(), SPS_TABLE_ID, BUS_PROCESS, BUS_PROCESS2)) {
            throw new CustomException("当前用户没有审核权限!");
        }

        //公平锁进行等待
        return fairLock(SPS_TABLE_ID, () -> {
            Map<String, Object> resultMap = ProcessTool.withdrawProcess(mapParam);

            return resultState(resultMap);
        });
    }

    /**
     * 流程日志
     *
     * @param ID
     * @param BUS_PROCESS
     * @param BUS_PROCESS2
     * @param SPS_ID
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/log")
    @NotEmptyLogin
    public String log(String ID, String BUS_PROCESS, String BUS_PROCESS2, String SPS_ID, Model model) throws Exception {

        int SPS_STEP_BRANCH = ProcessBranch.FIXED.getType();

        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(3);
        mapParam.put("BUS_PROCESS", BUS_PROCESS);
        mapParam.put("BUS_PROCESS2", BUS_PROCESS2);
        Map<String, Object> definition = processService.selectProcessDefinition(mapParam);
        String SPD_ID = toString(definition.get("ID"));

        //列表日志使用
        if (isEmpty(SPS_ID)) {
            mapParam.clear();
            mapParam.put("SPS_TABLE_ID", ID);
            mapParam.put("SPD_ID", SPD_ID);
            mapParam.put("SPS_IS_CANCEL", toString(STATUS_ERROR));
            mapParam.put("SPS_PARENTID", "0");
            Map<String, Object> schedule = processService.selectProcessSchedule(mapParam);
            //流程进度ID
            SPS_ID = toString(schedule.get("ID"));
        }

        mapParam.clear();
        mapParam.put("SPD_ID", SPD_ID);
        mapParam.put("SPS_PARENTID", "0");
        List<Map<String, Object>> stepList = processService.selectProcessStepList(mapParam);
        for (Map<String, Object> step : stepList) {
            SPS_STEP_BRANCH = toInt(step.get("SPS_STEP_BRANCH"), ProcessBranch.FIXED.getType());
            if (SPS_STEP_BRANCH != ProcessBranch.FIXED.getType()) {
                break;
            }
        }

        mapParam.clear();
        mapParam.put("SPS_ID", SPS_ID);
        mapParam.put("SPL_TABLE_ID", ID);
        Map<String, Object> log = processService.selectProcessLogList(mapParam).get(0);

        //流程其他信息
        model.addAttribute("SPS_TABLE_ID", ID);
        model.addAttribute("SPD_ID", SPD_ID);
        model.addAttribute("SPL_PARENTID", log.get("SPL_PARENTID"));
        //流程进度ID
        model.addAttribute("SPS_ID", SPS_ID);
        model.addAttribute("SPS_STEP_BRANCH", SPS_STEP_BRANCH);

        return "admin/component/process/processLog";
    }

    /**
     * 流程日志数据
     *
     * @param SPS_ID
     * @param SPD_ID
     * @param SPS_TABLE_ID
     * @param SPL_PARENTID
     * @return
     * @throws Exception
     */
    @GetMapping("/log/list")
    @NotEmptyLogin
    @ResponseBody
    public DataTablesView<?> logList(String SPS_ID, String SPD_ID, String SPS_TABLE_ID, String SPL_PARENTID) throws Exception {
        DataTablesView<Map<String, Object>> dataTablesView = new DataTablesView<>();
        //查询日志数据
        List<Map<String, Object>> logList = null;

        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("SPS_ID", SPS_ID);
        if (isEmpty(SPD_ID)) {
            logList = processService.selectProcessLogList(mapParam);
        } else {
            logList = processService.selectProcessLogList(0, SPD_ID, SPS_ID, SPS_TABLE_ID, SPL_PARENTID);
        }

        dataTablesView.setData(logList);
        return dataTablesView;
    }
    /********   流程定义    ********/

    /**
     * 获取流程定义列表 转为List<Tree>
     *
     * @param ID 已选角色ID
     * @return
     * @throws Exception
     */
    @GetMapping("/definition/tree")
    @RequiresPermissions(value = {"SYSTEM:MENU"}, logical = Logical.OR)
    @ResponseBody
    public List<Tree> definitionTree(String ID) throws Exception {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("ID", ID);
        List<Tree> definitionTreeList = processService.selectProcessDefinitionTreeList(mapParam);

        return definitionTreeList;
    }

    @GetMapping("/definition/add")
    @RequiresPermissions("SYSTEM:PROCESS_DEFINITION_INSERT")
    @Token(save = true)
    public String addHtmlDefinition(Model model) throws Exception {
        return "admin/system/process/definition/add";
    }


    @PostMapping("/definition/add")
    @RequiresPermissions("SYSTEM:PROCESS_DEFINITION_INSERT")
    @SystemControllerLog(useType = UseType.USE, event = "添加流程定义")
    @Token(remove = true)
    @Validate("SYS_PROCESS_DEFINITION")
    @ResponseBody
    public ResultState addDefinition(@RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = processService.insertAndUpdateProcessDefinition(mapParam);

        return resultState(resultMap);
    }


    @GetMapping("/definition/update")
    @RequiresPermissions("SYSTEM:PROCESS_DEFINITION_UPDATE")
    public String updateHtmlDefinition(String SPD_ID, String SM_ID, Model model) throws Exception {
        isNotFound(SPD_ID);
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("ID", SPD_ID);
        model.addAttribute("SPD", processService.selectProcessDefinition(mapParam));
        //查询菜单
        model.addAttribute("MENU", menuService.queryMenuById(SM_ID));
        return "admin/system/process/definition/edit";
    }

    @PutMapping("/definition/update")
    @RequiresPermissions("SYSTEM:PROCESS_DEFINITION_UPDATE_SAVE")
    @SystemControllerLog(useType = UseType.USE, event = "修改流程定义")
    @Validate("SYS_PROCESS_DEFINITION")
    @ResponseBody
    public ResultState updateDefinition(@RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = processService.insertAndUpdateProcessDefinition(mapParam);
        return resultState(resultMap);
    }

    @PutMapping("/definition/switchStatus")
    @RequiresPermissions("SYSTEM:PROCESS_DEFINITION_UPDATE")
    @SystemControllerLog(useType = UseType.USE, event = "修改流程定义状态")
    @ResponseBody
    public ResultState switchStatusDefinition(@RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = processService.changeProcessDefinitionStatus(mapParam);

        return resultState(resultMap);
    }

    @GetMapping("/definition/copy/{ID}")
    @RequiresPermissions("SYSTEM:PROCESS_DEFINITION_COPY")
    public String copyHtmlDefinition(@PathVariable("ID") String ID, Model model) throws Exception {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("ID", ID);
        model.addAttribute("SPD", processService.selectProcessDefinition(mapParam));
        return "admin/system/process/definition/copy";
    }

    @PutMapping("/definition/copy")
    @RequiresPermissions("SYSTEM:PROCESS_DEFINITION_COPY")
    @SystemControllerLog(useType = UseType.USE, event = "拷贝流程定义")
    @Validate("SYS_PROCESS_DEFINITION")
    @ResponseBody
    public ResultState copyDefinition(@RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = processService.copyProcessDefinition(mapParam);
        return resultState(resultMap);
    }

    /********   流程步骤    ********/

    @GetMapping("/step/add")
    @RequiresPermissions("SYSTEM:PROCESS_STEP_INSERT")
    @Token(save = true)
    public String addHtmlProcessStep(String SPD_ID, Model model) throws Exception {
        model.addAttribute("SPD_ID", SPD_ID);
        return "admin/system/process/step/addAndEdit";
    }

    @PostMapping("/step/add")
    @RequiresPermissions("SYSTEM:PROCESS_STEP_INSERT")
    @SystemControllerLog(useType = UseType.USE, event = "添加流程步骤")
    @Token(remove = true)
    @Validate("SYS_PROCESS_STEP")
    @ResponseBody
    public ResultState addProcessStep(@RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = processService.insertAndUpdateProcessStep(mapParam);

        return resultState(resultMap);
    }

    @GetMapping("/step/update/{ID}")
    @RequiresPermissions("SYSTEM:PROCESS_STEP_UPDATE")
    public String updateHtmlProcessStep(Model model, @PathVariable("ID") String ID) throws Exception {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("ID", ID);
        model.addAttribute("SPS", processService.selectProcessStep(mapParam));
        return "admin/system/process/step/addAndEdit";
    }

    @PutMapping("/step/update")
    @RequiresPermissions("SYSTEM:PROCESS_STEP_UPDATE")
    @SystemControllerLog(useType = UseType.USE, event = "修改流程步骤")
    @Validate("SYS_PROCESS_STEP")
    @ResponseBody
    public ResultState updateProcessStep(@RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = processService.insertAndUpdateProcessStep(mapParam);
        return resultState(resultMap);
    }

    @DeleteMapping("/step/delete/{ID}")
    @RequiresPermissions("SYSTEM:PROCESS_STEP_DELETE")
    @SystemControllerLog(useType = UseType.USE, event = "删除流程步骤")
    @ResponseBody
    public ResultState deleteProcessStep(@PathVariable("ID") String ID) throws Exception {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("ID", ID);
        Map<String, Object> resultMap = processService.deleteProcessStep(mapParam);
        return resultState(resultMap);
    }

    /********   流程启动角色    ********/

    @GetMapping("/start/add")
    @RequiresPermissions("SYSTEM:PROCESS_STEP_INSERT")
    @Token(save = true)
    public String addHtmlProcessStart(String SPD_ID, Model model) throws Exception {
        model.addAttribute("SPD_ID", SPD_ID);
        return "admin/system/process/start/addAndEdit";
    }

    @PostMapping("/start/add")
    @RequiresPermissions("SYSTEM:PROCESS_STEP_INSERT")
    @SystemControllerLog(useType = UseType.USE, event = "添加流程启动角色")
    @Token(remove = true)
    @Validate("SYS_PROCESS_STEP")
    @ResponseBody
    public ResultState addProcessStart(@RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = processService.insertAndUpdateProcessStart(mapParam);

        return resultState(resultMap);
    }

    @GetMapping("/start/update/{ID}")
    @RequiresPermissions("SYSTEM:PROCESS_STEP_UPDATE")
    public String updateHtmlProcessStart(Model model, @PathVariable("ID") String ID) throws Exception {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("ID", ID);
        model.addAttribute("SPS", processService.selectProcessStart(mapParam));
        return "admin/system/process/start/addAndEdit";
    }

    @PutMapping("/start/update")
    @RequiresPermissions("SYSTEM:PROCESS_STEP_UPDATE")
    @SystemControllerLog(useType = UseType.USE, event = "修改流程启动角色")
    @Validate("SYS_PROCESS_STEP")
    @ResponseBody
    public ResultState updateProcessStart(@RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = processService.insertAndUpdateProcessStart(mapParam);
        return resultState(resultMap);
    }

    @DeleteMapping("/start/delete/{ID}")
    @RequiresPermissions("SYSTEM:PROCESS_STEP_DELETE")
    @SystemControllerLog(useType = UseType.USE, event = "删除流程启动角色")
    @ResponseBody
    public ResultState deleteProcessStart(@PathVariable("ID") String ID) throws Exception {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("ID", ID);
        Map<String, Object> resultMap = processService.deleteProcessStart(mapParam);
        return resultState(resultMap);
    }

    /********   流程进度    ********/

    @GetMapping("/schedule/{ID}")
    @RequiresPermissions("SYSTEM:PROCESS_SCHEDULE")
    @Token(save = true)
    public String cancelHtmlProcessSchedule(@PathVariable("ID") String ID, Model model) throws Exception {
        model.addAttribute("ID", ID);
        return "admin/system/process/schedule/addAndEdit";
    }

    @PutMapping("/schedule/cancel")
    @RequiresPermissions("SYSTEM:PROCESS_SCHEDULE_CANCEL")
    @SystemControllerLog(useType = UseType.USE, event = "作废流程进度")
    @Token(remove = true)
    @Validate("SYS_PROCESS_SCHEDULE_CANCEL")
    @ResponseBody
    public ResultState cancelProcessSchedule(@RequestParam Map<String, Object> mapParam) throws Exception {
        String ID = toString(mapParam.get("ID"));

        return fairLock(ID, () -> {
            Map<String, Object> resultMap = ProcessTool.cancelProcess(ID, toString(mapParam.get("SPSC_REASON")));
            return resultState(resultMap);
        });
    }

    /**
     * 流程进度作废信息
     *
     * @param ID
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/schedule/cancel/{ID}")
    @RequiresPermissions("SYSTEM:PROCESS_SCHEDULE_CANCEL_INFO")
    @Token(save = true)
    public String cancelHtmlProcessScheduleInfo(@PathVariable("ID") String ID, Model model) throws Exception {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("ID", ID);
        Map<String, Object> cancel = processService.selectProcessScheduleCancel(mapParam);

        model.addAttribute("SPSC", cancel);
        return "admin/system/process/schedule/addAndEdit";
    }


    /********   时间控制    ********/

    @GetMapping("/timeControl/add")
    @RequiresPermissions("SYSTEM:PROCESS_TIME_CONTROL_INSERT")
    @Token(save = true)
    public String addHtmlProcessTimeControl(String SPD_ID, Model model) throws Exception {
        model.addAttribute("SPD_ID", SPD_ID);
        return "admin/system/process/timeControl/addAndEdit";
    }

    @PostMapping("/timeControl/add")
    @RequiresPermissions("SYSTEM:PROCESS_TIME_CONTROL_INSERT")
    @SystemControllerLog(useType = UseType.USE, event = "添加流程时间控制")
    @Token(remove = true)
    @Validate("SYS_PROCESS_TIME_CONTROL")
    @ResponseBody
    public ResultState addProcessTimeControl(@RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = processService.insertAndUpdateProcessTimeControl(mapParam);

        return resultState(resultMap);
    }

    @GetMapping("/timeControl/update/{ID}")
    @RequiresPermissions("SYSTEM:PROCESS_TIME_CONTROL_UPDATE")
    public String updateHtmlProcessTimeControl(Model model, @PathVariable("ID") String ID) throws Exception {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("ID", ID);
        model.addAttribute("SPTC", processService.selectProcessTimeControl(mapParam));
        return "admin/system/process/timeControl/addAndEdit";
    }

    @PutMapping("/timeControl/update")
    @RequiresPermissions("SYSTEM:PROCESS_TIME_CONTROL_UPDATE")
    @SystemControllerLog(useType = UseType.USE, event = "修改流程时间控制")
    @Validate("SYS_PROCESS_TIME_CONTROL")
    @ResponseBody
    public ResultState updateProcessTimeControl(@RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = processService.insertAndUpdateProcessTimeControl(mapParam);
        return resultState(resultMap);
    }

    @PutMapping("/timeControl/switchStatus")
    @RequiresPermissions("SYSTEM:PROCESS_TIME_CONTROL_UPDATE")
    @SystemControllerLog(useType = UseType.USE, event = "修改流程时间控制状态")
    @ResponseBody
    public ResultState switchStatusProcessTimeControl(@RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = processService.changeProcessTimeControlStatus(mapParam);

        return resultState(resultMap);
    }

    @DeleteMapping("/timeControl/delete/{ID}")
    @RequiresPermissions("SYSTEM:PROCESS_TIME_CONTROL_DELETE")
    @SystemControllerLog(useType = UseType.USE, event = "删除流程时间控制")
    @ResponseBody
    public ResultState deleteProcessTimeControl(@PathVariable("ID") String ID) throws Exception {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("ID", ID);
        Map<String, Object> resultMap = processService.deleteProcessTimeControl(mapParam);
        return resultState(resultMap);
    }
}
