package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.ResourceControlService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.StorageClass;
import com.cloudcheflabs.dataroaster.apiserver.filter.AuthorizationFilter;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class ResourceControlController {

    private static Logger LOG = LoggerFactory.getLogger(ResourceControlController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("resourceControlServiceImpl")
    private ResourceControlService resourceControlService;

    @GetMapping("/apis/resource_control/list_storageclass")
    public String listStorageClass(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String clusterId = params.get("cluster_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            List<StorageClass> storageClasses = resourceControlService.listStorageClasses(Long.valueOf(clusterId), userName);

            return JsonUtils.toJson(mapper, storageClasses);
        });
    }

    @GetMapping("/apis/resource_control/ingress_controller/get_external_ip")
    public String getExternalIpOfIngressControllerNginx(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String clusterId = params.get("cluster_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            String externalIp = resourceControlService.getExternalIpOfIngressControllerNginx(Long.valueOf(clusterId), userName);

            return JsonUtils.toJson(mapper, externalIp);
        });
    }
}
