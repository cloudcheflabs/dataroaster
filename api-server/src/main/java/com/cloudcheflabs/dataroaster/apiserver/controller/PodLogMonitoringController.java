package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.PodLogMonitoringService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class PodLogMonitoringController {

    private static Logger LOG = LoggerFactory.getLogger(PodLogMonitoringController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("podLogMonitoringServiceImpl")
    private PodLogMonitoringService podLogMonitoringService;

    @PostMapping("/apis/pod_log_monitoring/create")
    public String createPodLogMonitoring(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectId = params.get("project_id");
            String serviceDefId = params.get("service_def_id");
            String clusterId = params.get("cluster_id");
            String elasticsearchHosts = params.get("elasticsearch_hosts");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            List<String> elasticsearchHostList = new ArrayList<>();
            for(String token : elasticsearchHosts.split(",")) {
                elasticsearchHostList.add(token.trim());
            }

            podLogMonitoringService.create(Long.valueOf(projectId), Long.valueOf(serviceDefId), Long.valueOf(clusterId), userName, elasticsearchHostList);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/pod_log_monitoring/delete")
    public String deletePodLogMonitoring(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String serviceId = params.get("service_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            podLogMonitoringService.delete(Long.valueOf(serviceId), userName);
            return ControllerUtils.successMessage();
        });
    }
}
