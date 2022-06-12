package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterGroupService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.Roles;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ClusterController {

    private static Logger LOG = LoggerFactory.getLogger(ClusterController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("clusterServiceImpl")
    private ClusterService clusterService;

    @Autowired
    @Qualifier("clusterGroupServiceImpl")
    private ClusterGroupService clusterGroupService;

    @PostMapping("/v1/cluster/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterName = params.get("cluster_name");
            String clusterType = params.get("cluster_type");
            String url = params.get("url");
            String activated = params.get("activated");
            String groupName = params.get("group_name");

            Cluster cluster = new Cluster();
            cluster.setClusterName(clusterName);
            cluster.setClusterType(clusterType);
            cluster.setUrl(url);
            cluster.setActivated(Boolean.valueOf(activated));
            cluster.setClusterGroup(clusterGroupService.findOne(groupName));

            clusterService.create(cluster);
            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/v1/cluster/update/activated")
    public String updateActivated(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String clusterName = params.get("cluster_name");
            String activated = params.get("activated");

            Cluster cluster = clusterService.findOne(clusterName);
            cluster.setActivated(Boolean.valueOf(activated));
            clusterService.update(cluster);
            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/v1/cluster/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterName = params.get("cluster_name");
            clusterService.deleteById(clusterName);
            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/cluster/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            List<Map<String, Object>> mapList = new ArrayList<>();

            List<Cluster> lists = clusterService.findAll();
            for(Cluster cluster : lists) {
                Map<String, Object> map = new HashMap<>();
                map.put("clusterName", cluster.getClusterName());
                map.put("clusterType", cluster.getClusterType());
                map.put("url", cluster.getUrl());
                map.put("activated", cluster.isActivated());
                map.put("groupName", cluster.getClusterGroup().getGroupName());

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
