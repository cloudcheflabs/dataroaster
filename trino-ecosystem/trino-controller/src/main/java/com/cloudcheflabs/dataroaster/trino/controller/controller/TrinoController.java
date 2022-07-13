package com.cloudcheflabs.dataroaster.trino.controller.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import com.cloudcheflabs.dataroaster.trino.controller.api.service.ClusterJmxService;
import com.cloudcheflabs.dataroaster.trino.controller.api.service.K8sResourceService;
import com.cloudcheflabs.dataroaster.trino.controller.component.Initializer;
import com.cloudcheflabs.dataroaster.trino.controller.domain.CustomResource;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import com.cloudcheflabs.dataroaster.trino.controller.util.ContainerStatusChecker;
import com.cloudcheflabs.dataroaster.trino.controller.util.CustomResourceUtils;
import com.cloudcheflabs.dataroaster.trino.controller.util.YamlUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class TrinoController {

    private static Logger LOG = LoggerFactory.getLogger(TrinoController.class);

    private ObjectMapper mapper = new ObjectMapper();

    public static final String DEFAULT_TRINO_OPERATOR_NAMESPACE = "trino-operator";

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("kubernetesClient")
    private KubernetesClient kubernetesClient;

    @Autowired
    @Qualifier("k8sResourceServiceImpl")
    private K8sResourceService k8sResourceService;

    @Autowired
    @Qualifier("clusterJmxServiceImpl")
    private ClusterJmxService clusterJmxService;



    @PostMapping("/v1/trino/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");
            String namespace = params.get("namespace");
            String maxHeapSize = params.get("max_heap_size");
            String replicas = params.get("replicas");
            String minReplicas = params.get("min_replicas");
            String maxReplicas = params.get("max_replicas");
            String storageClass = params.get("storage_class");

            int replicasInt = Integer.valueOf(replicas);
            int minReplicasInt = Integer.valueOf(minReplicas);
            int maxReplicasInt = Integer.valueOf(maxReplicas);

            if(minReplicasInt >= maxReplicasInt) {
                throw new IllegalArgumentException("minReplicas must be less than maxReplicas");
            }
            if(minReplicasInt > replicasInt) {
                throw new IllegalArgumentException("minReplicas must be less than or equals to replicas");
            }


            Map<String, Object> kv = new HashMap<>();
            kv.put("name", name);
            kv.put("namespace", namespace);
            kv.put("maxHeapSize", maxHeapSize);
            kv.put("replicas", replicas);
            kv.put("maxReplicas", maxReplicas);
            kv.put("minReplicas", minReplicas);
            String trinoClusterString =
                    TemplateUtils.replace("/templates/cr/trino-cluster-jmx.yaml", true, kv);


            // trino cluster namespace.
            String trinoClusterNamespace = CustomResourceUtils.getTargetNamespace(trinoClusterString);

            // check if trino cluster is running.
            boolean trinoClusterRunning = ContainerStatusChecker.isRunning(
                    kubernetesClient,
                    "trino-coordinator",
                    trinoClusterNamespace,
                    "component",
                    "coordinator"
            );
            LOG.info("trino cluster in namespace [{}] is running: {}", trinoClusterNamespace, trinoClusterRunning);

            if(trinoClusterRunning) {
                throw new IllegalStateException("trino cluster in namespace [" + trinoClusterNamespace + "] already running.");
            }

            // build custom resource of trino cluster.
            CustomResource trinoClusterCr = CustomResourceUtils.fromYaml(trinoClusterString);

            // create cr of trino cluster.
            k8sResourceService.createCustomResource(trinoClusterCr);
            LOG.info("trino cluster custom resource created...");

            // wait for that trino cluster will be run.
            ContainerStatusChecker.checkContainerStatus(
                    kubernetesClient,
                    "trino-coordinator",
                    trinoClusterNamespace,
                    "component",
                    "coordinator",
                    50
            );


            // replace prometheus template.
            kv = new HashMap<>();
            kv.put("customResourceNamespace", Initializer.getNamespace());
            kv.put("storageClass", storageClass);
            String prometheusString =
                    TemplateUtils.replace("/templates/cr/prometheus.yaml", true, kv);


            // prometheus namespace.
            String prometheusNamespace = CustomResourceUtils.getTargetNamespace(prometheusString);

            // check if prometheus already installed.
            boolean prometheusRunning = ContainerStatusChecker.isRunning(
                    kubernetesClient,
                    "prometheus-server",
                    prometheusNamespace,
                    "component",
                    "server"
            );
            LOG.info("prometheus in namespace [{}] is running: {}", prometheusNamespace, prometheusRunning);

            // install prometheus.
            if(!prometheusRunning) {
                // build custom resource of prometheus.
                CustomResource prometheusCr = CustomResourceUtils.fromYaml(prometheusString);

                // create cr of prometheus.
                k8sResourceService.createCustomResource(prometheusCr);
                LOG.info("prometheus custom resource created...");

                // wait for that prometheus will be run.
                ContainerStatusChecker.checkContainerStatus(
                        kubernetesClient,
                        "prometheus-server",
                        prometheusNamespace,
                        "component",
                        "server",
                        50
                );
            }


            // get trino jmx exporter endpoints from trino operator with accessing rest api of it.
            String trinoOperatorRestUri = env.getProperty("trino.operator.restUri");
            RestResponse restResponse = clusterJmxService.listClusterJmxEndpoints(DEFAULT_TRINO_OPERATOR_NAMESPACE, trinoOperatorRestUri);
            String jmxEndpointsJson = restResponse.getSuccessMessage();
            LOG.info("jmxEndpointsJson: {}", jmxEndpointsJson);

            List<Map<String, Object>> mapList = JsonUtils.toMapList(new ObjectMapper(), jmxEndpointsJson);

            // coordinator jmx exporter endpoint.
            String coordinatorJmxExporterEndpoint = null;

            // worker jmx endpoints.
            List<String> workerJmxExporterEndpoints = null;
            for(Map<String, Object> clusterMap : mapList) {
                String clusterName = (String) clusterMap.get("clusterName");
                String clusterNamespace = (String) clusterMap.get("clusterNamespace");

                if(clusterName.equals(name) && clusterNamespace.equals(trinoClusterNamespace)) {
                    List<Map<String, Object>> coordinatorJmxExporterEpList = (List<Map<String, Object>>) clusterMap.get("coordinatorJmxExporterEndpoints");

                    coordinatorJmxExporterEndpoint = (String) coordinatorJmxExporterEpList.get(0).get("address");

                    List<Map<String, Object>> workerJmxExporterEpList = (List<Map<String, Object>>) clusterMap.get("workerJmxExporterEndpoints");

                    // worker jmx endpoints.
                    workerJmxExporterEndpoints = new ArrayList<>();
                    for(Map<String, Object> workerJmxExporterEpMap : workerJmxExporterEpList) {
                        String workerJmxExporterEndpoint = (String) workerJmxExporterEpMap.get("address");
                        workerJmxExporterEndpoints.add(workerJmxExporterEndpoint);
                    }

                    break;
                }
            }

            // add jobs of jmx exporter endpoints of trino coordinator and workers to configmap in prometheus to monitor trino cluster.
            if(coordinatorJmxExporterEndpoint != null && workerJmxExporterEndpoints != null) {
                Map<String, Object> coordinatorJobMap = makePrometheusJob(name + "-coordinator", Arrays.asList(coordinatorJmxExporterEndpoint));
                Map<String, Object> workerJobMap = makePrometheusJob(name + "-workers", workerJmxExporterEndpoints);

                // get prometheus configmap.
                ConfigMap prometheusConfigMap = kubernetesClient.configMaps().inNamespace(prometheusNamespace).withName("prometheus-server").get();
                Map<String, String> dataMap = prometheusConfigMap.getData();
                String prometheusYaml = dataMap.get("prometheus.yml");
                LOG.info("prometheusYaml: {}", prometheusYaml);

                Map<String, Object> prometheusYamlMap = YamlUtils.yamlToMap(prometheusYaml);
                List<Map<String, Object>> scrapeConfigs = (List<Map<String, Object>>) prometheusYamlMap.get("scrape_configs");
                LOG.info("scapeConfigs: {}", JsonUtils.toJson(scrapeConfigs));

                scrapeConfigs.add(coordinatorJobMap);
                scrapeConfigs.add(workerJobMap);

                // update scrape configs.
                prometheusYamlMap.put("scrape_configs", scrapeConfigs);

                String updatedPrometheusYaml = YamlUtils.objectToYaml(prometheusYamlMap);
                LOG.info("updated updatedPrometheusYaml: {}", updatedPrometheusYaml);

                prometheusConfigMap.getData().put("prometheus.yml", updatedPrometheusYaml);
                kubernetesClient.configMaps().inNamespace(prometheusNamespace).withName("prometheus-server").createOrReplace(prometheusConfigMap);
                LOG.info("prometheus configmap updated...");
            }



            // replace grafana template.
            kv = new HashMap<>();
            kv.put("customResourceNamespace", Initializer.getNamespace());
            kv.put("storageClass", storageClass);
            String grafanaString =
                    TemplateUtils.replace("/templates/cr/grafana.yaml", true, kv);


            // grafana namespace.
            String grafanaNamespace = CustomResourceUtils.getTargetNamespace(grafanaString);

            // check if grafana already installed.
            boolean grafanaRunning = ContainerStatusChecker.isRunning(
                    kubernetesClient,
                    "grafana",
                    grafanaNamespace,
                    "app.kubernetes.io/instance",
                    "grafana"
            );
            LOG.info("grafana in namespace [{}] is running: {}", grafanaNamespace, grafanaRunning);

            // install grafana.
            if(!grafanaRunning) {
                // build custom resource of grafana.
                CustomResource grafanaCr = CustomResourceUtils.fromYaml(grafanaString);

                // create cr of grafana.
                k8sResourceService.createCustomResource(grafanaCr);
                LOG.info("grafana custom resource created...");

                // wait for that grafana will be run.
                ContainerStatusChecker.checkContainerStatus(
                        kubernetesClient,
                        "grafana",
                        grafanaNamespace,
                        "app.kubernetes.io/instance",
                        "grafana",
                        50
                );
            }



            return ControllerUtils.successMessage();
        });
    }

    private Map<String, Object> makePrometheusJob(String jobName, List<String> endpoints) {
        Map<String, Object> jobMap = new HashMap<>();
        jobMap.put("job_name", jobName);

        List<Map<String, List<String>>> staticConfigs = new ArrayList<>();
        Map<String, List<String>> staticConfig = new HashMap<>();
        staticConfig.put("targets", endpoints);
        staticConfigs.add(staticConfig);

        jobMap.put("static_configs", staticConfigs);

        return jobMap;
    }

    /**
     * TODO:
     * - add catalog.
     * - update catalog configuration.
     * - update config.properties
     * - update jvm.config.
     *
     *
     *
     *
     */




    @DeleteMapping("/v1/trino/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");

            k8sResourceService.deleteCustomResource(name, DEFAULT_TRINO_OPERATOR_NAMESPACE, "TrinoCluster");

            // TODO: remove jobs of the trino cluster jmx exporter endpoints from prometheus configmap.


            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/trino/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {

            List<GenericKubernetesResource> trinoClusters =
                    k8sResourceService.listCustomResources(DEFAULT_TRINO_OPERATOR_NAMESPACE, "TrinoCluster");

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(GenericKubernetesResource genericKubernetesResource : trinoClusters) {
                Map<String, Object> additionalMap = genericKubernetesResource.getAdditionalProperties();
                Map<String, Object> specMap = (Map<String, Object>) additionalMap.get("spec");
                String namespace = (String) specMap.get("namespace");

                Map<String, Object> map = new HashMap<>();
                map.put("name", genericKubernetesResource.getMetadata().getName());
                map.put("namespace", namespace);
                mapList.add(map);
            }

            return JsonUtils.toJson(mapList);
        });
    }
}
