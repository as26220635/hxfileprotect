package cn.kim.common.annotation;

import java.lang.annotation.*;

/**
 * Created by 余庚鑫 on 2019/12/1.
 * 验证微信是否登录
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WechaNotEmptyLogin {
}
