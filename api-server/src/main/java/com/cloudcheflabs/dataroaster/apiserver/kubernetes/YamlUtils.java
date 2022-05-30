package com.cloudcheflabs.dataroaster.apiserver.kubernetes;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class YamlUtils {

    private static Logger LOG = LoggerFactory.getLogger(YamlUtils.class);

    public static Kubeconfig readKubeconfigYaml(String rawKubeconfig) {
        InputStream inputStream = new ByteArrayInputStream(rawKubeconfig.getBytes());
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(inputStream);
        LOG.debug(JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), map)));

        List<Map<String, Object>> clustersList = (List<Map<String, Object>>) map.get("clusters");
        Map<String, Object> firstClustersMap = clustersList.get(0);
        String clusterName = (String) firstClustersMap.get("name");

        Map<String, Object> clusterMap = (Map<String, Object>) firstClustersMap.get("cluster");
        String masterUrl = (String) clusterMap.get("server");
        String clusterCertData = (String) clusterMap.get("certificate-authority-data");
        String namespace = "default";

        List<Map<String, Object>> usersMap = (List<Map<String, Object>>) map.get("users");
        Map<String, Object> firstUsersMap = usersMap.get(0);
        String user = (String) firstUsersMap.get("name");

        Map<String, Object> userMap = (Map<String, Object>) firstUsersMap.get("user");
        String clientCertData = (String) userMap.get("client-certificate-data");
        String clientKeyData = (String) userMap.get("client-key-data");

        return new Kubeconfig(
                masterUrl,
                clusterName,
                clusterCertData,
                namespace,
                user,
                clientCertData,
                clientKeyData,
                rawKubeconfig);
    }
}
