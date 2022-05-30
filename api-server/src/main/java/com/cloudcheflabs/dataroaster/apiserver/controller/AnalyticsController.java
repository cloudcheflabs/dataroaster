package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.AnalyticsService;
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
public class AnalyticsController {

    private static Logger LOG = LoggerFactory.getLogger(AnalyticsController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("analyticsServiceImpl")
    private AnalyticsService analyticsService;

    @PostMapping("/apis/analytics/create")
    public String createAnalytics(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectId = params.get("project_id");
            String serviceDefId = params.get("service_def_id");
            String clusterId = params.get("cluster_id");
            String jupyterhubGithubClientId = params.get("jupyterhub_github_client_id");
            String jupyterhubGithubClientSecret = params.get("jupyterhub_github_client_secret");
            String jupyterhubIngressHost = params.get("jupyterhub_ingress_host");
            String storageClass = params.get("storage_class");
            String jupyterhubStorageSize = params.get("jupyterhub_storage_size");
            String redashStorageSize = params.get("redash_storage_size");

            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            analyticsService.create(Long.valueOf(projectId),
                    Long.valueOf(serviceDefId),
                    Long.valueOf(clusterId),
                    userName,
                    jupyterhubGithubClientId,
                    jupyterhubGithubClientSecret,
                    jupyterhubIngressHost,
                    storageClass,
                    Integer.valueOf(jupyterhubStorageSize),
                    Integer.valueOf(redashStorageSize));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/analytics/delete")
    public String deleteAnalytics(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String serviceId = params.get("service_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            analyticsService.delete(Long.valueOf(serviceId), userName);
            return ControllerUtils.successMessage();
        });
    }
}
