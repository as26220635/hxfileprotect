package cn.kim.common.mybatis;

import cn.kim.util.CacheUtil;
import org.apache.ibatis.cache.Cache;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Created by 余庚鑫 on 2019/12/26
 * mybatis redis缓存
 */
public final class RedisCache implements Cache {

    private final ReadWriteLock readWriteLock = new DummyReadWriteLock();

    private String id;

    public RedisCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void putObject(Object o, Object o1) {
        CacheUtil.put(id, o, o1);
    }

    @Override
    public Object getObject(Object o) {
        Object val = CacheUtil.get(id, o);
        return val instanceof org.redisson.spring.cache.NullValue ? null : val;
    }

    @Override
    public Object removeObject(Object o) {
        return CacheUtil.remove(id, o);
    }

    @Override
    public void clear() {
        CacheUtil.clear(id);
    }

    @Override
    public int getSize() {
        return CacheUtil.getCache(id).size();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public String toString() {
        return "Redis {" + id + "}";
    }
}
