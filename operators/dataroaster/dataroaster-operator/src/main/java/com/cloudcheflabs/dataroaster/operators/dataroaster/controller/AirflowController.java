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
import io.fabric8.kubernetes.api.model.Namespace;
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

import static com.cloudcheflabs.dataroaster.operators.dataroaster.controller.SparkThriftServerController.COMPONENT_NFS;

@RestController
public class AirflowController {

    private static Logger LOG = LoggerFactory.getLogger(AirflowController.class);

    public static final String COMPONENT_AIRFLOW = "airflow";


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


    @PostMapping("/v1/airflow/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String nfsStorageClass = params.get("nfs_storage_class");
            String nfsStorageSize = params.get("nfs_storage_size");

            String yaml = params.get("yaml");

            String decodedYaml = Base64Utils.decodeBase64(yaml);
            LOG.info("decodedYaml: {}", decodedYaml);

            Components nfsComponent = componentsService.findOne(COMPONENT_NFS);
            if(nfsComponent == null) {
                nfsComponent = new Components();
                nfsComponent.setCompName(COMPONENT_NFS);
                componentsService.create(nfsComponent);
            }

            // check if nfs is installed and running.
            boolean nfsRunning = ContainerStatusChecker.isRunning(kubernetesClient,
                    "nfs",
                    "nfs",
                    "app",
                    "nfs-server-provisioner");
            LOG.info("nfsRunning: [{}]", nfsRunning);

            // if nfs is not installed.
            if(!nfsRunning) {
                // create nfs on kubernetes.
                Map<String, Object> kv = new HashMap<>();
                kv.put("storageClass", nfsStorageClass);
                kv.put("size", nfsStorageSize);

                String nfsCustomResourceString =
                        TemplateUtils.replace("/templates/nfs/nfs.yaml", true, kv);
                CustomResource nfsCustomResource = CustomResourceUtils.fromYaml(nfsCustomResourceString);
                nfsCustomResource.setComponents(nfsComponent);
                k8sResourceService.createCustomResource(nfsCustomResource);

                // check if nfs is running status.
                ContainerStatusChecker.checkContainerStatus(kubernetesClient,
                        "nfs",
                        "nfs",
                        "app",
                        "nfs-server-provisioner");
                LOG.info("nfs installed...");
            }

            Components components = componentsService.findOne(COMPONENT_AIRFLOW);
            if(components == null) {
                components = new Components();
                components.setCompName(COMPONENT_AIRFLOW);
                componentsService.create(components);
            }

            // create redash custom resource.
            CustomResource redashCustomResource = CustomResourceUtils.fromYaml(decodedYaml);
            redashCustomResource.setComponents(components);
            k8sResourceService.createCustomResource(redashCustomResource);

            return ControllerUtils.successMessage();
        });
    }




    @DeleteMapping("/v1/airflow/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            Components components = componentsService.findOne(COMPONENT_AIRFLOW);
            if(components != null) {
                String targetNamespace = null;
                for(CustomResource customResource : components.getCustomResourceSet()) {
                    k8sResourceService.deleteCustomResource(customResource.getName(), customResource.getNamespace(), customResource.getKind());
                    if(targetNamespace == null) {
                        targetNamespace = CustomResourceUtils.getTargetNamespace(customResource.getYaml());
                    }
                }
                if(targetNamespace != null) {
                    for(Namespace ns : kubernetesClient.namespaces().list().getItems()) {
                        if(ns.getMetadata().getName().equals(targetNamespace)) {
                            boolean deleted = kubernetesClient.namespaces().delete(ns);
                            LOG.info("ns [{}] deleted [{}]", targetNamespace, deleted);
                            break;
                        }
                    }
                }
            }
            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/airflow/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            List<Map<String, Object>> mapList = new ArrayList<>();

            Components components = componentsService.findOne(COMPONENT_AIRFLOW);
            if(components != null) {
                for(CustomResource customResource : components.getCustomResourceSet()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("kind", customResource.getKind());
                    map.put("name", customResource.getName());
                    map.put("namespace", customResource.getNamespace());
                    map.put("components", COMPONENT_AIRFLOW);

                    mapList.add(map);
                }
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
