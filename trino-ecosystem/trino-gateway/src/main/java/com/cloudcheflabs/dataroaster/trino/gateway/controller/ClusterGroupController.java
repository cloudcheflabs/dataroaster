package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterGroupService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.Roles;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class ClusterGroupController {

    private static Logger LOG = LoggerFactory.getLogger(ClusterGroupController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("clusterGroupServiceImpl")
    private ClusterGroupService clusterGroupService;

    @PostMapping("/v1/cluster_group/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String groupName = params.get("group_name");
            ClusterGroup clusterGroup = new ClusterGroup();
            clusterGroup.setGroupName(groupName);
            clusterGroupService.create(clusterGroup);
            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/v1/cluster_group/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String groupName = params.get("group_name");
            clusterGroupService.deleteById(groupName);
            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/cluster_group/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            List<Map<String, Object>> mapList = new ArrayList<>();

            List<ClusterGroup> lists = clusterGroupService.findAll();
            for(ClusterGroup clusterGroup : lists) {
                String groupName = clusterGroup.getGroupName();

                Map<String, Object> map = new HashMap<>();
                map.put("groupName", groupName);

                Set<Cluster> clusters = clusterGroup.getClusterSet();
                List<Map<String, Object>> clusterList = new ArrayList<>();
                for(Cluster cluster : clusters) {
                    Map<String, Object> clusterMap = new HashMap<>();
                    clusterMap.put("clusterName", cluster.getClusterName());
                    clusterMap.put("clusterType", cluster.getClusterType());
                    clusterMap.put("url", cluster.getUrl());
                    clusterMap.put("activated", cluster.isActivated());

                    clusterList.add(clusterMap);
                }
                map.put("clusters", clusterList);

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
