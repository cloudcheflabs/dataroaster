package com.cloudcheflabs.dataroaster.trino.controller.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import com.cloudcheflabs.dataroaster.trino.controller.api.service.K8sResourceService;
import com.cloudcheflabs.dataroaster.trino.controller.domain.CustomResource;
import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import com.cloudcheflabs.dataroaster.trino.controller.util.ContainerStatusChecker;
import com.cloudcheflabs.dataroaster.trino.controller.util.CustomResourceUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TrinoController {

    private static Logger LOG = LoggerFactory.getLogger(TrinoController.class);

    private ObjectMapper mapper = new ObjectMapper();

    public static final String DEFAULT_TRINO_OPERATOR_NAMESPACE = "trino-operator";

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("kubernetesClient")
    private KubernetesClient kubernetesClient;

    @Autowired
    @Qualifier("k8sResourceServiceImpl")
    private K8sResourceService k8sResourceService;



    @PostMapping("/v1/trino/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");
            String namespace = params.get("namespace");
            String maxHeapSize = params.get("max_heap_size");
            String replicas = params.get("replicas");
            String maxReplicas = params.get("max_replicas");
            String minReplicas = params.get("min_replicas");


            Map<String, Object> kv = new HashMap<>();
            kv.put("name", name);
            kv.put("namespace", namespace);
            kv.put("maxHeapSize", maxHeapSize);
            kv.put("replicas", replicas);
            kv.put("maxReplicas", maxReplicas);
            kv.put("minReplicas", minReplicas);
            String trinoClusterString =
                    TemplateUtils.replace("/templates/cr/trino-cluster-jmx.yaml", true, kv);


            // trino cluster namespace.
            String trinoClusterNamespace = CustomResourceUtils.getTargetNamespace(trinoClusterString);

            // check if trino cluster is running.
            boolean trinoClusterRunning = ContainerStatusChecker.isRunning(
                    kubernetesClient,
                    "trino-coordinator",
                    trinoClusterNamespace,
                    "component",
                    "coordinator"
            );
            LOG.info("trino cluster in namespace [{}] is running: {}", trinoClusterNamespace, trinoClusterRunning);

            if(trinoClusterRunning) {
                throw new IllegalStateException("trino cluster in namespace [" + trinoClusterNamespace + "] already running.");
            }

            // build custom resource of trino cluster.
            CustomResource trinoClusterCr = CustomResourceUtils.fromYaml(trinoClusterString);

            // create cr of trino cluster.
            k8sResourceService.createCustomResource(trinoClusterCr);
            LOG.info("trino cluster custom resource created...");

            // wait for that trino cluster will be run.
            ContainerStatusChecker.checkContainerStatus(
                    kubernetesClient,
                    "trino-coordinator",
                    trinoClusterNamespace,
                    "component",
                    "coordinator",
                    50
            );


            return ControllerUtils.successMessage();
        });
    }

    /**
     * TODO:
     * - add catalog.
     * - update catalog configuration.
     * - update config.properties
     * - update jvm.config.
     *
     *
     *
     *
     */




    @DeleteMapping("/v1/trino/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");

            k8sResourceService.deleteCustomResource(name, DEFAULT_TRINO_OPERATOR_NAMESPACE, "TrinoCluster");

            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/trino/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {

            List<GenericKubernetesResource> trinoClusters =
                    k8sResourceService.listCustomResources(DEFAULT_TRINO_OPERATOR_NAMESPACE, "TrinoCluster");

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(GenericKubernetesResource genericKubernetesResource : trinoClusters) {
                Map<String, Object> additionalMap = genericKubernetesResource.getAdditionalProperties();
                Map<String, Object> specMap = (Map<String, Object>) additionalMap.get("spec");
                String namespace = (String) specMap.get("namespace");

                Map<String, Object> map = new HashMap<>();
                map.put("name", genericKubernetesResource.getMetadata().getName());
                map.put("namespace", namespace);
                mapList.add(map);
            }

            return JsonUtils.toJson(mapList);
        });
    }
}
