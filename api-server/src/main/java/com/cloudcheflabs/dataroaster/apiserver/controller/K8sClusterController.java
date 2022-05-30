package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.K8sClusterService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import com.cloudcheflabs.dataroaster.apiserver.filter.AuthorizationFilter;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
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
public class K8sClusterController {

    private static Logger LOG = LoggerFactory.getLogger(K8sClusterController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("k8sClusterServiceImpl")
    private K8sClusterService k8sClusterService;

    @PostMapping("/apis/k8s/create_cluster")
    public String createCluster(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterName = params.get("cluster_name");
            String description = params.get("description");

            k8sClusterService.createCluster(clusterName, description);
            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/apis/k8s/update_cluster")
    public String updateCluster(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String clusterName = params.get("cluster_name");
            String description = params.get("description");

            k8sClusterService.updateCluster(Long.valueOf(clusterId), clusterName, description);
            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/apis/k8s/delete_cluster")
    public String deleteCluster(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");

            k8sClusterService.deleteCluster(Long.valueOf(clusterId));
            return ControllerUtils.successMessage();
        });
    }


    @PostMapping("/apis/k8s/create_kubeconfig")
    public String createKubeconfig(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String clusterId = params.get("cluster_id");
            String kubeconfig = params.get("kubeconfig");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            k8sClusterService.createKubeconfig(Long.valueOf(clusterId), kubeconfig, userName);
            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/apis/k8s/update_kubeconfig")
    public String updateKubeconfig(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String clusterId = params.get("cluster_id");
            String kubeconfig = params.get("kubeconfig");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            k8sClusterService.updateKubeconfig(Long.valueOf(clusterId), kubeconfig, userName);
            return ControllerUtils.successMessage();
        });
    }


    @GetMapping("/apis/k8s/get_kubeconfig")
    public String getKubeconfig(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String clusterId = params.get("cluster_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            Kubeconfig kubeconfig = k8sClusterService.getKubeconfig(Long.valueOf(clusterId), userName);

            return kubeconfig.getRawKubeconfig();
        });
    }


    @GetMapping("/apis/k8s/list_cluster")
    public String listCluster(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            List<K8sCluster> list = k8sClusterService.findAll();

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(K8sCluster k8sCluster : list) {
                long id = k8sCluster.getId();
                String clusterName = k8sCluster.getClusterName();
                String description = k8sCluster.getDescription();

                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("name", clusterName);
                map.put("description", description);

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
