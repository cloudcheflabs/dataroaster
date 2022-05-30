package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.PrivateRegistryService;
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
public class PrivateRegistryController {

    private static Logger LOG = LoggerFactory.getLogger(PrivateRegistryController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("privateRegistryServiceImpl")
    private PrivateRegistryService privateRegistryService;

    @PostMapping("/apis/private_registry/create")
    public String createPrivateRegistry(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectId = params.get("project_id");
            String serviceDefId = params.get("service_def_id");
            String clusterId = params.get("cluster_id");
            String coreHost = params.get("core_host");
            String notaryHost = params.get("notary_host");
            String storageClass = params.get("storage_class");
            String registryStorageSize = params.get("registry_storage_size");
            String chartmuseumStorageSize = params.get("chartmuseum_storage_size");
            String jobserviceStorageSize = params.get("jobservice_storage_size");
            String databaseStorageSize = params.get("database_storage_size");
            String redisStorageSize = params.get("redis_storage_size");
            String trivyStorageSize = params.get("trivy_storage_size");
            String s3Bucket = params.get("s3_bucket");
            String s3AccessKey = params.get("s3_access_key");
            String s3SecretKey = params.get("s3_secret_key");
            String s3Endpoint = params.get("s3_endpoint");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);



            privateRegistryService.create(Long.valueOf(projectId),
                    Long.valueOf(serviceDefId),
                    Long.valueOf(clusterId),
                    userName,
                    coreHost,
                    notaryHost,
                    storageClass,
                    Integer.valueOf(registryStorageSize),
                    Integer.valueOf(chartmuseumStorageSize),
                    Integer.valueOf(jobserviceStorageSize),
                    Integer.valueOf(databaseStorageSize),
                    Integer.valueOf(redisStorageSize),
                    Integer.valueOf(trivyStorageSize),
                    s3Bucket,
                    s3AccessKey,
                    s3SecretKey,
                    s3Endpoint);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/private_registry/delete")
    public String deletePrivateRegistry(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String serviceId = params.get("service_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            privateRegistryService.delete(Long.valueOf(serviceId), userName);
            return ControllerUtils.successMessage();
        });
    }
}
