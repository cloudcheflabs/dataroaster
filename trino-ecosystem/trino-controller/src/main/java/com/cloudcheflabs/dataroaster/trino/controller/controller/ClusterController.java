package com.cloudcheflabs.dataroaster.trino.controller.controller;

import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class ClusterController {

    private static Logger LOG = LoggerFactory.getLogger(ClusterController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;



    @PostMapping("/v1/cluster/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterName = params.get("cluster_name");
            String clusterType = params.get("cluster_type");
            String url = params.get("url");
            String activated = params.get("activated");
            String groupName = params.get("group_name");

            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/v1/cluster/update/activated")
    public String updateActivated(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterName = params.get("cluster_name");
            String activated = params.get("activated");

            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/v1/cluster/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterName = params.get("cluster_name");

            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/cluster/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            // TODO
            String json = "";

            return json;
        });
    }
}
