package com.cloudcheflabs.dataroaster.operators.dataroaster.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.ComponentsService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.CustomResourceService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.K8sResourceService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.Roles;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.Components;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import com.cloudcheflabs.dataroaster.operators.dataroaster.util.Base64Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.*;
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
public class HiveMetastoreController {

    private static Logger LOG = LoggerFactory.getLogger(HiveMetastoreController.class);

    public static final String COMPONENT_HIVE_METASTORE = "hive-metastore";

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("k8sResourceServiceImpl")
    private K8sResourceService k8sResourceService;


    @Autowired
    @Qualifier("customResourceServiceImpl")
    private CustomResourceService customResourceService;


    @Autowired
    @Qualifier("componentsServiceImpl")
    private ComponentsService componentsService;
    
    @Autowired
    KubernetesClient kubernetesClient;


    @PostMapping("/v1/hive_metastore/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String mysqlStorageClass = params.get("mysql_storage_class");
            String mysqlStorageSize = params.get("mysql_storage_size");
            String yaml = params.get("yaml");

            String decodedYaml = Base64Utils.decodeBase64(yaml);
            LOG.info("decodedYaml: {}", decodedYaml);

            Components components = componentsService.findOne(COMPONENT_HIVE_METASTORE);
            if(components == null) {
                components = new Components();
                components.setCompName(COMPONENT_HIVE_METASTORE);
                componentsService.create(components);
            }

            // create hive metastore custom resource.
            CustomResource hiveMetastoreCustomResource = CustomResourceUtils.fromYaml(decodedYaml);
            hiveMetastoreCustomResource.setComponents(components);

            // create mysql custom resource.
            Map<String, Object> kv = new HashMap<>();
            kv.put("namespace", hiveMetastoreCustomResource.getNamespace());
            kv.put("storageClass", mysqlStorageClass);
            kv.put("size", mysqlStorageSize);
            
            String mysqlCustomResourceString = 
                    TemplateUtils.replace("/templates/hive-metastore/hive-metastore-mysql.yaml", true, kv);
            CustomResource mysqlCustomResource = CustomResourceUtils.fromYaml(mysqlCustomResourceString);
            mysqlCustomResource.setComponents(components);
        
            // create mysql on kubernetes.
            k8sResourceService.createCustomResource(mysqlCustomResource);
            
            // check if mysql pod is running status.
            checkMySQLStatus(hiveMetastoreCustomResource.getNamespace());

            // create hive metastore on kubernetes.
            k8sResourceService.createCustomResource(hiveMetastoreCustomResource);

            return ControllerUtils.successMessage();
        });
    }

    private void checkMySQLStatus(String namespace) {
        int MAX_COUNT = 20;
        int count = 0;
        boolean running = true;
        // watch mysql pod if it has the status of RUNNING.
        while (running) {
            PodList podList = kubernetesClient.pods().inNamespace(namespace).list();
            for(Pod pod : podList.getItems()) {
                ObjectMeta metadata = pod.getMetadata();
                LOG.info("metadata: [{}]", JsonUtils.toJson(metadata));
                Map<String, String> labels = metadata.getLabels();
                LOG.info("labels: [{}]", JsonUtils.toJson(labels));
                for(String key : labels.keySet()) {
                    LOG.info("key: [{}]", key);
                    if(key.equals("app")) {
                        String value = labels.get(key);
                        LOG.info("key: [{}], value: [{}]", key, value);
                        if(value.equals("mysql")) {
                            PodStatus status = pod.getStatus();
                            List<ContainerStatus> containerStatuses = status.getContainerStatuses();
                            LOG.info("containerStatuses: [{}]", containerStatuses.size());
                            if (!containerStatuses.isEmpty()) {
                                ContainerStatus containerStatus = containerStatuses.get(0);
                                ContainerState state = containerStatus.getState();
                                LOG.info("state: [{}]", state.toString());
                                ContainerStateRunning containerStateRunning = state.getRunning();
                                if(containerStateRunning != null) {
                                    LOG.info("mysql has running status now.");
                                    running = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if(count < MAX_COUNT) {
                count++;
                try {
                    Thread.sleep(5000);
                    continue;
                } catch (Exception e) {
                    System.err.println(e);
                }
            } else {
                throw new IllegalStateException("mysql has no running status!");
            }
        }
    }


    @DeleteMapping("/v1/hive_metastore/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            Components components = componentsService.findOne(COMPONENT_HIVE_METASTORE);
            if(components != null) {
                for(CustomResource customResource : components.getCustomResourceSet()) {
                    k8sResourceService.deleteCustomResource(customResource.getName(), customResource.getNamespace(), customResource.getKind());
                }
            }
            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/hive_metastore/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            List<Map<String, Object>> mapList = new ArrayList<>();

            Components components = componentsService.findOne(COMPONENT_HIVE_METASTORE);
            if(components != null) {
                for(CustomResource customResource : components.getCustomResourceSet()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("kind", customResource.getKind());
                    map.put("name", customResource.getName());
                    map.put("namespace", customResource.getNamespace());
                    map.put("components", COMPONENT_HIVE_METASTORE);

                    mapList.add(map);
                }
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
