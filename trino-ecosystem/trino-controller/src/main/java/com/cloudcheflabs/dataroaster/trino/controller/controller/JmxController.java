package com.cloudcheflabs.dataroaster.trino.controller.controller;

import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class JmxController {

    private static Logger LOG = LoggerFactory.getLogger(JmxController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;


    @Autowired
    private KubernetesClient client;



    @GetMapping("/v1/cluster/list_clusters")
    public String listClusters(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);

            // TODO
            String json = "";

            return json;
        });
    }

    @GetMapping("/v1/jmx/list_mbeans")
    public String listMBeans(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);
            String clusterName = params.get("cluster_name");
            LOG.info("clusterName: {}", clusterName);

            // TODO
            String json = "";

            return json;
        });
    }



    @GetMapping("/v1/jmx/get_value")
    public String getMBeanValue(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);
            String clusterName = params.get("cluster_name");
            LOG.info("clusterName: {}", clusterName);

            String objectName = params.get("object_name");
            LOG.info("objectName: {}", objectName);

            String attribute = params.get("attribute");
            LOG.info("attribute: {}", attribute);

            // optional.
            String compositeKey = params.get("composite_key");
            LOG.info("compositeKey: {}", compositeKey);

            // TODO
            String json = "";

            return json;

        });
    }
}
