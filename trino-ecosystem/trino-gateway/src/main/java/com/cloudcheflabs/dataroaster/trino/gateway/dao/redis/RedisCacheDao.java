package com.cloudcheflabs.dataroaster.trino.gateway.dao.redis;

import com.cloudcheflabs.dataroaster.trino.gateway.dao.common.AbstractCacheDao;
import redis.clients.jedis.JedisSharding;


public class RedisCacheDao<T> extends AbstractCacheDao<T> {

    public RedisCacheDao(JedisSharding jedis, Class<T> clazz) {
        super(jedis, clazz);
    }
}
