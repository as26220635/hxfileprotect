package cn.kim.common.eu;

/**
 * Created by 余庚鑫 on 2018/3/21.
 * 系统变量
 */
public enum SystemEnum {
    //系统
    SYSTEM(0),
    //管理员
    MANAGER(1),
    //微信用户
    WECHAT(2),
    ;

    private int type;

    private SystemEnum(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.valueOf(this.type);
    }

    public int getType() {
        return this.type;
    }
}
