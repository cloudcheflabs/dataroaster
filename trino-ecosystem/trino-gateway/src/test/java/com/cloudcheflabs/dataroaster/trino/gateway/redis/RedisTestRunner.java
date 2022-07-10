package com.cloudcheflabs.dataroaster.trino.gateway.redis;

import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoResponse;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisSharding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RedisTestRunner {

    private static Kryo kryo;

    @Before
    public void setup() {
        kryo = new Kryo();
        kryo.register(TrinoResponse.class);
    }

    @Test
    public void addMap() throws Exception {

        String host = System.getProperty("host", "localhost");
        String port = System.getProperty("port", "6379");
        String password = System.getProperty("password");

        HostAndPort hostAndPort = new HostAndPort(host, Integer.valueOf(port));
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder().password(password).build();
        List<HostAndPort> shards = new ArrayList<>();
        shards.add(hostAndPort);
        JedisSharding jedis = new JedisSharding(shards, clientConfig);


        TrinoResponse trinoResponse = new TrinoResponse();
        trinoResponse.setId("any-id");
        trinoResponse.setNextUri("any-next-uri");
        trinoResponse.setInfoUri("any-info-uri");

        // set trino response object.
        jedis.set(trinoResponse.getId().getBytes(), serialize(trinoResponse));

        TrinoResponse ret =
                deserialize(jedis.get(trinoResponse.getId().getBytes()), TrinoResponse.class);
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
