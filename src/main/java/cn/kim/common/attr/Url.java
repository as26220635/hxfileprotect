package cn.kim.common.attr;

/**
 * Created by 余庚鑫 on 2019/3/27
 * 全局URL
 */
public class Url {
    /**
     * URL地址
     */
    public static final String MANAGER_URL = "admin/";
    public static final String EDIT_USER = MANAGER_URL + "editUser";
    public static final String EDIT_PASSWORD = MANAGER_URL + "editPwd";
    public static final String EDIT_HEADPORTRAIT = MANAGER_URL + "headPortrait";
    /**
     * 获取ID
     */
    public static final String SEQUENCE_ID_URL = "getSequenceId";
    /**
     * 图片地址url
     */
    public static final String IMG_URL = AttributePath.FILE_PREVIEW_URL;
    public static final String DOWN_URL = AttributePath.FILE_DOWNLOAD_URL;
    public static final String DOWN_CACHE_URL = AttributePath.FILE_DOWNLOAD_URL + AttributePath.FILE_SERVICE_CACHE_PATH;
    /**
     * 图片服务器预览地址
     */
    public static final String FILE_SERVER_PREVIEW_URL = "preview/";
    public static final String FILE_SERVER_PLAYER_URL = "player/";
    public static final String FILE_SERVER_DOWNLOAD_URL = "download/";
    public static final String FILE_SERVER_UPLOAD_URL = "upload/";
    public static final String FILE_SERVER_UPDATE_URL = "file/update";
    /**
     * excel导出地址
     */
    public static final String EXPORT_URL = "file/export/";
    /**
     * 列表地址
     */
    public static final String DATA_GRID_URL = MANAGER_URL + "dataGrid/data/";
    public static final String TREE_GRID_URL = MANAGER_URL + "treeGrid/data/";
    public static final String DIFF_URL = MANAGER_URL + "diff";
    /**
     * 系统配置管理
     */
    public static final String ALLOCATION_BASE_URL = MANAGER_URL + "allocation/";
    /**
     * 邮箱配置管理
     */
    public static final String EMAIL_BASE_URL = ALLOCATION_BASE_URL + "email";
    public static final String EMAIL_CACHE_URL = EMAIL_BASE_URL + "/cache";
    /**
     * 网站配置地址
     */
    public static final String WEBCONFIG_BASE_URL = ALLOCATION_BASE_URL + "webConfig";
    /**
     * 前端管理管理
     */
    public static final String MOBILE_BOTTOM_MENU_BASE_URL = ALLOCATION_BASE_URL + "mobileBottomMenu";
    /**
     * 授权
     */
    public static final String AUTHORIZATION_BASE_URL = MANAGER_URL + "authorization/";
    public static final String AUTHORIZATION_TREE_URL = AUTHORIZATION_BASE_URL + "tree";
    public static final String AUTHORIZATION_UPDATE_URL = AUTHORIZATION_BASE_URL + "update";
    /**
     * 日志
     */
    public static final String LOG_BASE_URL = MANAGER_URL + "log/";
    /**
     * 变动记录
     */
    public static final String LOG_VALUE_RECORD_BASE_URL = LOG_BASE_URL + "valueRecord/";
    public static final String LOG_VALUE_RECORD_DETAIL_URL = LOG_VALUE_RECORD_BASE_URL + "detail/column";
    /**
     * 菜单管理
     */
    public static final String MENU_BASE_URL = MANAGER_URL + "menu/";
    public static final String MENU_LIST_URL = MENU_BASE_URL + "list";
    public static final String MENU_TREE_DATA_URL = MENU_BASE_URL + "getMenuTreeData";
    public static final String MENU_TREE_BUTTON_DATA_URL = MENU_BASE_URL + "getMenuButtonTreeData";
    public static final String MENU_TREE_BUTTON_DATA_UPDATE_URL = MENU_BASE_URL + "updateMenuButton";
    public static final String MENU_ADD_URL = MENU_BASE_URL + "add";
    public static final String MENU_UPDATE_URL = MENU_BASE_URL + "update";
    public static final String MENU_COPY_URL = MENU_BASE_URL + "copy";
    public static final String MENU_SWITCH_STATUS_URL = MENU_BASE_URL + "switchStatus";
    public static final String MENU_DELETE_URL = MENU_BASE_URL + "delete";
    /**
     * 配置列表
     */
    public static final String CONFIGURE_BASE_URL = MANAGER_URL + "configure/";
    public static final String CONFIGURE_TREE_DATA_URL = CONFIGURE_BASE_URL + "getConfigureTreeData";
    public static final String CONFIGURE_ADD_URL = CONFIGURE_BASE_URL + "add";
    public static final String CONFIGURE_UPDATE_URL = CONFIGURE_BASE_URL + "update";
    public static final String CONFIGURE_COPY_URL = CONFIGURE_BASE_URL + "copy";
    public static final String CONFIGURE_DELETE_URL = CONFIGURE_BASE_URL + "delete";
    /**
     * 配置列表列
     */
    public static final String CONFIGURE_COLUMN_BASE_URL = CONFIGURE_BASE_URL + "column/";
    public static final String CONFIGURE_COLUMN_ADD_URL = CONFIGURE_COLUMN_BASE_URL + "add";
    public static final String CONFIGURE_COLUMN_UPDATE_URL = CONFIGURE_COLUMN_BASE_URL + "update";
    public static final String CONFIGURE_COLUMN_DELETE_URL = CONFIGURE_COLUMN_BASE_URL + "delete";
    /**
     * 配置列表搜索
     */
    public static final String CONFIGURE_SEARCH_BASE_URL = CONFIGURE_BASE_URL + "search/";
    public static final String CONFIGURE_SEARCH_ADD_URL = CONFIGURE_SEARCH_BASE_URL + "add";
    public static final String CONFIGURE_SEARCH_UPDATE_URL = CONFIGURE_SEARCH_BASE_URL + "update";
    public static final String CONFIGURE_SEARCH_DELETE_URL = CONFIGURE_SEARCH_BASE_URL + "delete";
    /***
     * 配置列表文件
     */
    public static final String CONFIGURE_FILE_BASE_URL = CONFIGURE_BASE_URL + "file/";
    public static final String CONFIGURE_FILE_ADD_URL = CONFIGURE_FILE_BASE_URL + "add";
    public static final String CONFIGURE_FILE_UPDATE_URL = CONFIGURE_FILE_BASE_URL + "update";
    public static final String CONFIGURE_FILE_SWITCH_STATUS_URL = CONFIGURE_FILE_BASE_URL + "switchStatus";
    public static final String CONFIGURE_FILE_DELETE_URL = CONFIGURE_FILE_BASE_URL + "delete";
    /**
     * 按钮列表
     */
    public static final String BUTTON_BASE_URL = MANAGER_URL + "button/";
    public static final String BUTTON_ADD_URL = BUTTON_BASE_URL + "add";
    public static final String BUTTON_UPDATE_URL = BUTTON_BASE_URL + "update";
    public static final String BUTTON_DELETE_URL = BUTTON_BASE_URL + "delete";
    /**
     * 角色
     */
    public static final String ROLE_BASE_URL = MANAGER_URL + "role/";
    public static final String ROLE_LIST_URL = ROLE_BASE_URL + "list";
    public static final String ROLE_TREE_DATA_URL = ROLE_BASE_URL + "tree";
    public static final String ROLE_ADD_URL = ROLE_BASE_URL + "add";
    public static final String ROLE_UPDATE_URL = ROLE_BASE_URL + "update";
    public static final String ROLE_PERMISSION_TREE_MENU = ROLE_BASE_URL + "menuTree/";
    public static final String ROLE_PERMISSION_TREE_MENU_DATA = ROLE_BASE_URL + "getMenuTreeData/";
    public static final String ROLE_PERMISSION_TREE_MENU_UPDATE = ROLE_BASE_URL + "updateRoleMenuPermission";
    public static final String ROLE_PERMISSION_TREE_BUTTON = ROLE_BASE_URL + "buttonTree/";
    public static final String ROLE_PERMISSION_TREE_BUTTON_DATA = ROLE_BASE_URL + "getButtonTreeData/";
    public static final String ROLE_PERMISSION_TREE_BUTTON_UPDATE = ROLE_BASE_URL + "updateRoleButtonPermission";
    public static final String ROLE_SWITCH_STATUS_URL = ROLE_BASE_URL + "switchStatus";
    public static final String ROLE_DELETE_URL = ROLE_BASE_URL + "delete";
    /**
     * 验证
     */
    public static final String VALIDATE_BASE_URL = MANAGER_URL + "validate/";
    public static final String VALIDATE_ADD_URL = VALIDATE_BASE_URL + "add";
    public static final String VALIDATE_UPDATE_URL = VALIDATE_BASE_URL + "update";
    public static final String VALIDATE_SWITCH_STATUS_URL = VALIDATE_BASE_URL + "switchStatus";
    public static final String VALIDATE_DELETE_URL = VALIDATE_BASE_URL + "delete";
    /**
     * 验证字段
     */
    public static final String VALIDATE_FIELD_BASE_URL = VALIDATE_BASE_URL + "field/";
    public static final String VALIDATE_FIELD_LIST_URL = VALIDATE_FIELD_BASE_URL + "list";
    public static final String VALIDATE_FIELD_ADD_URL = VALIDATE_FIELD_BASE_URL + "add";
    public static final String VALIDATE_FIELD_UPDATE_URL = VALIDATE_FIELD_BASE_URL + "update";
    public static final String VALIDATE_FIELD_SWITCH_STATUS_URL = VALIDATE_FIELD_BASE_URL + "switchStatus";
    public static final String VALIDATE_FIELD_DELETE_URL = VALIDATE_FIELD_BASE_URL + "delete";
    /**
     * 验证组
     */
    public static final String VALIDATE_GROUP_BASE_URL = VALIDATE_BASE_URL + "group/";
    public static final String VALIDATE_GROUP_ADD_URL = VALIDATE_GROUP_BASE_URL + "add";
    public static final String VALIDATE_GROUP_UPDATE_URL = VALIDATE_GROUP_BASE_URL + "update";
    public static final String VALIDATE_GROUP_DELETE_URL = VALIDATE_GROUP_BASE_URL + "delete";
    /**
     * 验证正则
     */
    public static final String VALIDATE_REGEX_BASE_URL = VALIDATE_BASE_URL + "regex/";
    public static final String VALIDATE_REGEX_TREE_DATA_URL = VALIDATE_REGEX_BASE_URL + "tree";
    public static final String VALIDATE_REGEX_ADD_URL = VALIDATE_REGEX_BASE_URL + "add";
    public static final String VALIDATE_REGEX_UPDATE_URL = VALIDATE_REGEX_BASE_URL + "update";
    public static final String VALIDATE_REGEX_SWITCH_STATUS_URL = VALIDATE_REGEX_BASE_URL + "switchStatus";
    public static final String VALIDATE_REGEX_DELETE_URL = VALIDATE_REGEX_BASE_URL + "delete";
    /**
     * 字典
     */
    public static final String DICT_BASE_URL = MANAGER_URL + "dict/";
    public static final String DICT_ADD_URL = DICT_BASE_URL + "add";
    public static final String DICT_UPDATE_URL = DICT_BASE_URL + "update";
    public static final String DICT_SWITCH_STATUS_URL = DICT_BASE_URL + "switchStatus";
    public static final String DICT_DELETE_URL = DICT_BASE_URL + "delete";
    public static final String DICT_CACHE_URL = DICT_BASE_URL + "cache";
    /**
     * 字典信息
     */
    public static final String DICT_INFO_BASE_URL = DICT_BASE_URL + "info/";
    public static final String DICT_INFO_TREE_URL = DICT_INFO_BASE_URL + "tree";
    public static final String DICT_INFO_ADD_URL = DICT_INFO_BASE_URL + "add";
    public static final String DICT_INFO_UPDATE_URL = DICT_INFO_BASE_URL + "update";
    public static final String DICT_INFO_SWITCH_STATUS_URL = DICT_INFO_BASE_URL + "switchStatus";
    public static final String DICT_INFO_DELETE_URL = DICT_INFO_BASE_URL + "delete";
    /**
     * 操作员列表
     */
    public static final String OPERATOR_BASE_URL = MANAGER_URL + "operator/";
    public static final String OPERATOR_ADD_URL = OPERATOR_BASE_URL + "add";
    public static final String OPERATOR_UPDATE_URL = OPERATOR_BASE_URL + "update";
    public static final String OPERATOR_SWITCH_STATUS_URL = OPERATOR_BASE_URL + "switchStatus";
    public static final String OPERATOR_RESET_PWD_URL = OPERATOR_BASE_URL + "resetPwd";
    public static final String OPERATOR_DELETE_URL = OPERATOR_BASE_URL + "delete";
    public static final String OPERATOR_TREE_ROLE_DATA_URL = OPERATOR_BASE_URL + "roles";
    public static final String OPERATOR_TREE_ROLE_DATA_UPDATE_URL = OPERATOR_BASE_URL + "updateOperatorRoles";
    public static final String OPERATOR_LIST_URL = OPERATOR_BASE_URL + "list";
    /**
     * 操作员账号列表
     */
    public static final String OPERATOR_SUB_BASE_URL = OPERATOR_BASE_URL + "sub/";
    public static final String OPERATOR_SUB_ADD_URL = OPERATOR_SUB_BASE_URL + "add";
    public static final String OPERATOR_SUB_UPDATE_URL = OPERATOR_SUB_BASE_URL + "update";
    public static final String OPERATOR_SUB_SWITCH_STATUS_URL = OPERATOR_SUB_BASE_URL + "switchStatus";
    public static final String OPERATOR_SUB_DELETE_URL = OPERATOR_SUB_BASE_URL + "delete";
    /**
     * 格式管理
     */
    public static final String FORMAT_BASE_URL = MANAGER_URL + "format/";
    public static final String FORMAT_ADD_URL = FORMAT_BASE_URL + "add";
    public static final String FORMAT_UPDATE_URL = FORMAT_BASE_URL + "update";
    public static final String FORMAT_DELETE_URL = FORMAT_BASE_URL + "delete";
    /**
     * 格式详细管理
     */
    public static final String FORMAT_DETAIL_BASE_URL = FORMAT_BASE_URL + "detail/";
    public static final String FORMAT_DETAIL_TREE_URL = FORMAT_DETAIL_BASE_URL + "tree";
    public static final String FORMAT_DETAIL_ADD_URL = FORMAT_DETAIL_BASE_URL + "add";
    public static final String FORMAT_DETAIL_UPDATE_URL = FORMAT_DETAIL_BASE_URL + "update";
    public static final String FORMAT_DETAIL_SWITCH_STATUS_URL = FORMAT_DETAIL_BASE_URL + "switchStatus";
    public static final String FORMAT_DETAIL_DELETE_URL = FORMAT_DETAIL_BASE_URL + "delete";
    /**
     * 流程列表
     */
    public static final String PROCESS_BASE_URL = MANAGER_URL + "process/";
    public static final String PROCESS_DATAGRID_BTN = PROCESS_BASE_URL + "showDataGridBtn";
    public static final String PROCESS_DATAGRID_IS_EDIT = PROCESS_BASE_URL + "showDataGridIsEdit";
    public static final String PROCESS_PROCESS_STATUS = PROCESS_BASE_URL + "getProcessAuditStatus";
    public static final String PROCESS_SHOW_HOME = PROCESS_BASE_URL + "showDataGridProcess";
    public static final String PROCESS_SUBMIT = PROCESS_BASE_URL + "submit";
    public static final String PROCESS_WITHDRAW = PROCESS_BASE_URL + "withdraw";
    public static final String PROCESS_LOG = PROCESS_BASE_URL + "log";
    public static final String PROCESS_LOG_LIST = PROCESS_BASE_URL + "log/list";
    /**
     * 流程定义列表
     */
    public static final String PROCESS_DEFINITION_BASE_URL = PROCESS_BASE_URL + "definition/";
    public static final String PROCESS_DEFINITION_TREE_DATA = PROCESS_DEFINITION_BASE_URL + "tree";
    public static final String PROCESS_DEFINITION_ADD_URL = PROCESS_DEFINITION_BASE_URL + "add";
    public static final String PROCESS_DEFINITION_UPDATE_URL = PROCESS_DEFINITION_BASE_URL + "update";
    public static final String PROCESS_DEFINITION_SWITCH_STATUS_URL = PROCESS_DEFINITION_BASE_URL + "switchStatus";
    public static final String PROCESS_DEFINITION_COPY_URL = PROCESS_DEFINITION_BASE_URL + "copy";
    /**
     * 流程定义列表
     */
    public static final String PROCESS_STEP_BASE_URL = PROCESS_BASE_URL + "step/";
    public static final String PROCESS_STEP_ADD_URL = PROCESS_STEP_BASE_URL + "add";
    public static final String PROCESS_STEP_UPDATE_URL = PROCESS_STEP_BASE_URL + "update";
    public static final String PROCESS_STEP_DELETE_URL = PROCESS_STEP_BASE_URL + "delete";
    /**
     * 流程启动角色列表
     */
    public static final String PROCESS_START_BASE_URL = PROCESS_BASE_URL + "start/";
    public static final String PROCESS_START_ADD_URL = PROCESS_START_BASE_URL + "add";
    public static final String PROCESS_START_UPDATE_URL = PROCESS_START_BASE_URL + "update";
    public static final String PROCESS_START_DELETE_URL = PROCESS_START_BASE_URL + "delete";
    /**
     * 流程时间控制
     */
    public static final String PROCESS_TIME_CONTROL_BASE_URL = PROCESS_BASE_URL + "timeControl/";
    public static final String PROCESS_TIME_CONTROL_ADD_URL = PROCESS_TIME_CONTROL_BASE_URL + "add";
    public static final String PROCESS_TIME_CONTROL_UPDATE_URL = PROCESS_TIME_CONTROL_BASE_URL + "update";
    public static final String PROCESS_TIME_CONTROL_SWITCH_STATUS_URL = PROCESS_TIME_CONTROL_BASE_URL + "switchStatus";
    public static final String PROCESS_TIME_CONTROL_DELETE_URL = PROCESS_TIME_CONTROL_BASE_URL + "delete";
    /**
     * 流程进度列表
     */
    public static final String PROCESS_SCHEDULE_BASE_URL = PROCESS_BASE_URL + "schedule/";
    public static final String PROCESS_SCHEDULE_CANCEL_URL = PROCESS_SCHEDULE_BASE_URL + "cancel";

    /**
     * 主页图片
     */
    public static final String MAINIMAGE_BASE_URL = MANAGER_URL + "mainImage/";
    public static final String MAINIMAGE_ADD_URL = MAINIMAGE_BASE_URL + "add";
    public static final String MAINIMAGE_UPDATE_URL = MAINIMAGE_BASE_URL + "update";
    public static final String MAINIMAGE_SWITCHSTATUS_URL = MAINIMAGE_BASE_URL + "switchStatus";
    public static final String MAINIMAGE_DELETE_URL = MAINIMAGE_BASE_URL + "delete";
    /**
     * 主页图片关联成就墙
     */
    public static final String MAINIMAGE_TREE_DATA_URL = MAINIMAGE_BASE_URL + "achievementTreeData";
    public static final String MAINIMAGE_TREE_UPDATE_URL = MAINIMAGE_BASE_URL + "updateAchievementMainImage";
    /**
     * 主页图片区域管理
     */
    public static final String MAINIMAGE_AREA_UPDATE_URL = MAINIMAGE_BASE_URL + "area/update";
    /**
     * 成就墙
     */
    public static final String ACHIEVEMENT_BASE_URL = MANAGER_URL + "achievement/";
    public static final String ACHIEVEMENT_ADD_URL = ACHIEVEMENT_BASE_URL + "add";
    public static final String ACHIEVEMENT_UPDATE_URL = ACHIEVEMENT_BASE_URL + "update";
    public static final String ACHIEVEMENT_SWITCHSTATUS_URL = ACHIEVEMENT_BASE_URL + "switchStatus";
    public static final String ACHIEVEMENT_DELETE_URL = ACHIEVEMENT_BASE_URL + "delete";
    /**
     * 成就墙分享管理
     */
    public static final String ACHIEVEMENT_SHARE_HTML_UPDATE_URL = ACHIEVEMENT_BASE_URL + "share/updateHtml";
    public static final String ACHIEVEMENT_SHARE_UPDATE_URL = ACHIEVEMENT_BASE_URL + "share/update";
    /**
     * 打卡记录
     */
    public static final String CLOCKIN_BASE_URL = MANAGER_URL + "clockin/";
    public static final String CLOCKIN_ADD_URL = CLOCKIN_BASE_URL + "add";
    public static final String CLOCKIN_UPDATE_URL = CLOCKIN_BASE_URL + "update";
    public static final String CLOCKIN_DELETE_URL = CLOCKIN_BASE_URL + "delete";
    /**
     * 微信用户管理
     */
    public static final String WECHAT_BASE_URL = MANAGER_URL + "wechat/";
    public static final String WECHAT_SWITCHSTATUS_URL = WECHAT_BASE_URL + "switchStatus";

    /**
     * 活动
     */
    public static final String ACTIVITY_BASE_URL = MANAGER_URL + "activity/";
    public static final String ACTIVITY_ADD_URL = ACTIVITY_BASE_URL + "add";
    public static final String ACTIVITY_UPDATE_URL = ACTIVITY_BASE_URL + "update";
    public static final String ACTIVITY_SWITCHSTATUS_URL = ACTIVITY_BASE_URL + "switchStatus";
    public static final String ACTIVITY_DELETE_URL = ACTIVITY_BASE_URL + "delete";
}
