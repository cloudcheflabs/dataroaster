package com.cloudcheflabs.dataroaster.trino.controller.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import com.cloudcheflabs.dataroaster.trino.controller.api.service.ClusterJmxService;
import com.cloudcheflabs.dataroaster.trino.controller.api.service.K8sResourceService;
import com.cloudcheflabs.dataroaster.trino.controller.component.Initializer;
import com.cloudcheflabs.dataroaster.trino.controller.domain.CustomResource;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import com.cloudcheflabs.dataroaster.trino.controller.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@RestController
public class TrinoController {

    private static Logger LOG = LoggerFactory.getLogger(TrinoController.class);

    private ObjectMapper mapper = new ObjectMapper();

    public static final String DEFAULT_TRINO_OPERATOR_NAMESPACE = "trino-operator";
    public static final String DEFAULT_TRINO_IMAGE_REPO = "trinodb/trino";
    public static final String DEFAULT_TRINO_IMAGE_TAG = "384";

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
            // optional.
            String trinoImage = params.get("trino_image");

            String trinoImageRepo = DEFAULT_TRINO_IMAGE_REPO;
            String trinoImageTag = DEFAULT_TRINO_IMAGE_TAG;
            if(trinoImage != null) {
                String[] tokens = trinoImage.split(":");
                trinoImageRepo = tokens[0];
                trinoImageTag = tokens[1];
            }
            LOG.info("trinoImageRepo: {}", trinoImageRepo);
            LOG.info("trinoImageTag: {}", trinoImageTag);


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
            kv.put("trinoImageRepo", trinoImageRepo);
            kv.put("trinoImageTag", trinoImageTag);
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
                LOG.info("trino cluster in namespace [" + trinoClusterNamespace + "] already running.");
                return ControllerUtils.successMessage();
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

    private String getPrometheusNamespace() {
        Map<String, Object> kv = new HashMap<>();
        kv.put("customResourceNamespace", Initializer.getNamespace());
        kv.put("storageClass", "");
        String prometheusString =
                TemplateUtils.replace("/templates/cr/prometheus.yaml", true, kv);


        // prometheus namespace.
        return CustomResourceUtils.getTargetNamespace(prometheusString);
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


    @DeleteMapping("/v1/trino/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");

            k8sResourceService.deleteCustomResource(name, DEFAULT_TRINO_OPERATOR_NAMESPACE, "TrinoCluster");

            String coordinatorJobName = name + "-coordinator";
            String workersJobName = name + "-workers";

            // replace prometheus template.
            Map<String, Object> kv = new HashMap<>();
            kv.put("customResourceNamespace", Initializer.getNamespace());
            kv.put("storageClass", "anysc");
            String prometheusString =
                    TemplateUtils.replace("/templates/cr/prometheus.yaml", true, kv);

            // prometheus namespace.
            String prometheusNamespace = CustomResourceUtils.getTargetNamespace(prometheusString);

            // remove jobs of the trino cluster jmx exporter endpoints from prometheus configmap.
            // get prometheus configmap.
            ConfigMap prometheusConfigMap = kubernetesClient.configMaps().inNamespace(prometheusNamespace).withName("prometheus-server").get();
            Map<String, String> dataMap = prometheusConfigMap.getData();
            String prometheusYaml = dataMap.get("prometheus.yml");
            LOG.info("prometheusYaml: {}", prometheusYaml);

            Map<String, Object> prometheusYamlMap = YamlUtils.yamlToMap(prometheusYaml);
            List<Map<String, Object>> scrapeConfigs = (List<Map<String, Object>>) prometheusYamlMap.get("scrape_configs");
            LOG.info("scapeConfigs: {}", JsonUtils.toJson(scrapeConfigs));

            // create new scrape configs.
            List<Map<String, Object>> scrapeConfigsArrayList = new ArrayList<>();
            for(Map<String, Object> scrapeConfig : scrapeConfigs) {
                boolean candidated = false;
                if(scrapeConfig.containsKey("job_name")) {
                    String jobName = (String) scrapeConfig.get("job_name");
                    if(jobName.equals(coordinatorJobName) || jobName.equals(workersJobName)) {
                        // remove jobs.
                        LOG.info("job removed: \n{}", YamlUtils.objectToYaml(scrapeConfig));
                        candidated = true;
                    }
                }
                if(!candidated) {
                    scrapeConfigsArrayList.add(scrapeConfig);
                }
            }

            // update scrape configs.
            prometheusYamlMap.put("scrape_configs", scrapeConfigsArrayList);

            String updatedPrometheusYaml = YamlUtils.objectToYaml(prometheusYamlMap);
            LOG.info("updated updatedPrometheusYaml: {}", updatedPrometheusYaml);

            prometheusConfigMap.getData().put("prometheus.yml", updatedPrometheusYaml);
            kubernetesClient.configMaps().inNamespace(prometheusNamespace).withName("prometheus-server").createOrReplace(prometheusConfigMap);
            LOG.info("prometheus configmap updated...");


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



    @PostMapping("/v1/trino/config/create")
    public String createConfig(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");
            String coordinatorConfigName = params.get("coordinator_config_name");
            String coordinatorConfigPath = params.get("coordinator_config_path");
            String coordinatorConfigValue = params.get("coordinator_config_value");
            String workerConfigName = params.get("worker_config_name");
            String workerConfigPath = params.get("worker_config_path");
            String workerConfigValue = params.get("worker_config_value");

            Map<String, Object> coordinatorConfigMap = null;
            if(coordinatorConfigName != null && coordinatorConfigPath != null && coordinatorConfigValue != null) {
                // decode value with base64.
                coordinatorConfigValue = Base64Utils.decodeBase64(coordinatorConfigValue);

                coordinatorConfigMap = new HashMap<>();
                coordinatorConfigMap.put("name", coordinatorConfigName);
                coordinatorConfigMap.put("path", coordinatorConfigPath);
                coordinatorConfigMap.put("value", coordinatorConfigValue);
            } else {
                throw new IllegalArgumentException("coordinator config params NULL not allowed.");
            }

            Map<String, Object> workerConfigMap = null;
            if(workerConfigName != null && workerConfigPath != null && workerConfigValue != null) {
                // decode value with base64.
                workerConfigValue = Base64Utils.decodeBase64(workerConfigValue);

                workerConfigMap = new HashMap<>();
                workerConfigMap.put("name", workerConfigName);
                workerConfigMap.put("path", workerConfigPath);
                workerConfigMap.put("value", workerConfigValue);
            } else {
                throw new IllegalArgumentException("worker config params NULL not allowed.");
            }


            List<GenericKubernetesResource> trinoClusters =
                    k8sResourceService.listCustomResources(DEFAULT_TRINO_OPERATOR_NAMESPACE, "TrinoCluster");

            for (GenericKubernetesResource genericKubernetesResource : trinoClusters) {
                String clusterName = genericKubernetesResource.getMetadata().getName();
                if(clusterName.equals(name)) {
                    Map<String, Object> additionalMap = genericKubernetesResource.getAdditionalProperties();
                    Map<String, Object> specMap = (Map<String, Object>) additionalMap.get("spec");
                    Map<String, Object> coordinatorMap = (Map<String, Object>) specMap.get("coordinator");
                    List<Map<String, Object>> coordinatorConfigs = (List<Map<String, Object>>) coordinatorMap.get("configs");
                    Map<String, Object> workerMap = (Map<String, Object>) specMap.get("worker");
                    List<Map<String, Object>> workerConfigs = (List<Map<String, Object>>) workerMap.get("configs");

                    boolean coordinatorConfigExists = false;
                    for(Map<String, Object> coordinatorConfig : coordinatorConfigs) {
                        if(coordinatorConfig.get("name").equals(coordinatorConfigName) &&
                                coordinatorConfig.get("path").equals(coordinatorConfigPath)) {
                            coordinatorConfigExists = true;
                            break;
                        }
                    }

                    // if not such config with the same name and path exists.
                    if(!coordinatorConfigExists) {
                        coordinatorConfigs.add(coordinatorConfigMap);
                    }

                    boolean workerConfigExists = false;
                    for(Map<String, Object> workerConfig : workerConfigs) {
                        if(workerConfig.get("name").equals(workerConfigName) &&
                                workerConfig.get("path").equals(workerConfigPath)) {
                            workerConfigExists = true;
                            break;
                        }
                    }
                    // if not such config with the same name and path exists.
                    if(!workerConfigExists) {
                        workerConfigs.add(workerConfigMap);
                    }
                    LOG.info("updated generic custom resource: \n{}", YamlUtils.objectToYaml(genericKubernetesResource));

                    k8sResourceService.updateCustomResource(genericKubernetesResource);
                    LOG.info("cluster [{}] configs updated.", name);

                    LOG.info("waiting for update trinocluster custom resource...");
                    PauseUtils.pause(30000);

                    String clusterNamespace = (String) specMap.get("namespace");
                    rolloutDeployment(clusterNamespace);

                    // wait for coordinator and workers restarted.
                    LOG.info("waiting for coordinator and workers restarted...");
                    PauseUtils.pause(30000);

                    // update prometheus configmap to update prometheus jobs.
                    updatePrometheusConfigMap(name, clusterNamespace);

                    break;
                }
            }

            return ControllerUtils.successMessage();
        });
    }

    private void rolloutDeployment(String clusterNamespace) {
        kubernetesClient.apps().deployments().inNamespace(clusterNamespace).withName("trino-coordinator").rolling().restart();
        LOG.info("deployment [{}] in namespace [{}] rollout restarted...", "trino-coordinator", clusterNamespace);

        kubernetesClient.apps().deployments().inNamespace(clusterNamespace).withName("trino-worker").rolling().restart();
        LOG.info("deployment [{}] in namespace [{}] rollout restarted...", "trino-worker", clusterNamespace);
    }



    @PutMapping("/v1/trino/config/update")
    public String updateConfig(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");
            String coordinatorConfigName = params.get("coordinator_config_name");
            String coordinatorConfigPath = params.get("coordinator_config_path");
            String coordinatorConfigValue = params.get("coordinator_config_value");
            String workerConfigName = params.get("worker_config_name");
            String workerConfigPath = params.get("worker_config_path");
            String workerConfigValue = params.get("worker_config_value");

            Map<String, Object> coordinatorConfigMap = null;
            if(coordinatorConfigName != null && coordinatorConfigPath != null && coordinatorConfigValue != null) {
                // decode value with base64.
                coordinatorConfigValue = Base64Utils.decodeBase64(coordinatorConfigValue);

                coordinatorConfigMap = new HashMap<>();
                coordinatorConfigMap.put("name", coordinatorConfigName);
                coordinatorConfigMap.put("path", coordinatorConfigPath);
                coordinatorConfigMap.put("value", coordinatorConfigValue);
            } else {
                throw new IllegalArgumentException("coordinator config params NULL not allowed.");
            }

            Map<String, Object> workerConfigMap = null;
            if(workerConfigName != null && workerConfigPath != null && workerConfigValue != null) {
                // decode value with base64.
                workerConfigValue = Base64Utils.decodeBase64(workerConfigValue);

                workerConfigMap = new HashMap<>();
                workerConfigMap.put("name", workerConfigName);
                workerConfigMap.put("path", workerConfigPath);
                workerConfigMap.put("value", workerConfigValue);
            } else {
                throw new IllegalArgumentException("worker config params NULL not allowed.");
            }


            List<GenericKubernetesResource> trinoClusters =
                    k8sResourceService.listCustomResources(DEFAULT_TRINO_OPERATOR_NAMESPACE, "TrinoCluster");

            for (GenericKubernetesResource genericKubernetesResource : trinoClusters) {
                String clusterName = genericKubernetesResource.getMetadata().getName();
                if(clusterName.equals(name)) {
                    Map<String, Object> additionalMap = genericKubernetesResource.getAdditionalProperties();
                    Map<String, Object> specMap = (Map<String, Object>) additionalMap.get("spec");
                    Map<String, Object> coordinatorMap = (Map<String, Object>) specMap.get("coordinator");
                    List<Map<String, Object>> coordinatorConfigs = (List<Map<String, Object>>) coordinatorMap.get("configs");
                    Map<String, Object> workerMap = (Map<String, Object>) specMap.get("worker");
                    List<Map<String, Object>> workerConfigs = (List<Map<String, Object>>) workerMap.get("configs");

                    for(Map<String, Object> coordinatorConfig : coordinatorConfigs) {
                        if(coordinatorConfig.get("name").equals(coordinatorConfigName) &&
                                coordinatorConfig.get("path").equals(coordinatorConfigPath)) {
                            coordinatorConfig.put("value", coordinatorConfigValue);
                            LOG.info("coordinator config value updated: name [{}], path [{}]", coordinatorConfigName, coordinatorConfigPath);
                            break;
                        }
                    }

                    for(Map<String, Object> workerConfig : workerConfigs) {
                        if(workerConfig.get("name").equals(workerConfigName) &&
                                workerConfig.get("path").equals(workerConfigPath)) {
                            workerConfig.put("value", workerConfigValue);
                            LOG.info("worker config value updated: name [{}], path [{}]", workerConfigName, workerConfigPath);
                            break;
                        }
                    }
                    LOG.info("updated generic custom resource: \n{}", YamlUtils.objectToYaml(genericKubernetesResource));

                    k8sResourceService.updateCustomResource(genericKubernetesResource);
                    LOG.info("cluster [{}] configs updated.", name);

                    LOG.info("waiting for update trinocluster custom resource...");
                    PauseUtils.pause(30000);

                    String clusterNamespace = (String) specMap.get("namespace");
                    rolloutDeployment(clusterNamespace);

                    // wait for coordinator and workers restarted.
                    LOG.info("waiting for coordinator and workers restarted...");
                    PauseUtils.pause(30000);

                    // update prometheus configmap to update prometheus jobs.
                    updatePrometheusConfigMap(name, clusterNamespace);

                    break;
                }
            }

            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/v1/trino/config/delete")
    public String deleteConfig(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");
            String coordinatorConfigName = params.get("coordinator_config_name");
            String coordinatorConfigPath = params.get("coordinator_config_path");
            String workerConfigName = params.get("worker_config_name");
            String workerConfigPath = params.get("worker_config_path");

            if(coordinatorConfigName == null || coordinatorConfigPath == null) {
                throw new IllegalArgumentException("coordinator config params NULL not allowed.");
            }

            if(workerConfigName == null || workerConfigPath == null) {
                throw new IllegalArgumentException("worker config params NULL not allowed.");
            }

            List<GenericKubernetesResource> trinoClusters =
                    k8sResourceService.listCustomResources(DEFAULT_TRINO_OPERATOR_NAMESPACE, "TrinoCluster");

            for (GenericKubernetesResource genericKubernetesResource : trinoClusters) {
                String clusterName = genericKubernetesResource.getMetadata().getName();
                if(clusterName.equals(name)) {
                    Map<String, Object> additionalMap = genericKubernetesResource.getAdditionalProperties();
                    Map<String, Object> specMap = (Map<String, Object>) additionalMap.get("spec");
                    Map<String, Object> coordinatorMap = (Map<String, Object>) specMap.get("coordinator");
                    List<Map<String, Object>> coordinatorConfigs = (List<Map<String, Object>>) coordinatorMap.get("configs");
                    Map<String, Object> workerMap = (Map<String, Object>) specMap.get("worker");
                    List<Map<String, Object>> workerConfigs = (List<Map<String, Object>>) workerMap.get("configs");

                    List<Map<String, Object>> coordinatorConfigsArrayList = new ArrayList<>();
                    for(Map<String, Object> coordinatorConfig : coordinatorConfigs) {
                        if(coordinatorConfig.get("name").equals(coordinatorConfigName) &&
                                coordinatorConfig.get("path").equals(coordinatorConfigPath)) {
                            LOG.info("coordinator config removed: name [{}], path [{}]", coordinatorConfigName, coordinatorConfigPath);
                        } else {
                            coordinatorConfigsArrayList.add(coordinatorConfig);
                        }
                    }
                    // update coordinator configs.
                    coordinatorMap.put("configs", coordinatorConfigsArrayList);


                    List<Map<String, Object>> workerConfigsArrayList = new ArrayList<>();
                    for(Map<String, Object> workerConfig : workerConfigs) {
                        if(workerConfig.get("name").equals(workerConfigName) &&
                                workerConfig.get("path").equals(workerConfigPath)) {
                            LOG.info("worker config removed: name [{}], path [{}]", workerConfigName, workerConfigPath);
                        } else {
                            workerConfigsArrayList.add(workerConfig);
                        }
                    }
                    // update worker configs.
                    workerMap.put("configs", workerConfigsArrayList);

                    LOG.info("updated generic custom resource: \n{}", YamlUtils.objectToYaml(genericKubernetesResource));

                    k8sResourceService.updateCustomResource(genericKubernetesResource);
                    LOG.info("cluster [{}] configs updated.", name);

                    String clusterNamespace = (String) specMap.get("namespace");
                    rolloutDeployment(clusterNamespace);

                    // wait for coordinator and workers restarted.
                    LOG.info("waiting for coordinator and workers restarted...");
                    PauseUtils.pause(30000);

                    // update prometheus configmap to update prometheus jobs.
                    updatePrometheusConfigMap(name, clusterNamespace);

                    break;
                }
            }

            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/trino/config/list")
    public String listClusterConfigs(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {

            List<GenericKubernetesResource> trinoClusters =
                    k8sResourceService.listCustomResources(DEFAULT_TRINO_OPERATOR_NAMESPACE, "TrinoCluster");

            List<Map<String, Object>> mapList = new ArrayList<>();
            for (GenericKubernetesResource genericKubernetesResource : trinoClusters) {
                Map<String, Object> additionalMap = genericKubernetesResource.getAdditionalProperties();
                Map<String, Object> specMap = (Map<String, Object>) additionalMap.get("spec");
                Map<String, Object> coordinatorMap = (Map<String, Object>) specMap.get("coordinator");
                List<Map<String, Object>> coordinatorConfigs = (List<Map<String, Object>>) coordinatorMap.get("configs");
                Map<String, Object> workerMap = (Map<String, Object>) specMap.get("worker");
                List<Map<String, Object>> workerConfigs = (List<Map<String, Object>>) workerMap.get("configs");

                Map<String, Object> map = new HashMap<>();
                map.put("name", genericKubernetesResource.getMetadata().getName());
                map.put("coordinator", coordinatorConfigs);
                map.put("worker", workerConfigs);
                mapList.add(map);
            }

            return JsonUtils.toJson(mapList);
        });
    }

    @PutMapping("/v1/trino/config/memory-properties/update")
    public String updateMemoryConfig(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");
            String memoryProperties = params.get("memory_properties");

            if(name == null) {
                throw new IllegalArgumentException("cluster name param NULL not allowed.");
            }

            if(memoryProperties == null) {
                throw new IllegalArgumentException("memory properties param NULL not allowed.");
            }
            memoryProperties = Base64Utils.decodeBase64(memoryProperties);
            LOG.info("decoded memory properties: \n{}", memoryProperties);
            Properties memoryPropObj = new Properties();
            memoryPropObj.load(new ByteArrayInputStream(memoryProperties.getBytes()));


            List<GenericKubernetesResource> trinoClusters =
                    k8sResourceService.listCustomResources(DEFAULT_TRINO_OPERATOR_NAMESPACE, "TrinoCluster");

            for (GenericKubernetesResource genericKubernetesResource : trinoClusters) {
                String clusterName = genericKubernetesResource.getMetadata().getName();
                if(clusterName.equals(name)) {
                    Map<String, Object> additionalMap = genericKubernetesResource.getAdditionalProperties();
                    Map<String, Object> specMap = (Map<String, Object>) additionalMap.get("spec");
                    Map<String, Object> coordinatorMap = (Map<String, Object>) specMap.get("coordinator");
                    List<Map<String, Object>> coordinatorConfigs = (List<Map<String, Object>>) coordinatorMap.get("configs");
                    Map<String, Object> workerMap = (Map<String, Object>) specMap.get("worker");
                    List<Map<String, Object>> workerConfigs = (List<Map<String, Object>>) workerMap.get("configs");

                    for(Map<String, Object> coordinatorConfig : coordinatorConfigs) {
                        if(coordinatorConfig.get("name").equals("config.properties") &&
                                coordinatorConfig.get("path").equals("/etc/trino")) {

                            String configProperties = (String) coordinatorConfig.get("value");
                            String changedConfigProperties = updateConfigProperties(memoryPropObj, configProperties);
                            LOG.info("changed config properties: \n{}", changedConfigProperties);

                            // update config properties.
                            coordinatorConfig.put("value", changedConfigProperties);
                            break;
                        }
                    }

                    for(Map<String, Object> workerConfig : workerConfigs) {
                        if(workerConfig.get("name").equals("config.properties") &&
                                workerConfig.get("path").equals("/etc/trino")) {
                            String configProperties = (String) workerConfig.get("value");
                            String changedConfigProperties = updateConfigProperties(memoryPropObj, configProperties);
                            LOG.info("changed config properties: \n{}", changedConfigProperties);

                            // update config properties.
                            workerConfig.put("value", changedConfigProperties);
                            break;
                        }
                    }
                    LOG.info("updated generic custom resource: \n{}", YamlUtils.objectToYaml(genericKubernetesResource));

                    k8sResourceService.updateCustomResource(genericKubernetesResource);
                    LOG.info("cluster [{}] configs updated.", name);

                    LOG.info("waiting for update trinocluster custom resource...");
                    PauseUtils.pause(30000);

                    String clusterNamespace = (String) specMap.get("namespace");
                    rolloutDeployment(clusterNamespace);

                    // wait for coordinator and workers restarted.
                    LOG.info("waiting for coordinator and workers restarted...");
                    PauseUtils.pause(30000);

                    // update prometheus configmap to update prometheus jobs.
                    updatePrometheusConfigMap(name, clusterNamespace);

                    break;
                }
            }

            return ControllerUtils.successMessage();
        });
    }

    private String updateConfigProperties(Properties memoryPropObj, String configProperties) throws IOException {
        Properties configPropObj = new Properties();
        configPropObj.load(new ByteArrayInputStream(configProperties.getBytes()));

        for(Object keyObj : memoryPropObj.keySet()) {
            if(configPropObj.containsKey(keyObj)) {
                configPropObj.put(keyObj, memoryPropObj.get(keyObj));
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        configPropObj.store(out, null);
        return new String(out.toByteArray());
    }

    private void updatePrometheusConfigMap(String name, String trinoClusterNamespace) {
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

        // prometheus namespace.
        String prometheusNamespace = getPrometheusNamespace();

        // add jobs of jmx exporter endpoints of trino coordinator and workers to configmap in prometheus to monitor trino cluster.
        if(coordinatorJmxExporterEndpoint != null && workerJmxExporterEndpoints != null) {
            String coordinatorJobName = name + "-coordinator";
            String workerJobName = name + "-workers";
            Map<String, Object> coordinatorJobMap = makePrometheusJob(coordinatorJobName, Arrays.asList(coordinatorJmxExporterEndpoint));
            Map<String, Object> workerJobMap = makePrometheusJob(workerJobName, workerJmxExporterEndpoints);

            // get prometheus configmap.
            ConfigMap prometheusConfigMap = kubernetesClient.configMaps().inNamespace(prometheusNamespace).withName("prometheus-server").get();
            Map<String, String> dataMap = prometheusConfigMap.getData();
            String prometheusYaml = dataMap.get("prometheus.yml");
            LOG.info("prometheusYaml: {}", prometheusYaml);

            Map<String, Object> prometheusYamlMap = YamlUtils.yamlToMap(prometheusYaml);
            List<Map<String, Object>> scrapeConfigs = (List<Map<String, Object>>) prometheusYamlMap.get("scrape_configs");
            LOG.info("scapeConfigs: {}", JsonUtils.toJson(scrapeConfigs));

            List<Map<String, Object>> scrapeConfigsArrayList = new ArrayList<>();
            for(Map<String, Object> scrapeConfig : scrapeConfigs) {
                if(scrapeConfig.containsKey("job_name")) {
                    String jobName = (String) scrapeConfig.get("job_name");
                    // update coordinator job map.
                    if(jobName.equals(coordinatorJobName)) {
                        scrapeConfigsArrayList.add(coordinatorJobMap);
                    }
                    // update worker job map.
                    else if(jobName.equals(workerJobName)) {
                        scrapeConfigsArrayList.add(workerJobMap);
                    } else {
                        scrapeConfigsArrayList.add(scrapeConfig);
                    }
                } else {
                    scrapeConfigsArrayList.add(scrapeConfig);
                }
            }

            // update scrape configs.
            prometheusYamlMap.put("scrape_configs", scrapeConfigsArrayList);

            String updatedPrometheusYaml = YamlUtils.objectToYaml(prometheusYamlMap);
            LOG.info("updated updatedPrometheusYaml: {}", updatedPrometheusYaml);

            prometheusConfigMap.getData().put("prometheus.yml", updatedPrometheusYaml);
            kubernetesClient.configMaps().inNamespace(prometheusNamespace).withName("prometheus-server").createOrReplace(prometheusConfigMap);
            LOG.info("prometheus configmap updated...");
        }
    }



    @PutMapping("/v1/trino/pod-template/update")
    public String updatePodTemplate(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String name = params.get("name");
            String coordinatorPodTemplate = params.get("coordinator_pod_template");
            String workerPodTemplate = params.get("worker_pod_template");
            String imageCacheRepoPrefix = params.get("image_cache_repo_prefix");
            String imagePullSecret = params.get("image_pull_secret");

            if(coordinatorPodTemplate == null) {
                throw new RuntimeException("coordinator pod template is null!");
            }
            if(workerPodTemplate == null) {
                throw new RuntimeException("worker pod template is null!");
            }

            coordinatorPodTemplate = Base64Utils.decodeBase64(coordinatorPodTemplate);
            LOG.info("coordinatorPodTemplate: {}", coordinatorPodTemplate);

            workerPodTemplate = Base64Utils.decodeBase64(workerPodTemplate);
            LOG.info("workerPodTemplate: {}", workerPodTemplate);

            // convert yaml to map.
            Map<String, Object> coordinatorPodTemplateMap = YamlUtils.yamlToMap(coordinatorPodTemplate);
            Map<String, Object> workerPodTemplateMap = YamlUtils.yamlToMap(workerPodTemplate);


            List<GenericKubernetesResource> trinoClusters =
                    k8sResourceService.listCustomResources(DEFAULT_TRINO_OPERATOR_NAMESPACE, "TrinoCluster");

            for (GenericKubernetesResource genericKubernetesResource : trinoClusters) {
                String clusterName = genericKubernetesResource.getMetadata().getName();
                if(clusterName.equals(name)) {
                    Map<String, Object> additionalMap = genericKubernetesResource.getAdditionalProperties();
                    Map<String, Object> specMap = (Map<String, Object>) additionalMap.get("spec");

                    Map<String, Object> imageMap = (Map<String, Object>) specMap.get("image");
                    if(imageCacheRepoPrefix != null) {
                        // add image cache server prefix like harbor server to repository.
                        String repository = (String) imageMap.get("repository");
                        if(!repository.startsWith(imageCacheRepoPrefix)) {
                            LOG.info("original repository: {}", repository);
                            repository = imageCacheRepoPrefix + "/" + repository;
                            LOG.info("new repository: {}", repository);
                            imageMap.put("repository", repository);
                        }
                    }
                    // add image pull secret to pull image from harbor, for instance.
                    if(imagePullSecret != null) {
                        List<String> imagePullSecretList = new ArrayList<>();
                        imagePullSecretList.add(imagePullSecret);
                        imageMap.put("imagePullSecrets", imagePullSecretList);
                    }

                    // add coordinator pod template.
                    Map<String, Object> coordinatorMap = (Map<String, Object>) specMap.get("coordinator");
                    if(imageCacheRepoPrefix != null) {
                        List<Map<String, Object>> initContainers = (List<Map<String, Object>>) coordinatorMap.get("initContainers");
                        for(Map<String, Object> initContainer : initContainers) {
                            String image = (String) initContainer.get("image");
                            if(!image.startsWith(imageCacheRepoPrefix)) {
                                LOG.info("original init container image for coordinator: {}", image);
                                image = imageCacheRepoPrefix + "/" + image;
                                LOG.info("new init container image for coordinator: {}", image);
                                initContainer.put("image", image);
                            }
                        }
                    }

                    if(coordinatorPodTemplateMap.containsKey("resources")) {
                        coordinatorMap.put("resources", coordinatorPodTemplateMap.get("resources"));
                    }
                    if(coordinatorPodTemplateMap.containsKey("nodeSelector")) {
                        coordinatorMap.put("nodeSelector", coordinatorPodTemplateMap.get("nodeSelector"));
                    }
                    if(coordinatorPodTemplateMap.containsKey("affinity")) {
                        coordinatorMap.put("affinity", coordinatorPodTemplateMap.get("affinity"));
                    }
                    if(coordinatorPodTemplateMap.containsKey("tolerations")) {
                        coordinatorMap.put("tolerations", coordinatorPodTemplateMap.get("tolerations"));
                    }

                    // add worker pod template.
                    Map<String, Object> workerMap = (Map<String, Object>) specMap.get("worker");
                    if(imageCacheRepoPrefix != null) {
                        List<Map<String, Object>> initContainers = (List<Map<String, Object>>) workerMap.get("initContainers");
                        for(Map<String, Object> initContainer : initContainers) {
                            String image = (String) initContainer.get("image");
                            if(!image.startsWith(imageCacheRepoPrefix)) {
                                LOG.info("original init container image for worker: {}", image);
                                image = imageCacheRepoPrefix + "/" + image;
                                LOG.info("new init container image for worker: {}", image);
                                initContainer.put("image", image);
                            }
                        }
                    }
                    if(workerPodTemplateMap.containsKey("resources")) {
                        workerMap.put("resources", workerPodTemplateMap.get("resources"));
                    }
                    if(workerPodTemplateMap.containsKey("nodeSelector")) {
                        workerMap.put("nodeSelector", workerPodTemplateMap.get("nodeSelector"));
                    }
                    if(workerPodTemplateMap.containsKey("affinity")) {
                        workerMap.put("affinity", workerPodTemplateMap.get("affinity"));
                    }
                    if(workerPodTemplateMap.containsKey("tolerations")) {
                        workerMap.put("tolerations", workerPodTemplateMap.get("tolerations"));
                    }

                    LOG.info("updated generic custom resource: \n{}", YamlUtils.objectToYaml(genericKubernetesResource));

                    k8sResourceService.updateCustomResource(genericKubernetesResource);
                    LOG.info("cluster [{}] configs updated.", name);

                    String clusterNamespace = (String) specMap.get("namespace");
                    rolloutDeployment(clusterNamespace);

                    // wait for coordinator and workers restarted.
                    LOG.info("waiting for coordinator and workers restarted...");
                    PauseUtils.pause(20000);

                    // update prometheus configmap to update prometheus jobs.
                    updatePrometheusConfigMap(name, clusterNamespace);

                    break;
                }
            }

            return ControllerUtils.successMessage();
        });
    }
}
