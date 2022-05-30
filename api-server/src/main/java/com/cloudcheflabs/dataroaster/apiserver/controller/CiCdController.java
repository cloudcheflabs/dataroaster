package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.CiCdService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.filter.AuthorizationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class CiCdController {

    private static Logger LOG = LoggerFactory.getLogger(CiCdController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("ciCdServiceImpl")
    private CiCdService ciCdService;

    @PostMapping("/apis/ci_cd/create")
    public String createCiCd(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectId = params.get("project_id");
            String serviceDefId = params.get("service_def_id");
            String clusterId = params.get("cluster_id");
            String argocdIngressHost = params.get("argocd_ingress_host");
            String jenkinsIngressHost = params.get("jenkins_ingress_host");
            String storageClass = params.get("storage_class");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            ciCdService.create(Long.valueOf(projectId),
                    Long.valueOf(serviceDefId),
                    Long.valueOf(clusterId),
                    userName,
                    argocdIngressHost,
                    jenkinsIngressHost,
                    storageClass);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/ci_cd/delete")
    public String deleteCiCd(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String serviceId = params.get("service_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            ciCdService.delete(Long.valueOf(serviceId), userName);
            return ControllerUtils.successMessage();
        });
    }
}
