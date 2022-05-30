package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.QueryEngineService;
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
public class QueryEngineController {

    private static Logger LOG = LoggerFactory.getLogger(QueryEngineController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("queryEngineServiceImpl")
    private QueryEngineService queryEngineService;

    @PostMapping("/apis/query_engine/create")
    public String createQueryEngine(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectId = params.get("project_id");
            String serviceDefId = params.get("service_def_id");
            String clusterId = params.get("cluster_id");
            String s3Bucket = params.get("s3_bucket");
            String s3AccessKey = params.get("s3_access_key");
            String s3SecretKey = params.get("s3_secret_key");
            String s3Endpoint = params.get("s3_endpoint");
            String sparkThriftServerStorageClass = params.get("spark_thrift_server_storage_class");
            String sparkThriftServerExecutors = params.get("spark_thrift_server_executors");
            String sparkThriftServerExecutorMemory = params.get("spark_thrift_server_executor_memory");
            String sparkThriftServerExecutorCores = params.get("spark_thrift_server_executor_cores");
            String sparkThriftServerDriverMemory = params.get("spark_thrift_server_driver_memory");
            String trinoWorkers = params.get("trino_workers");
            String trinoServerMaxMemory = params.get("trino_server_max_memory");
            String trinoCores = params.get("trino_cores");
            String trinoTempDataStorage = params.get("trino_temp_data_storage");
            String trinoDataStorage = params.get("trino_data_storage");
            String trinoStorageClass = params.get("trino_storage_class");

            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            queryEngineService.create(Long.valueOf(projectId),
                    Long.valueOf(serviceDefId),
                    Long.valueOf(clusterId),
                    userName,
                    s3Bucket,
                    s3AccessKey,
                    s3SecretKey,
                    s3Endpoint,
                    sparkThriftServerStorageClass,
                    Integer.valueOf(sparkThriftServerExecutors),
                    Integer.valueOf(sparkThriftServerExecutorMemory),
                    Integer.valueOf(sparkThriftServerExecutorCores),
                    Integer.valueOf(sparkThriftServerDriverMemory),
                    Integer.valueOf(trinoWorkers),
                    Integer.valueOf(trinoServerMaxMemory),
                    Integer.valueOf(trinoCores),
                    Integer.valueOf(trinoTempDataStorage),
                    Integer.valueOf(trinoDataStorage),
                    trinoStorageClass);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/query_engine/delete")
    public String deleteQueryEngine(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String serviceId = params.get("service_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            queryEngineService.delete(Long.valueOf(serviceId), userName);
            return ControllerUtils.successMessage();
        });
    }
}
