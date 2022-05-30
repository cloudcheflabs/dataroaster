package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.DataCatalogService;
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
public class DataCatalogController {

    private static Logger LOG = LoggerFactory.getLogger(DataCatalogController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("dataCatalogServiceImpl")
    private DataCatalogService dataCatalogService;

    @PostMapping("/apis/data_catalog/create")
    public String createDataCatalog(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectId = params.get("project_id");
            String serviceDefId = params.get("service_def_id");
            String clusterId = params.get("cluster_id");
            String s3Bucket = params.get("s3_bucket");
            String s3AccessKey = params.get("s3_access_key");
            String s3SecretKey = params.get("s3_secret_key");
            String s3Endpoint = params.get("s3_endpoint");
            String storageClass = params.get("storage_class");
            String storageSize = params.get("storage_size");

            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);


            dataCatalogService.create(Long.valueOf(projectId),
                    Long.valueOf(serviceDefId),
                    Long.valueOf(clusterId),
                    userName,
                    s3Bucket,
                    s3AccessKey,
                    s3SecretKey,
                    s3Endpoint,
                    storageClass,
                    Integer.valueOf(storageSize));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/data_catalog/delete")
    public String deleteDataCatalog(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String serviceId = params.get("service_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            dataCatalogService.delete(Long.valueOf(serviceId), userName);
            return ControllerUtils.successMessage();
        });
    }
}
