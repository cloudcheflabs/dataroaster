package com.cloudcheflabs.dataroaster.operators.dataroaster.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
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

@RestController
public class KafkaController {

    private static Logger LOG = LoggerFactory.getLogger(KafkaController.class);

    public static final String COMPONENT_KAFKA = "kafka";


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


    @PostMapping("/v1/kafka/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {


            String yaml = params.get("yaml");

            String decodedYaml = Base64Utils.decodeBase64(yaml);
            LOG.info("decodedYaml: {}", decodedYaml);

            Components components = componentsService.findOne(COMPONENT_KAFKA);
            if(components == null) {
                components = new Components();
                components.setCompName(COMPONENT_KAFKA);
                componentsService.create(components);
            }

            // create kafka custom resource.
            CustomResource kafkaCustomResource = CustomResourceUtils.fromYaml(decodedYaml);
            kafkaCustomResource.setComponents(components);
            k8sResourceService.createCustomResource(kafkaCustomResource);

            return ControllerUtils.successMessage();
        });
    }




    @DeleteMapping("/v1/kafka/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            Components components = componentsService.findOne(COMPONENT_KAFKA);
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

    @GetMapping("/v1/kafka/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            List<Map<String, Object>> mapList = new ArrayList<>();

            Components components = componentsService.findOne(COMPONENT_KAFKA);
            if(components != null) {
                for(CustomResource customResource : components.getCustomResourceSet()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("kind", customResource.getKind());
                    map.put("name", customResource.getName());
                    map.put("namespace", customResource.getNamespace());
                    map.put("components", COMPONENT_KAFKA);

                    mapList.add(map);
                }
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
