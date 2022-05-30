package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.StreamingService;
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
public class StreamingController {

    private static Logger LOG = LoggerFactory.getLogger(StreamingController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("streamingServiceImpl")
    private StreamingService streamingService;

    @PostMapping("/apis/streaming/create")
    public String createStreaming(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectId = params.get("project_id");
            String serviceDefId = params.get("service_def_id");
            String clusterId = params.get("cluster_id");
            String kafkaReplicaCount = params.get("kafka_replica_count");
            String kafkaStorageSize = params.get("kafka_storage_size");
            String storageClass = params.get("storage_class");
            String zkReplicaCount = params.get("zk_replica_count");

            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            streamingService.create(Long.valueOf(projectId),
                    Long.valueOf(serviceDefId),
                    Long.valueOf(clusterId),
                    userName,
                    Integer.valueOf(kafkaReplicaCount),
                    Integer.valueOf(kafkaStorageSize),
                    storageClass,
                    Integer.valueOf(zkReplicaCount));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/streaming/delete")
    public String deleteStreaming(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String serviceId = params.get("service_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            streamingService.delete(Long.valueOf(serviceId), userName);
            return ControllerUtils.successMessage();
        });
    }
}
