package com.cloudcheflabs.dataroaster.trino.controller.controller;

import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class ScaleController {

    private static Logger LOG = LoggerFactory.getLogger(ScaleController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;


    @Autowired
    private KubernetesClient client;

    @GetMapping("/v1/scale/list_worker_count")
    public String listWorkerCount(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);

            // TODO
            String json = "";

            return json;
        });
    }

    @PutMapping("/v1/scale/scale_workers")
    public String scaleWorkers(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);
            String clusterName = params.get("cluster_name");
            LOG.info("clusterName: {}", clusterName);

            String replicas = params.get("replicas");
            LOG.info("replicas: {}", replicas);



            return ControllerUtils.successMessage();
        });
    }


    @GetMapping("/v1/scale/list_hpa")
    public String listHpa(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);

            // TODO
            String json = "";

            return json;
        });
    }


    @PutMapping("/v1/scale/scale_hpa")
    public String scaleHpa(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);
            String clusterName = params.get("cluster_name");
            LOG.info("clusterName: {}", clusterName);

            String minReplicas = params.get("min_replicas");
            String maxReplicas = params.get("max_replicas");

            return ControllerUtils.successMessage();
        });
    }
}
