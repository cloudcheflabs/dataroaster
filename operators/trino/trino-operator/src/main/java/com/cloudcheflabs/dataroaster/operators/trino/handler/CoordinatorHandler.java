package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.crd.Config;
import com.cloudcheflabs.dataroaster.operators.trino.crd.*;
import com.cloudcheflabs.dataroaster.operators.trino.util.YamlUtils;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.cloudcheflabs.dataroaster.operators.trino.config.TrinoConfiguration.*;

public class CoordinatorHandler {

    private static Logger LOG = LoggerFactory.getLogger(CoordinatorHandler.class);

    private KubernetesClient client;

    public CoordinatorHandler(KubernetesClient client) {
        this.client = client;
    }

    public void create(TrinoCluster trinoCluster) {
        TrinoClusterSpec spec = trinoCluster.getSpec();

        String namespace = spec.getNamespace();
        String serviceAccountName = spec.getServiceAccountName();
        Image image = spec.getImage();
        PodSecurityContext securityContext = spec.getSecurityContext();
        Coordinator coordinator = spec.getCoordinator();
        List<Container> initContainers = coordinator.getInitContainers();

        // create namespace.
        Namespace ns = new NamespaceBuilder().withNewMetadata().withName(namespace).endMetadata().build();
        client.namespaces().createOrReplace(ns);

        // create service account.
        ServiceAccount sa = new ServiceAccountBuilder()
                .withNewMetadata()
                .withName(serviceAccountName).withNamespace(namespace)
                .endMetadata().build();
        client.serviceAccounts().inNamespace(namespace).createOrReplace(sa);

        // construct coordinator configmap.
        ConfigMapBuilder coordinatorConfigMapBuilder = new ConfigMapBuilder()
                .withNewMetadata()
                .withName(DEFAULT_COORDINATOR_CONFIGMAP)
                .addToLabels("app", "trino-cluster").addToLabels("component", "coordinator")
                .withNamespace(namespace)
                .endMetadata();

        Map<String, Config> coordinatorConfigsMap = new HashMap<>();
        Map<String, String> coordinatorConfigMapKV = new HashMap<>();
        List<Config> coordinatorConfigs = coordinator.getConfigs();
        int coordinatorTrinoPort = -1;
        int rmiRegistryPort = -1;
        int rmiPort = -1;
        int jmxExporterPort = -1;
        for(Config config : coordinatorConfigs) {
            String name = config.getName();
            String value = config.getValue();
            coordinatorConfigsMap.put(name, config);
            coordinatorConfigMapKV.put(name, value);

            // get trino container port.
            if(name.equals("config.properties")) {
                Properties prop = new Properties();
                try {
                    prop.load(new ByteArrayInputStream(value.getBytes()));
                    coordinatorTrinoPort = Integer.valueOf(prop.getProperty("http-server.http.port"));
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

        // create coordinator configmap.
        coordinatorConfigMapBuilder.withData(coordinatorConfigMapKV);
        ConfigMap retConfigMap = client.configMaps().inNamespace(namespace).createOrReplace(coordinatorConfigMapBuilder.build());
        LOG.info("coordinator configmap: \n {}", YamlUtils.objectToYaml(retConfigMap));

        // coordinator volumes.
        List<Volume> coordinatorVolumes = new ArrayList<>();
        Volume coordinatorVolume = new VolumeBuilder()
                .withName("configmap-volume")
                .withNewConfigMap().withName(DEFAULT_COORDINATOR_CONFIGMAP).endConfigMap()
                .build();
        coordinatorVolumes.add(coordinatorVolume);

        // coordinator volume mounts.
        List<VolumeMount> coordinatorVolumeMounts = new ArrayList<>();
        for(String confName : coordinatorConfigsMap.keySet()) {
            Config config = coordinatorConfigsMap.get(confName);
            String path = config.getPath();

            VolumeMount volumeMount = new VolumeMountBuilder()
                    .withName("configmap-volume")
                    .withMountPath(path + "/" + confName)
                    .withSubPath(confName).build();
            coordinatorVolumeMounts.add(volumeMount);
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
                    coordinatorVolumes.add(volumeForInitContainers);

                    // add volume mount.
                    coordinatorVolumeMounts.add(volumeMount);
                }
            }
        }


        // coordinator containers.
        Map<String, Quantity> coordinatorResourcesRequestsMap = null;
        if(coordinator.getResources() != null) {
            Resources.Requests coordinatorResourcesRequests = coordinator.getResources().getRequests();
            if(coordinatorResourcesRequests != null) {
                coordinatorResourcesRequestsMap = new HashMap<>();
                coordinatorResourcesRequestsMap.put("cpu", new Quantity(coordinatorResourcesRequests.getCpu()));
                coordinatorResourcesRequestsMap.put("memory", new Quantity(coordinatorResourcesRequests.getMemory()));
            }
        }

        Map<String, Quantity> coordinatorResourcesLimitsMap = null;
        if(coordinator.getResources() != null) {
            Resources.Limits coordinatorResourcesLimits = coordinator.getResources().getLimits();
            if(coordinatorResourcesLimits != null) {
                coordinatorResourcesLimitsMap = new HashMap<>();
                coordinatorResourcesLimitsMap.put("cpu", new Quantity(coordinatorResourcesLimits.getCpu()));
                coordinatorResourcesLimitsMap.put("memory", new Quantity(coordinatorResourcesLimits.getMemory()));
            }
        }

        List<ContainerPort> containerPorts = new ArrayList<>();

        // coordinator container ports.
        ContainerPort coordinatorContainerPort = new ContainerPortBuilder()
                .withName("http")
                .withContainerPort(coordinatorTrinoPort)
                .withProtocol("TCP").build();
        containerPorts.add(coordinatorContainerPort);

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




        // coordinator containers.
        List<Container> coordinatorContainers = new ArrayList<>();
        Container coordinatorContainer = new ContainerBuilder()
                .withName("trino-coordinator")
                .withImage(image.getRepository() + ":" + image.getTag())
                .withImagePullPolicy(image.getImagePullPolicy())
                .withNewResources()
                .withRequests(coordinatorResourcesRequestsMap)
                .withLimits(coordinatorResourcesLimitsMap)
                .endResources()
                .withVolumeMounts(coordinatorVolumeMounts)
                .withPorts(containerPorts)
                .build();
        coordinatorContainers.add(coordinatorContainer);


        // create coordinator deployment.
        DeploymentBuilder coordinatorDeploymentBuilder = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(DEFAULT_COORDINATOR_DEPLOYMENT)
                    .withNamespace(namespace)
                    .addToLabels("app", "trino-cluster").addToLabels("component", "coordinator")
                .endMetadata()
                .withNewSpec()
                    .withNewSelector()
                        .addToMatchLabels("app", "trino-cluster").addToMatchLabels("component", "coordinator")
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .addToLabels("app", "trino-cluster").addToLabels("component", "coordinator")
                        .endMetadata()
                        .withNewSpec()
                            .withServiceAccountName(serviceAccountName)
                            .withSecurityContext(securityContext)
                            .withNodeSelector(coordinator.getNodeSelector())
                            .withTolerations(coordinator.getTolerations())
                            .withAffinity(coordinator.getAffinity())
                            .withImagePullSecrets(image.getImagePullSecrets())
                            .withVolumes(coordinatorVolumes)
                            .withContainers(coordinatorContainers)
                            .withInitContainers(initContainers)
                        .endSpec()
                    .endTemplate()
                .endSpec();

        // create coordinator deployment.
        Deployment retDeployment = client.apps().deployments().inNamespace(namespace).createOrReplace(coordinatorDeploymentBuilder.build());
        LOG.info("coordinator deployment: \n{}", YamlUtils.objectToYaml(retDeployment));

        // construct coordinator service.
        ServicePort coordinatorServicePort = new ServicePortBuilder()
                .withName("http")
                .withPort(coordinatorTrinoPort)
                .withTargetPort(new IntOrString("http"))
                .withProtocol("TCP")
                .build();

        Map<String, String> serviceLabelSelectorMap = new HashMap<>();
        serviceLabelSelectorMap.put("app", "trino-cluster");
        serviceLabelSelectorMap.put("component", "coordinator");

        Service coordinatorService = new ServiceBuilder()
                .withNewMetadata()
                    .withName(DEFAULT_COORDINATOR_SERVICE)
                    .withNamespace(namespace)
                    .addToLabels("app", "trino-cluster").addToLabels("component", "coordinator")
                .endMetadata()
                .withNewSpec()
                    .withType("ClusterIP")
                    .withPorts(coordinatorServicePort)
                    .withSelector(serviceLabelSelectorMap)
                .endSpec().build();

        // create coordinator service.
        Service retService = client.services().inNamespace(namespace).createOrReplace(coordinatorService);
        LOG.info("coordinator service: \n{}", YamlUtils.objectToYaml(retService));


        // create jmx related headless services.
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
                    .withName(COORDINATOR_RMI_REGISTRY_SERVICE)
                    .withNamespace(namespace)
                    .addToLabels("app", "trino-cluster").addToLabels("component", "coordinator")
                    .endMetadata()
                    .withNewSpec()
                    .withClusterIP("None")
                    .withPorts(rmiRegistryServicePort)
                    .withSelector(serviceLabelSelectorMap)
                    .endSpec().build();

            // create coordinator rmiregistry service.
            retService = client.services().inNamespace(namespace).createOrReplace(rmiRegistryService);
            LOG.info("coordinator rmiregistry service: \n{}", YamlUtils.objectToYaml(retService));
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
                    .withName("trino-coordinator-rmi-service")
                    .withNamespace(namespace)
                    .addToLabels("app", "trino-cluster").addToLabels("component", "coordinator")
                    .endMetadata()
                    .withNewSpec()
                    .withClusterIP("None")
                    .withPorts(rmiServicePort)
                    .withSelector(serviceLabelSelectorMap)
                    .endSpec().build();

            // create coordinator rmi service.
            retService = client.services().inNamespace(namespace).createOrReplace(rmiService);
            LOG.info("coordinator rmi service: \n{}", YamlUtils.objectToYaml(retService));
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
                    .withName(COORDINATOR_JMX_EXPORTER_SERVICE)
                    .withNamespace(namespace)
                    .addToLabels("app", "trino-cluster").addToLabels("component", "coordinator")
                    .endMetadata()
                    .withNewSpec()
                    .withClusterIP("None")
                    .withPorts(jmxExporterServicePort)
                    .withSelector(serviceLabelSelectorMap)
                    .endSpec().build();

            // create coordinator jmxexporter service.
            retService = client.services().inNamespace(namespace).createOrReplace(jmxExporterService);
            LOG.info("coordinator jmxexporter service: \n{}", YamlUtils.objectToYaml(retService));
        }
    }

    public void delete(TrinoCluster trinoCluster) {
        TrinoClusterSpec spec = trinoCluster.getSpec();
        String namespace = spec.getNamespace();
        String serviceAccountName = spec.getServiceAccountName();

        // delete coordinator deployment.
        boolean deploymentDeleted = client.apps().deployments().inNamespace(namespace).withName(DEFAULT_COORDINATOR_DEPLOYMENT).delete();
        LOG.info("coordinator deployment [{}] deleted  in namespace [{}]: {}", DEFAULT_COORDINATOR_DEPLOYMENT, namespace, deploymentDeleted);

        // delete coordinator service.
        boolean serviceDeleted = client.services().inNamespace(namespace).withName(DEFAULT_COORDINATOR_SERVICE).delete();
        LOG.info("coordinator service [{}] deleted  in namespace [{}]: {}", DEFAULT_COORDINATOR_SERVICE, namespace, serviceDeleted);

        // delete coordinator config map.
        boolean configmapDeleted = client.configMaps().inNamespace(namespace).withName(DEFAULT_COORDINATOR_CONFIGMAP).delete();
        LOG.info("coordinator configmap [{}] deleted  in namespace [{}]: {}", DEFAULT_COORDINATOR_CONFIGMAP, namespace, configmapDeleted);

        // delete service account.
        boolean saDeleted = client.serviceAccounts().inNamespace(namespace).withName(serviceAccountName).delete();
        LOG.info("sa [{}] deleted  in namespace [{}]: {}", serviceAccountName, namespace, saDeleted);
    }
}
