package com.cloudcheflabs.dataroaster.trino.controller.controller;

import com.cloudcheflabs.dataroaster.trino.controller.api.service.RegisterClusterService;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class ClusterController implements InitializingBean {

    private static Logger LOG = LoggerFactory.getLogger(ClusterController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("registerClusterServiceImpl")
    private RegisterClusterService registerClusterService;

    private String trinoGatewayRestUri;

    @Override
    public void afterPropertiesSet() throws Exception {
        trinoGatewayRestUri = env.getProperty("trino.gateway.restUri");
    }

    @PostMapping("/v1/cluster/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterName = params.get("cluster_name");
            String clusterType = params.get("cluster_type");
            String url = params.get("url");
            String activated = params.get("activated");
            String groupName = params.get("group_name");

            registerClusterService.registerCluster(trinoGatewayRestUri, clusterName, clusterType, url, Boolean.valueOf(activated), groupName);

            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/v1/cluster/update/activated")
    public String updateActivated(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterName = params.get("cluster_name");
            String activated = params.get("activated");

            registerClusterService.updateClusterActivated(trinoGatewayRestUri, clusterName, Boolean.valueOf(activated));

            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/v1/cluster/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterName = params.get("cluster_name");

            registerClusterService.deregisterCluster(trinoGatewayRestUri, clusterName);

            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/cluster/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            RestResponse restResponse = registerClusterService.listClusters(trinoGatewayRestUri);
            return (restResponse.getStatusCode() == 200) ? restResponse.getSuccessMessage() : restResponse.getErrorMessage();
        });
    }
}
