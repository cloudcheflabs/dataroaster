package com.cloudcheflabs.dataroaster.trino.gateway.redis;

import com.cloudcheflabs.dataroaster.trino.gateway.proxy.TrinoProxyServlet;
import com.esotericsoftware.kryo.Kryo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.*;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RedisTestRunner {

    private static Kryo kryo;

    @Before
    public void setup() {
        kryo = new Kryo();
        kryo.register(TrinoProxyServlet.TrinoResponse.class);
    }

    @Test
    public void addMap() throws Exception {

        String password = System.getProperty("password");

        HostAndPort hostAndPort = new HostAndPort("localhost", 6379);
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder().password(password).build();
        List<HostAndPort> shards = new ArrayList<>();
        shards.add(hostAndPort);
        JedisSharding jedis = new JedisSharding(shards, clientConfig);


        TrinoProxyServlet.TrinoResponse trinoResponse = new TrinoProxyServlet.TrinoResponse();
        trinoResponse.setId("any-id");
        trinoResponse.setNextUri("any-next-uri");
        trinoResponse.setInfoUri("any-info-uri");

        // set trino response object.
        jedis.set(trinoResponse.getId().getBytes(), serialize(trinoResponse));

        TrinoProxyServlet.TrinoResponse ret =
                deserialize(jedis.get(trinoResponse.getId().getBytes()), TrinoProxyServlet.TrinoResponse.class);
        Assert.assertEquals(trinoResponse.getNextUri(), ret.getNextUri());
        Assert.assertEquals(trinoResponse.getInfoUri(), ret.getInfoUri());
    }

    private static <T> byte[]  serialize(T t) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Output output = new Output(os);
        kryo.writeObject(output, t);
        output.close();

        return os.toByteArray();
    }

    private static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Input input = new Input(is);
        T t = kryo.readObject(input, clazz);
        input.close();

        return t;
    }
}
