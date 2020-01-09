package cn.kim.exception;

import org.apache.shiro.authc.AuthenticationException;

/**
 * Created by 余庚鑫 on 2019/12/1.
 */
public class WechatNoLoginException extends AuthenticationException {

    public WechatNoLoginException() {
        super();
    }

    public WechatNoLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public WechatNoLoginException(String message) {
        super(message);
    }

    public WechatNoLoginException(Throwable cause) {
        super(cause);
    }
}