package cn.kim.common.aspect;

import cn.kim.common.BaseData;
import cn.kim.common.attr.MagicValue;
import cn.kim.exception.WechatNoLoginException;
import cn.kim.service.ValidateService;
import cn.kim.util.AuthcUtil;
import cn.kim.util.SessionUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.web.util.WebUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by 余庚鑫 on 2019/12/1.
 * aop验证是否登录
 */
@Aspect
@Component
@Log4j2
public class WechaNotEmptyLoginAspect extends BaseData {

    @Autowired
    private ValidateService validateService;

    //Controller层切点
    @Pointcut("@annotation(cn.kim.common.annotation.WechaNotEmptyLogin)")
    public void wechaNotEmptyLoginAspect() {
    }

    @Around("wechaNotEmptyLoginAspect()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        if (isEmpty(SessionUtil.get(MagicValue.SESSION_WECHAT_USER))) {
            throw new WechatNoLoginException("没有登录!");
        }
        return pjp.proceed();
    }


}
