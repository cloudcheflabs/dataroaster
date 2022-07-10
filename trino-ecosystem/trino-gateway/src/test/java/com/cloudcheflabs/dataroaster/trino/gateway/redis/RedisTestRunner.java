package com.cloudcheflabs.dataroaster.trino.gateway.redis;

import org.junit.Test;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.*;

public class RedisTestRunner {

    @Test
    public void addMap() throws Exception {
        JedisPooled jedis = new JedisPooled("localhost", 6379);
    }
}
