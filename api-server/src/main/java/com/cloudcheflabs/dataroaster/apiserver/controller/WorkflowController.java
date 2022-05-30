package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.WorkflowService;
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
public class WorkflowController {

    private static Logger LOG = LoggerFactory.getLogger(WorkflowController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("workflowServiceImpl")
    private WorkflowService workflowService;

    @PostMapping("/apis/workflow/create")
    public String createWorkflow(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectId = params.get("project_id");
            String serviceDefId = params.get("service_def_id");
            String clusterId = params.get("cluster_id");
            String storageClass = params.get("storage_class");
            String storageSize = params.get("storage_size");
            String s3Bucket = params.get("s3_bucket");
            String s3AccessKey = params.get("s3_access_key");
            String s3SecretKey = params.get("s3_secret_key");
            String s3Endpoint = params.get("s3_endpoint");

            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            workflowService.create(Long.valueOf(projectId),
                    Long.valueOf(serviceDefId),
                    Long.valueOf(clusterId),
                    userName,
                    storageClass,
                    Integer.valueOf(storageSize),
                    s3Bucket,
                    s3AccessKey,
                    s3SecretKey,
                    s3Endpoint);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/workflow/delete")
    public String deleteWorkflow(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String serviceId = params.get("service_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            workflowService.delete(Long.valueOf(serviceId), userName);
            return ControllerUtils.successMessage();
        });
    }
}
