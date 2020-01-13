package cn.kim.common;

import cn.kim.common.attr.CacheName;
import cn.kim.common.eu.NameSpace;
import cn.kim.remote.LogRemoteInterface;
import cn.kim.remote.LogRemoteInterfaceAsync;
import cn.kim.remote.impl.LogRemoteServiceImpl;
import com.google.common.collect.Maps;
import org.redisson.Redisson;
import org.redisson.api.RRemoteService;
import org.redisson.api.RedissonClient;
import org.redisson.api.RemoteInvocationOptions;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.IOException;
import java.util.Map;

/**
 * Created by 余庚鑫 on 2018/3/22.
 * redisson配置
 */
@Configuration
@ComponentScan
@EnableCaching
public class WebAppConfig {

    @Bean
    public TaskScheduler scheduledExecutorService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(8);
        scheduler.setThreadNamePrefix("scheduled-thread-");
        return scheduler;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(@Value("classpath:redis/spring-redisson.yaml") Resource configFile) throws IOException {
        Config config = Config.fromYAML(configFile.getInputStream());
        return Redisson.create(config);
    }

    @Bean("cacheManager")
    public CacheManager cacheManager(RedissonClient redissonClient) throws IOException {
        Map<String, CacheConfig> config = Maps.newHashMapWithExpectedSize(16);
        //shiro
        config.put(CacheName.SHIRO_AUTHORIZATIONCACHE, new CacheConfig(7200000, 3600000));
        config.put(CacheName.SHIRO_AUTHENTICATIONCACHE, new CacheConfig(7200000, 3600000));
        config.put(CacheName.SHIRO_ACTIVESESSIONCACHE, new CacheConfig(7200000, 3600000));
        //密码锁定
        config.put(CacheName.PASSWORD_RETRY_CACHE, new CacheConfig(600000, 600000));
        //前端搜索
        config.put(CacheName.ACHIEVEMENT_CACHE, new CacheConfig(0, 0));
        //字典
        config.put(CacheName.DICT_CACHE, new CacheConfig(0, 0));
        //数据缓存
        config.put(CacheName.VALUE_COLLECTION, new CacheConfig(0, 0));
        //微信点赞
        config.put(CacheName.WECHAT_PRAISE, new CacheConfig(0, 0));
        config.put(CacheName.WECHAT_PRAISE_POINTS, new CacheConfig(0, 0));
        //数据库缓存
        for (NameSpace e : NameSpace.values()) {
            config.put(e.getValue(), new CacheConfig(0, 0));
        }

        return new RedissonSpringCacheManager(redissonClient, config);
    }

    @Bean("remoteService")
    public RRemoteService rRemoteService(RedissonClient redissonClient) {
        RRemoteService remoteService = redissonClient.getRemoteService();
        //日志
        LogRemoteInterface serviceImpl = new LogRemoteServiceImpl();
        remoteService.register(LogRemoteInterface.class, serviceImpl);

        return remoteService;
    }

    /**
     * 异步
     *
     * @param remoteService
     * @return
     */
    @Bean("LogRemoteInterfaceAsync")
    public LogRemoteInterfaceAsync logRemoteInterfaceAsync(RRemoteService remoteService) {
        // 发送即不管（Fire-and-Forget）模式，无需应答回执，不等待结果
        RemoteInvocationOptions options = RemoteInvocationOptions.defaults().noAck().noResult();
        return remoteService.get(LogRemoteInterfaceAsync.class, options);
    }

}