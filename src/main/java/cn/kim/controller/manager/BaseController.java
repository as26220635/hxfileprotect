package cn.kim.controller.manager;

import cn.kim.common.BaseData;
import cn.kim.common.attr.Attribute;
import cn.kim.common.attr.Constants;
import cn.kim.common.attr.MagicValue;
import cn.kim.common.attr.Tips;
import cn.kim.controller.ManagerController;
import cn.kim.entity.ActiveUser;
import cn.kim.entity.ResultState;
import cn.kim.entity.Tree;
import cn.kim.entity.TreeState;
import cn.kim.exception.NotFoundException;
import cn.kim.interceptor.PjaxInterceptor;
import cn.kim.listener.LockListener;
import cn.kim.service.ManagerService;
import cn.kim.util.AuthcUtil;
import cn.kim.util.HttpUtil;
import cn.kim.util.ValidateUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by 余庚鑫 on 2018/3/22.
 */
public abstract class BaseController extends BaseData {

    /**
     * redis客户端
     */
    @Autowired
    protected RedissonClient redissonClient;

    @Autowired
    private ManagerService sysManagerService;

    /**
     * 用户
     *
     * @return
     */
    @ModelAttribute(Constants.SESSION_USERNAME)
    public ActiveUser activeUser() {
        return getActiveUser();
    }

    /**
     * 表单提交验证的token 名称
     *
     * @return
     */
    @ModelAttribute("SUBMIT_TOKEN_NAME")
    public String submitTokenName() {
        return Attribute.SUBMIT_TOKEN_NAME;
    }


    /**
     * 设置账号信息
     *
     * @param user
     */
    protected void setActiveUser(ActiveUser user) {
        AuthcUtil.setCurrentUser(user);
    }

    /**
     * 获取账号信息
     *
     * @return
     */
    protected Map<String, Object> getAccountInfo() {
        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(1);
        mapParam.put("SO_ID", getActiveUser().getId());
        return sysManagerService.queryAccountInfo(mapParam);
    }

    /******************     公用方法    *********************/
    /**
     * 设置默认ID
     *
     * @param model
     */
    protected void setInsertId(Model model) {
        model.addAttribute("insertId", getId());
    }

    /**
     * 设置默认ID
     *
     * @param model
     * @param isNull 传来参数为空的情况添加ID到model中
     */
    protected void setInsertId(Model model, Object isNull) {
        if (ValidateUtil.isEmpty(isNull)) {
            setInsertId(model);
        }
    }

    /**
     * redission 公平锁
     * 最多等待40秒，上锁以后20秒自动解锁
     *
     * @param name         锁名
     * @param lockListener 回调操作
     * @return
     * @throws InvalidKeyException
     */
    public ResultState fairLock(String name, LockListener lockListener) throws InvalidKeyException {
        return fairLock(name, 40, 20, lockListener);
    }

    /**
     * redission 公平锁
     *
     * @param name
     * @param waitTime
     * @param leaseTime
     * @param lockListener
     * @return
     * @throws InvalidKeyException
     */
    public ResultState fairLock(String name, long waitTime, long leaseTime, LockListener lockListener) throws InvalidKeyException {
        //采用公平锁
        RLock lock = redissonClient.getFairLock(name);
        try {
            //尝试加锁，最多等待20秒，上锁以后10秒自动解锁
            boolean res = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);

            if (res) {
                return lockListener.lock();
            } else {
                return resultError();
            }
        } catch (Exception e) {
            return resultError(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 吧List<Map<String,Object>> 格式的权限转为TREE
     *
     * @param list
     * @return
     */
    protected List<Tree> toMenuTreeData(List<Map<String, Object>> list) {
        List<Tree> resultTrees = Lists.newArrayList();
        for (Map<String, Object> menu : list) {
            Tree tree = new Tree();
            tree.setId(toString(menu.get("ID")));
            tree.setText(toString(menu.get("SM_NAME")));
            tree.setTags(new String[]{toHtmlBColor(menu.get("SM_CODE"))});

            TreeState state = new TreeState();
            //是否选中
            state.setChecked(toBoolean(menu.get("IS_HAVE")));
            //选中的设置打开
            if (state.isChecked()) {
                state.setExpanded(true);
            }
            //禁止选择
            Integer isStatus = toInt(menu.get("IS_STATUS"));
            if (!isEmpty(isStatus) && isStatus == STATUS_ERROR) {
                state.setDisabled(true);
            }
            //设置状态
            tree.setState(state);

            //递归
            List<Map<String, Object>> childrenMenus = toList(menu.get("CHILDREN_MENU"));
            if (childrenMenus != null && childrenMenus.size() > 0) {
                tree.setNodes(toMenuTreeData(childrenMenus));
            }
            resultTrees.add(tree);
        }
        return resultTrees;
    }

    /**
     * 吧list中的 2个字段转为 下拉框选择的 value和name
     *
     * @param list
     * @param idKey
     * @param nameKey
     * @return
     */
    protected List<Map<String, Object>> toComboboxValue(List<Map<String, Object>> list, String idKey, String nameKey) {
        if (!isEmpty(list)) {
            list.forEach(map -> {
                if (!isEmpty(map.get(idKey))) {
                    map.put("ID", map.get(idKey));
                }
                if (!isEmpty(map.get(nameKey))) {
                    map.put("NAME", map.get(nameKey));
                }
            });
        }
        return list;
    }

    /**
     * 跳转到404
     */
    public void isNotFound() {
        throw new NotFoundException("没有找到页面!");
    }

    /**
     * 参数为空的情况就跳转到404
     *
     * @param val
     */
    public void isNotFound(Object val) {
        if (isEmpty(val) || val.equals(Tips.LOG_ERROR)) {
            isNotFound();
        }
    }

    /**
     * 返回404地址
     *
     * @param model
     * @return
     */
    public static String return404(Model model) {
        //404跳转
        HttpServletRequest request = HttpUtil.getRequest();
        if (!ValidateUtil.isEmpty(request.getHeader(PjaxInterceptor.PJAX))) {
            model.addAttribute("PJAX", true);
        }
        if (request.getRequestURI().contains(ManagerController.MANAGER_URL)) {
            return Attribute.MANAGER_404;
        } else {
            return Attribute.RECEPTION_404;
        }
    }

    /**
     * 浏览器下载获取文件流
     *
     * @param response
     * @param fileName
     * @return
     * @throws IOException
     */
    protected static OutputStream getResponseOutputStream(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "attachment;filename=" + new String(fileName.getBytes("UTF-8"), "ISO8859-1"));
        return response.getOutputStream();
    }

    /**
     * 设置标题
     *
     * @param model
     * @param title
     */
    protected void setHeaderTitle(Model model, String title) {
        model.addAttribute("headerTitle", title);
    }

}
