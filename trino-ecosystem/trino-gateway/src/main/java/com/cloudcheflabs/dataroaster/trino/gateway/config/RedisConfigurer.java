package com.cloudcheflabs.dataroaster.trino.gateway.config;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.util.Base64Utils;
import com.cloudcheflabs.dataroaster.trino.gateway.util.ContainerStatusChecker;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisSharding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class RedisConfigurer {

    public static final String DEFAULT_NAMESPACE = "trino-gateway";

    @Autowired
    private Environment env;

    @Autowired
    private KubernetesClient kubernetesClient;

    @Bean
    public  JedisSharding jedis() {
        String host = env.getProperty("redis.host");
        String port = env.getProperty("redis.port");

        // get redis password from secret.
        String namespace = getNamespace();

        // check if redis is running now.
        String componentName = (namespace.equals("redis")) ? "redis-master-0" : namespace + "-" + "redis-master-0";
        ContainerStatusChecker.checkContainerStatus(kubernetesClient,
                componentName,
                namespace,
                "app.kubernetes.io/component",
               "master",
                20);

        String secretName = (namespace.equals("redis")) ? "redis" : namespace + "-" + "redis";
        Resource<Secret> secret = kubernetesClient.secrets().inNamespace(namespace).withName(secretName);
        Map<String, String> data = secret.get().getData();
        String base64EncodedPassword = data.get("redis-password");
        String password = Base64Utils.decodeBase64(base64EncodedPassword);

        HostAndPort hostAndPort = new HostAndPort(host, Integer.valueOf(port));
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder().password(password).build();
        List<HostAndPort> shards = new ArrayList<>();
        shards.add(hostAndPort);
        JedisSharding jedis = new JedisSharding(shards, clientConfig);

        return jedis;
    }

    public static String getNamespace() {
        try {
            String namespaceFile = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";
            return FileUtils.fileToString(namespaceFile, false);
        } catch (Exception e) {
            System.out.printf("instead return default namespace [%s]\n", DEFAULT_NAMESPACE);
            return DEFAULT_NAMESPACE;
        }
    }
}
