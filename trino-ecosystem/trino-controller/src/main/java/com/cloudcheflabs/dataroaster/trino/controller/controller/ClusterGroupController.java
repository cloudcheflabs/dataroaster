package com.cloudcheflabs.dataroaster.trino.controller.controller;

import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ClusterGroupController {

    private static Logger LOG = LoggerFactory.getLogger(ClusterGroupController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;


    @PostMapping("/v1/cluster_group/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String groupName = params.get("group_name");

            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/v1/cluster_group/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String groupName = params.get("group_name");

            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/cluster_group/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            List<Map<String, Object>> mapList = new ArrayList<>();

            // TODO
            String json = "";

            return json;
        });
    }
}
