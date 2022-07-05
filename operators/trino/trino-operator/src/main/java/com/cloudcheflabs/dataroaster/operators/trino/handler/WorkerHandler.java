package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.crd.Config;
import com.cloudcheflabs.dataroaster.operators.trino.crd.*;
import com.cloudcheflabs.dataroaster.operators.trino.util.YamlUtils;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscalerBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.cloudcheflabs.dataroaster.operators.trino.config.TrinoConfiguration.*;

public class WorkerHandler {

    private static Logger LOG = LoggerFactory.getLogger(WorkerHandler.class);

    private KubernetesClient client;

    public WorkerHandler(KubernetesClient client) {
        this.client = client;
    }

    public void create(TrinoCluster trinoCluster) {
        TrinoClusterSpec spec = trinoCluster.getSpec();

        String namespace = spec.getNamespace();
        String serviceAccountName = spec.getServiceAccountName();
        Image image = spec.getImage();
        PodSecurityContext securityContext = spec.getSecurityContext();
        Worker worker = spec.getWorker();
        List<Container> initContainers = worker.getInitContainers();

        // create namespace.
        Namespace ns = new NamespaceBuilder().withNewMetadata().withName(namespace).endMetadata().build();
        client.namespaces().createOrReplace(ns);

        // create service account.
        ServiceAccount sa = new ServiceAccountBuilder()
                .withNewMetadata()
                .withName(serviceAccountName).withNamespace(namespace)
                .endMetadata().build();
        client.serviceAccounts().inNamespace(namespace).createOrReplace(sa);

        // construct worker configmap.
        ConfigMapBuilder workerConfigMapBuilder = new ConfigMapBuilder()
                .withNewMetadata()
                .withName(DEFAULT_WORKER_CONFIGMAP)
                .addToLabels("app", "trino-cluster").addToLabels("component", "worker")
                .withNamespace(namespace)
                .endMetadata();

        Map<String, Config> workerConfigsMap = new HashMap<>();
        Map<String, String> workerConfigMapKV = new HashMap<>();
        List<Config> workerConfigs = worker.getConfigs();
        int workerTrinoPort = -1;
        int rmiRegistryPort = -1;
        int rmiPort = -1;
        int jmxExporterPort = -1;
        for(Config config : workerConfigs) {
            String name = config.getName();
            String value = config.getValue();
            workerConfigsMap.put(name, config);
            workerConfigMapKV.put(name, value);

            // get trino container port.
            if(name.equals("config.properties")) {
                Properties prop = new Properties();
                try {
                    prop.load(new ByteArrayInputStream(value.getBytes()));
                    workerTrinoPort = Integer.valueOf(prop.getProperty("http-server.http.port"));
                    String rmiRegistryPortString = prop.getProperty("jmx.rmiregistry.port");
                    rmiRegistryPort = (rmiRegistryPortString != null) ? Integer.valueOf(rmiRegistryPortString) : -1;
                    LOG.info("rmiRegistryPort: {}", rmiRegistryPort);
                    String rmiPortString = prop.getProperty("jmx.rmiserver.port");
                    rmiPort = (rmiPortString != null) ? Integer.valueOf(rmiPortString) : -1;
                    LOG.info("rmiPort: {}", rmiPort);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if(name.equals("jvm.config")) {
                // get jmx export port number from the string line like '-javaagent:/path/jmx_prometheus_javaagent-0.17.0.jar=9090:/path/config.yaml'.
                for(String s : value.lines().collect(Collectors.toList())) {
                    if(s.contains("-javaagent")) {
                        String[] lineTokens = s.split("=");
                        if(lineTokens.length == 2) {
                            String portPart = lineTokens[1];
                            jmxExporterPort = Integer.valueOf(portPart.split(":")[0]);
                            LOG.info("jmxExporterPort: {}", jmxExporterPort);
                        }
                    }
                }
            }
        }

        // create worker configmap.
        workerConfigMapBuilder.withData(workerConfigMapKV);
        ConfigMap retConfigMap = client.configMaps().inNamespace(namespace).createOrReplace(workerConfigMapBuilder.build());
        LOG.info("worker configmap: \n {}", YamlUtils.objectToYaml(retConfigMap));

        // worker volumes.
        List<Volume> workerVolumes = new ArrayList<>();
        Volume workerVolume = new VolumeBuilder()
                .withName("configmap-volume")
                .withNewConfigMap().withName(DEFAULT_WORKER_CONFIGMAP).endConfigMap()
                .build();
        workerVolumes.add(workerVolume);

        // worker volume mounts.
        List<VolumeMount> workerVolumeMounts = new ArrayList<>();
        for(String confName : workerConfigsMap.keySet()) {
            Config config = workerConfigsMap.get(confName);
            String path = config.getPath();

            VolumeMount volumeMount = new VolumeMountBuilder()
                    .withName("configmap-volume")
                    .withMountPath(path + "/" + confName)
                    .withSubPath(confName).build();
            workerVolumeMounts.add(volumeMount);
        }

        // if there are init containers.
        if(initContainers != null && initContainers.size() >0) {
            for(Container container : initContainers) {
                for(VolumeMount volumeMount : container.getVolumeMounts()) {
                    String volumeName = volumeMount.getName();
                    // create emptyDir volume.
                    Volume volumeForInitContainers = new VolumeBuilder()
                            .withName(volumeName)
                            .withEmptyDir(new EmptyDirVolumeSourceBuilder().build())
                            .build();

                    // add volume.
                    workerVolumes.add(volumeForInitContainers);

                    // add volume mount.
                    workerVolumeMounts.add(volumeMount);
                }
            }
        }


        // worker containers.
        Map<String, Quantity> workerResourcesRequestsMap = null;
        if(worker.getResources() != null) {
            Resources.Requests workerResourcesRequests = worker.getResources().getRequests();
            if(workerResourcesRequests != null) {
                workerResourcesRequestsMap = new HashMap<>();
                workerResourcesRequestsMap.put("cpu", new Quantity(workerResourcesRequests.getCpu()));
                workerResourcesRequestsMap.put("memory", new Quantity(workerResourcesRequests.getMemory()));
            }
        }

        Map<String, Quantity> workerResourcesLimitsMap = null;
        if(worker.getResources() != null) {
            Resources.Limits workerResourcesLimits = worker.getResources().getLimits();
            if(workerResourcesLimits != null) {
                workerResourcesLimitsMap = new HashMap<>();
                workerResourcesLimitsMap.put("cpu", new Quantity(workerResourcesLimits.getCpu()));
                workerResourcesLimitsMap.put("memory", new Quantity(workerResourcesLimits.getMemory()));
            }
        }


        List<ContainerPort> containerPorts = new ArrayList<>();

        // worker container ports.
        ContainerPort workerContainerPort = new ContainerPortBuilder()
                .withName("http")
                .withContainerPort(workerTrinoPort)
                .withProtocol("TCP").build();
        containerPorts.add(workerContainerPort);

        // add rmiregistry port.
        if(rmiRegistryPort > 0) {
            ContainerPort rmiRegistryContainerPort = new ContainerPortBuilder()
                    .withName("rmiregistry")
                    .withContainerPort(rmiRegistryPort)
                    .withProtocol("TCP").build();
            containerPorts.add(rmiRegistryContainerPort);
        }

        // add rmi port.
        if(rmiPort > 0) {
            ContainerPort rmiContainerPort = new ContainerPortBuilder()
                    .withName("rmi")
                    .withContainerPort(rmiPort)
                    .withProtocol("TCP").build();
            containerPorts.add(rmiContainerPort);
        }


        // add prometheus jmx exporter port.
        if(jmxExporterPort > 0) {
            ContainerPort jmxExporterContainerPort = new ContainerPortBuilder()
                    .withName("jmxexporter")
                    .withContainerPort(jmxExporterPort)
                    .withProtocol("TCP").build();
            containerPorts.add(jmxExporterContainerPort);
        }



        // worker containers.
        List<Container> workerContainers = new ArrayList<>();
        Container workerContainer = new ContainerBuilder()
                .withName("trino-worker")
                .withImage(image.getRepository() + ":" + image.getTag())
                .withImagePullPolicy(image.getImagePullPolicy())
                .withNewResources()
                .withRequests(workerResourcesRequestsMap)
                .withLimits(workerResourcesLimitsMap)
                .endResources()
                .withVolumeMounts(workerVolumeMounts)
                .withPorts(containerPorts)
                .build();
        workerContainers.add(workerContainer);


        // create worker deployment.
        DeploymentBuilder workerDeploymentBuilder = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(DEFAULT_WORKER_DEPLOYMENT)
                    .withNamespace(namespace)
                    .addToLabels("app", "trino-cluster").addToLabels("component", "worker")
                .endMetadata()
                .withNewSpec()
                    .withReplicas(worker.getReplicas())
                    .withNewSelector()
                        .addToMatchLabels("app", "trino-cluster").addToMatchLabels("component", "worker")
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .addToLabels("app", "trino-cluster").addToLabels("component", "worker")
                        .endMetadata()
                        .withNewSpec()
                            .withServiceAccountName(serviceAccountName)
                            .withSecurityContext(securityContext)
                            .withNodeSelector(worker.getNodeSelector())
                            .withTolerations(worker.getTolerations())
                            .withAffinity(worker.getAffinity())
                            .withImagePullSecrets(image.getImagePullSecrets())
                            .withVolumes(workerVolumes)
                            .withContainers(workerContainers)
                            .withInitContainers(initContainers)
                        .endSpec()
                    .endTemplate()
                .endSpec();

        // create worker deployment.
        Deployment retDeployment = client.apps().deployments().inNamespace(namespace).createOrReplace(workerDeploymentBuilder.build());
        LOG.info("worker deployment: \n{}", YamlUtils.objectToYaml(retDeployment));

        // create horizontal pod autoscaler.
        Autoscaler autoscaler = worker.getAutoscaler();
        if(autoscaler != null) {
            HorizontalPodAutoscaler workerHpa = new HorizontalPodAutoscalerBuilder()
                    .withNewMetadata()
                        .withName(DEFAULT_WORKER_HPA)
                        .withNamespace(namespace)
                    .endMetadata()
                    .withNewSpec()
                        .withMinReplicas(autoscaler.getMinReplicas())
                        .withMaxReplicas(autoscaler.getMaxReplicas())
                        .withTargetCPUUtilizationPercentage(autoscaler.getTargetCPUUtilizationPercentage())
                        .withNewScaleTargetRef()
                            .withName(DEFAULT_WORKER_DEPLOYMENT)
                            .withKind("Deployment")
                            .withApiVersion("apps/v1")
                        .endScaleTargetRef()
                    .endSpec().build();
            HorizontalPodAutoscaler retHpa = client.autoscaling().v1().horizontalPodAutoscalers().inNamespace(namespace).createOrReplace(workerHpa);
            LOG.info("worker hpa: \n{}", YamlUtils.objectToYaml(retHpa));
        }



        // create jmx related headless services.
        Map<String, String> serviceLabelSelectorMap = new HashMap<>();
        serviceLabelSelectorMap.put("app", "trino-cluster");
        serviceLabelSelectorMap.put("component", "worker");

        // create rmiregistry service.
        if(rmiRegistryPort > 0) {
            ServicePort rmiRegistryServicePort = new ServicePortBuilder()
                    .withName("rmiregistry")
                    .withPort(rmiRegistryPort)
                    .withTargetPort(new IntOrString("rmiregistry"))
                    .withProtocol("TCP")
                    .build();

            Service rmiRegistryService = new ServiceBuilder()
                    .withNewMetadata()
                    .withName("trino-worker-rmiregistry-service")
                    .withNamespace(namespace)
                    .addToLabels("app", "trino-cluster").addToLabels("component", "worker")
                    .endMetadata()
                    .withNewSpec()
                    .withType("None")
                    .withPorts(rmiRegistryServicePort)
                    .withSelector(serviceLabelSelectorMap)
                    .endSpec().build();

            // create coordinator rmiregistry service.
            Service retService = client.services().inNamespace(namespace).createOrReplace(rmiRegistryService);
            LOG.info("worker rmiregistry service: \n{}", YamlUtils.objectToYaml(retService));
        }

        // create rmi service.
        if(rmiPort > 0) {
            ServicePort rmiServicePort = new ServicePortBuilder()
                    .withName("rmi")
                    .withPort(rmiPort)
                    .withTargetPort(new IntOrString("rmi"))
                    .withProtocol("TCP")
                    .build();

            Service rmiService = new ServiceBuilder()
                    .withNewMetadata()
                    .withName("trino-worker-rmi-service")
                    .withNamespace(namespace)
                    .addToLabels("app", "trino-cluster").addToLabels("component", "worker")
                    .endMetadata()
                    .withNewSpec()
                    .withType("None")
                    .withPorts(rmiServicePort)
                    .withSelector(serviceLabelSelectorMap)
                    .endSpec().build();

            // create coordinator rmi service.
            Service retService = client.services().inNamespace(namespace).createOrReplace(rmiService);
            LOG.info("worker rmi service: \n{}", YamlUtils.objectToYaml(retService));
        }


        // create prometheus jmx exporter service.
        if(jmxExporterPort > 0) {
            ServicePort jmxExporterServicePort = new ServicePortBuilder()
                    .withName("jmxexporter")
                    .withPort(jmxExporterPort)
                    .withTargetPort(new IntOrString("jmxexporter"))
                    .withProtocol("TCP")
                    .build();

            Service jmxExporterService = new ServiceBuilder()
                    .withNewMetadata()
                    .withName("trino-worker-jmxexporter-service")
                    .withNamespace(namespace)
                    .addToLabels("app", "trino-cluster").addToLabels("component", "worker")
                    .endMetadata()
                    .withNewSpec()
                    .withType("None")
                    .withPorts(jmxExporterServicePort)
                    .withSelector(serviceLabelSelectorMap)
                    .endSpec().build();

            // create coordinator jmxexporter service.
            Service retService = client.services().inNamespace(namespace).createOrReplace(jmxExporterService);
            LOG.info("worker jmxexporter service: \n{}", YamlUtils.objectToYaml(retService));
        }
    }

    public void delete(TrinoCluster trinoCluster) {
        String namespace = trinoCluster.getSpec().getNamespace();

        // delete worker deployment.
        boolean deploymentDeleted = client.apps().deployments().inNamespace(namespace).withName(DEFAULT_WORKER_DEPLOYMENT).delete();
        LOG.info("worker deployment [{}] deleted  in namespace [{}]: {}", DEFAULT_WORKER_DEPLOYMENT, namespace, deploymentDeleted);

        // delete worker config map.
        boolean configmapDeleted = client.configMaps().inNamespace(namespace).withName(DEFAULT_WORKER_CONFIGMAP).delete();
        LOG.info("worker configmap [{}] deleted  in namespace [{}]: {}", DEFAULT_WORKER_CONFIGMAP, namespace, configmapDeleted);

        // delete worker hpa.
        if(trinoCluster.getSpec().getWorker().getAutoscaler() != null) {
            boolean hpaDeleted = client.autoscaling().v1().horizontalPodAutoscalers().inNamespace(namespace).withName(DEFAULT_WORKER_HPA).delete();
            LOG.info("worker hpa [{}] deleted  in namespace [{}]: {}", DEFAULT_WORKER_HPA, namespace, hpaDeleted);
        }
    }
}
