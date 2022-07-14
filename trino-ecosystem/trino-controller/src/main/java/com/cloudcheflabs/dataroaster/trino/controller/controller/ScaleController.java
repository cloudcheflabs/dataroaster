package com.cloudcheflabs.dataroaster.trino.controller.controller;

import com.cloudcheflabs.dataroaster.trino.controller.api.service.ScaleWorkerService;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.cloudcheflabs.dataroaster.trino.controller.controller.TrinoController.DEFAULT_TRINO_OPERATOR_NAMESPACE;

@RestController
public class ScaleController implements InitializingBean {

    private static Logger LOG = LoggerFactory.getLogger(ScaleController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;


    @Autowired
    private KubernetesClient client;

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("scaleWorkerServiceImpl")
    private ScaleWorkerService scaleWorkerService;

    private String trinoOperatorRestUri;

    @Override
    public void afterPropertiesSet() throws Exception {
        trinoOperatorRestUri = env.getProperty("trino.operator.restUri");
    }

    @GetMapping("/v1/scale/list_worker_count")
    public String listWorkerCount(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            RestResponse restResponse = scaleWorkerService.listWorkerCount(trinoOperatorRestUri, DEFAULT_TRINO_OPERATOR_NAMESPACE);
            return (restResponse.getStatusCode() == 200) ? restResponse.getSuccessMessage() : restResponse.getErrorMessage();
        });
    }

    @PutMapping("/v1/scale/scale_workers")
    public String scaleWorkers(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");
            LOG.info("name: {}", name);

            String replicas = params.get("replicas");
            LOG.info("replicas: {}", replicas);

            scaleWorkerService.scaleOutWorkers(trinoOperatorRestUri, DEFAULT_TRINO_OPERATOR_NAMESPACE, name, Integer.valueOf(replicas));

            return ControllerUtils.successMessage();
        });
    }


    @GetMapping("/v1/scale/list_hpa")
    public String listHpa(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            RestResponse restResponse = scaleWorkerService.listHpa(trinoOperatorRestUri, DEFAULT_TRINO_OPERATOR_NAMESPACE);
            return (restResponse.getStatusCode() == 200) ? restResponse.getSuccessMessage() : restResponse.getErrorMessage();
        });
    }


    @PutMapping("/v1/scale/scale_hpa")
    public String scaleHpa(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");
            LOG.info("name: {}", name);

            String minReplicas = params.get("min_replicas");
            LOG.info("minReplicas: {}", minReplicas);

            String maxReplicas = params.get("max_replicas");
            LOG.info("maxReplicas: {}", maxReplicas);

            scaleWorkerService.updateHpa(trinoOperatorRestUri, DEFAULT_TRINO_OPERATOR_NAMESPACE, name, Integer.valueOf(minReplicas), Integer.valueOf(maxReplicas));

            return ControllerUtils.successMessage();
        });
    }
}
