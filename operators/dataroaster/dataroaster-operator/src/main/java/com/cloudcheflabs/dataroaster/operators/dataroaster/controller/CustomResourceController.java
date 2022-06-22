package com.cloudcheflabs.dataroaster.operators.dataroaster.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.CustomResourceService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.K8sResourceService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.Roles;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class CustomResourceController {

    private static Logger LOG = LoggerFactory.getLogger(CustomResourceController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("k8sResourceServiceImpl")
    private K8sResourceService k8sResourceService;


    @Autowired
    @Qualifier("customResourceServiceImpl")
    private CustomResourceService customResourceService;


    @PostMapping("/v1/cr/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String yaml = params.get("yaml");

            // create custom resource on kubernetes.
            CustomResource customResource = CustomResourceUtils.fromYaml(yaml);
            k8sResourceService.createCustomResource(customResource);

            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/v1/cr/update")
    public String update(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String yaml = params.get("yaml");

            // create custom resource on kubernetes.
            CustomResource customResource = CustomResourceUtils.fromYaml(yaml);
            k8sResourceService.createCustomResource(customResource);

            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/v1/cr/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");
            String namespace = params.get("namespace");
            String kind = params.get("kind");
            k8sResourceService.deleteCustomResource(name, namespace, kind);
            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/cr/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            List<Map<String, Object>> mapList = new ArrayList<>();

            for(CustomResource customResource : customResourceService.findAll()) {
                Map<String, Object> map = new HashMap<>();
                map.put("kind", customResource.getKind());
                map.put("name", customResource.getName());
                map.put("namespace", customResource.getNamespace());
                map.put("yaml", customResource.getYaml());

                mapList.add(map);
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
