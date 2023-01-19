package com.cloudcheflabs.dataroaster.operators.trino.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.operators.trino.api.dao.K8sResourceDao;
import com.cloudcheflabs.dataroaster.operators.trino.crd.Autoscaler;
import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import com.cloudcheflabs.dataroaster.operators.trino.domain.Roles;
import com.cloudcheflabs.dataroaster.operators.trino.util.YamlUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cloudcheflabs.dataroaster.operators.trino.config.TrinoConfiguration.DEFAULT_WORKER_DEPLOYMENT;

@RestController
public class ScaleController implements InitializingBean {

    private static Logger LOG = LoggerFactory.getLogger(ScaleController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;


    @Autowired
    private KubernetesClient client;

    @Autowired
    private K8sResourceDao k8sResourceDao;

    private MixedOperation<TrinoCluster, KubernetesResourceList<TrinoCluster>, Resource<TrinoCluster>> trinoClusterClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        trinoClusterClient = client.resources(TrinoCluster.class);
    }


    @GetMapping("/v1/scale/list_worker_count")
    public String listWorkerCount(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);

            List<Map<String, Object>> mapList = new ArrayList<>();

            KubernetesResourceList<TrinoCluster> resourceList = trinoClusterClient.inNamespace(namespace).list();
            List<TrinoCluster> trinoClusters = resourceList.getItems();
            for(TrinoCluster trinoCluster : trinoClusters) {
                String clusterName = trinoCluster.getMetadata().getName();
                String clusterNamespace = trinoCluster.getSpec().getNamespace();
                Deployment workerDeployment = client.apps().deployments().inNamespace(clusterNamespace).withName(DEFAULT_WORKER_DEPLOYMENT).get();
                //LOG.info("workerDeployment: \n{}", YamlUtils.objectToYaml(workerDeployment));
                int replicas = 0;
                if(workerDeployment != null) {
                    replicas = workerDeployment.getSpec().getReplicas();
                }

                Map<String, Object> map = new HashMap<>();
                map.put("clusterName", clusterName);
                map.put("clusterNamespace", clusterNamespace);
                map.put("workerReplicas", replicas);

                mapList.add(map);
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }

    @PutMapping("/v1/scale/scale_workers")
    public String scaleWorkers(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);
            String clusterName = params.get("cluster_name");
            LOG.info("clusterName: {}", clusterName);

            String replicas = params.get("replicas");
            LOG.info("replicas: {}", replicas);

            List<GenericKubernetesResource> trinoClusters =
                    k8sResourceDao.listCustomResources(namespace, "TrinoCluster");

            for (GenericKubernetesResource genericKubernetesResource : trinoClusters) {
                String trinoClusterName = genericKubernetesResource.getMetadata().getName();
                if(trinoClusterName.equals(clusterName)) {
                    Map<String, Object> additionalMap = genericKubernetesResource.getAdditionalProperties();
                    Map<String, Object> specMap = (Map<String, Object>) additionalMap.get("spec");

                    // add worker pod template.
                    Map<String, Object> workerMap = (Map<String, Object>) specMap.get("worker");
                    workerMap.put("replicas", Integer.valueOf(replicas));

                    LOG.info("updated generic custom resource: \n{}", YamlUtils.objectToYaml(genericKubernetesResource));

                    k8sResourceDao.updateCustomResource(genericKubernetesResource);
                    LOG.info("cluster [{}] configs updated.", clusterName);
                    break;
                }
            }

            return ControllerUtils.successMessage();
        });
    }


    @GetMapping("/v1/scale/list_hpa")
    public String listHpa(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);

            List<Map<String, Object>> mapList = new ArrayList<>();

            KubernetesResourceList<TrinoCluster> resourceList = trinoClusterClient.inNamespace(namespace).list();
            List<TrinoCluster> trinoClusters = resourceList.getItems();
            for(TrinoCluster trinoCluster : trinoClusters) {
                String clusterName = trinoCluster.getMetadata().getName();
                String clusterNamespace = trinoCluster.getSpec().getNamespace();

                Autoscaler autoscaler = trinoCluster.getSpec().getWorker().getAutoscaler();

                Map<String, Object> map = new HashMap<>();
                map.put("clusterName", clusterName);
                map.put("clusterNamespace", clusterNamespace);
                map.put("minReplicas", autoscaler.getMinReplicas());
                map.put("maxReplicas", autoscaler.getMaxReplicas());

                mapList.add(map);
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }


    @PutMapping("/v1/scale/scale_hpa")
    public String scaleHpa(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);
            String clusterName = params.get("cluster_name");
            LOG.info("clusterName: {}", clusterName);

            String minReplicas = params.get("min_replicas");
            String maxReplicas = params.get("max_replicas");

            TrinoCluster trinoCluster = trinoClusterClient.inNamespace(namespace).withName(clusterName).get();
            Autoscaler autoscaler = trinoCluster.getSpec().getWorker().getAutoscaler();
            autoscaler.setMinReplicas(Integer.valueOf(minReplicas));
            autoscaler.setMaxReplicas(Integer.valueOf(maxReplicas));
            trinoCluster.getSpec().getWorker().setAutoscaler(autoscaler);

            // update trino cluster custom resource.
            trinoClusterClient.inNamespace(namespace).withName(clusterName).createOrReplace(trinoCluster);

            return ControllerUtils.successMessage();
        });
    }
}
