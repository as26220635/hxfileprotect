package cn.kim.common.eu;

/**
 * Created by 余庚鑫 on 2018/3/21
 * mapper 命名空间
 */
public enum NameSpace {
    //数据库查询
    DbMapper("cn.kim.mapper.DbMapper"),
    //配置
    AllocationMapper("cn.kim.mapper.AllocationMapper"),
    //管理
    ManagerMapper("cn.kim.mapper.ManagerMapper"),
    //数据列表
    DataGridMapper("cn.kim.mapper.DataGridMapper"),
    //操作员
    OperatorMapper("cn.kim.mapper.OperatorMapper"),
    //角色
    RoleMapper("cn.kim.mapper.RoleMapper"),
    //菜单
    MenuMapper("cn.kim.mapper.MenuMapper"),
    //字典
    DictMapper("cn.kim.mapper.DictMapper"),
    //按钮
    ButtonMapper("cn.kim.mapper.ButtonMapper"),
    //配置列表
    ConfigureMapper("cn.kim.mapper.ConfigureMapper"),
    //验证
    ValidateMapper("cn.kim.mapper.ValidateMapper"),
    //日志
    LogMapper("cn.kim.mapper.LogMapper"),
    //参数记录
    ValueRecordMapper("cn.kim.mapper.ValueRecordMapper"),
    //文件
    FileMapper("cn.kim.mapper.FileMapper"),
    //流程进度、日志
    ProcessMapper("cn.kim.mapper.ProcessMapper"),
    //流程定义、步骤、启动角色（缓存）
    ProcessFixedMapper("cn.kim.mapper.ProcessFixedMapper"),
    //格式管理
    FormatMapper("cn.kim.mapper.FormatMapper"),
    //授权
    AuthorizationMapper("cn.kim.mapper.AuthorizationMapper"),
    ;

    private final String value;

    private NameSpace(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
