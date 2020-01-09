package cn.kim.common.shiro.cache;

import org.apache.shiro.cache.CacheException;

import java.util.Collection;
import java.util.Set;

/**
 * Created by 余庚鑫 on 2019/12/3
 * 重写cache
 */
public interface Cache<K, V> extends org.apache.shiro.cache.Cache {

    /**
     * redission 模糊搜索
     *
     * @param keyPattern
     * @return
     */
    Collection<V> values(String keyPattern);

    Collection<V> values(String keyPattern, int count);
}
