package cn.kim.controller.manager;

import cn.kim.common.annotation.NotEmptyLogin;
import cn.kim.common.annotation.SystemControllerLog;
import cn.kim.common.annotation.Validate;
import cn.kim.common.eu.UseType;
import cn.kim.entity.ActiveUser;
import cn.kim.entity.ResultState;
import cn.kim.service.ManagerService;
import cn.kim.service.OperatorService;
import cn.kim.util.CommonUtil;
import cn.kim.util.TextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class FirstController extends BaseController {

    @Autowired
    private OperatorService operatorService;

    @Autowired
    private ManagerService managerService;

    @GetMapping("/editUser")
    @NotEmptyLogin
    public String queryUser(Model model) throws Exception {
        model.addAttribute("accountInfo", getAccountInfo());
        return "admin/system/first/editActiveUser";
    }


    @PostMapping("/editUser")
    @SystemControllerLog(useType = UseType.USE, event = "修改信息")
    @Validate("SYS_ACCOUNT_INFO")
    @NotEmptyLogin
    @ResponseBody
    public ResultState editUser(@RequestParam Map<String, Object> mapParam) throws Exception {
        String id = activeUser().getId();
        mapParam.put("ID", id);
        return fairLock(id, () -> {
            Map<String, Object> resultMap = operatorService.insertAndUpdateOperator(mapParam);
            return resultState(resultMap);
        });
    }


    /**
     * 后台修改密码
     *
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/editPwd")
    @NotEmptyLogin
    public String editPswdHtml(Model model) throws Exception {
        return "admin/system/first/editPwd";
    }

    /**
     * 前台修改密码
     *
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/editPwdR")
    @NotEmptyLogin
    public String editPswdR(Model model) throws Exception {
        return "admin/system/first/editPwdR";
    }


    @PostMapping("/editPwd")
    @SystemControllerLog(useType = UseType.USE, event = "修改密码")
    @NotEmptyLogin
    @ResponseBody
    public ResultState editPwd(@RequestParam Map<String, Object> mapParam) throws Exception {
        String id = activeUser().getId();
        mapParam.put("ID", id);
        return fairLock(id, () -> {
            Map<String, Object> resultMap = operatorService.updateOperatorPassword(mapParam);
            return resultState(resultMap);
        });
    }

    /**
     * 修改头像
     *
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/headPortrait")
    @NotEmptyLogin
    public String headPortrait(Model model) throws Exception {
        return "admin/system/first/headportrait";
    }

    /**
     * 保存头像
     *
     * @param fileUpload
     * @param mapParam
     * @return
     * @throws Exception
     */
    @PostMapping("/headPortrait")
    @SystemControllerLog(useType = UseType.USE, event = "修改头像")
    @NotEmptyLogin
    @ResponseBody
    public ResultState headPortrait(MultipartFile fileUpload, @RequestParam Map<String, Object> mapParam) throws Exception {
        Map<String, Object> resultMap = operatorService.headPortrait(fileUpload, mapParam);
        return resultState(resultMap);
    }

    /**
     * 后台首页
     *
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/home")
    @NotEmptyLogin
    public String home(Model model) throws Exception {
        return "admin/system/first/home";
    }
}
