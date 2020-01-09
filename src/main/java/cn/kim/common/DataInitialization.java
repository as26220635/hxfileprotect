package cn.kim.common;

import cn.kim.common.attr.WebConfig;
import cn.kim.dao.BaseDao;
import cn.kim.util.DictUtil;
import cn.kim.util.EmailUtil;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.security.Security;

/**
 * Created by 余庚鑫 on 2017/7/3.
 * spring/mvc 启动完成后初始化数据
 */
@Component("DataInitialization")
public class DataInitialization extends BaseData implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    public BaseDao baseDao;

    @Autowired
    private RedissonClient redisson;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
            //设置AES加密包
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            //加载字典
            DictUtil.initDictToCache();

            //加载邮箱配置
            EmailUtil.init();

            //加载网站参数
            WebConfig.init();
        }
    }
}
