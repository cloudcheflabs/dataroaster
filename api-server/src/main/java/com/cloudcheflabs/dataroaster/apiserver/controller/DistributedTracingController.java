package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.DistributedTracingService;
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
public class DistributedTracingController {

    private static Logger LOG = LoggerFactory.getLogger(DistributedTracingController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("distributedTracingServiceImpl")
    private DistributedTracingService distributedTracingService;

    @PostMapping("/apis/distributed_tracing/create")
    public String createDistributedTracing(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectId = params.get("project_id");
            String serviceDefId = params.get("service_def_id");
            String clusterId = params.get("cluster_id");
            String storageClass = params.get("storage_class");
            String ingressHost = params.get("ingress_host");
            String elasticsearchHostPort = params.get("elasticsearch_host_port");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            distributedTracingService.create(Long.valueOf(projectId),
                    Long.valueOf(serviceDefId),
                    Long.valueOf(clusterId),
                    userName,
                    storageClass,
                    ingressHost,
                    elasticsearchHostPort);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/distributed_tracing/delete")
    public String deleteDistributedTracing(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String serviceId = params.get("service_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            distributedTracingService.delete(Long.valueOf(serviceId), userName);
            return ControllerUtils.successMessage();
        });
    }
}
