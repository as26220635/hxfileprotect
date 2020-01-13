package cn.kim.common.eu;

import cn.kim.util.TextUtil;

/**
 * Created by 余庚鑫 on 2020/1/9
 * 流程分支
 */
public enum ProcessBranch {
    //固定
    FIXED(1),
    //分支
    BRANCH(2),
    //并发
    CONCURRENT(3);

    private int type;

    private ProcessBranch(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return TextUtil.toString(this.type);
    }

    public int getType() {
        return this.type;
    }

}
