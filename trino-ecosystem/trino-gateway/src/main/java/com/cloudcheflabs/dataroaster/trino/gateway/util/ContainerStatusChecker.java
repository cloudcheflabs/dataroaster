package com.cloudcheflabs.dataroaster.trino.gateway.util;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ContainerStatusChecker {

    private static Logger LOG = LoggerFactory.getLogger(ContainerStatusChecker.class);

    public static void checkContainerStatus(KubernetesClient kubernetesClient, String componentName, String namespace, String labelKey, String labelValue) {
        checkContainerStatus(kubernetesClient, componentName, namespace, labelKey, labelValue, 20);
    }
    public static void checkContainerStatus(KubernetesClient kubernetesClient, String componentName, String namespace, String labelKey, String labelValue, int maxCount) {
        int MAX_COUNT = maxCount;
        int count = 0;
        boolean running = true;
        // watch pod if it has the status of RUNNING.
        while (running) {
            PodList podList = kubernetesClient.pods().inNamespace(namespace).list();
            for(Pod pod : podList.getItems()) {
                ObjectMeta metadata = pod.getMetadata();
                //LOG.info("metadata: [{}]", JsonUtils.toJson(metadata));
                Map<String, String> labels = metadata.getLabels();
                LOG.info("labels: [{}]", JsonUtils.toJson(labels));
                for(String key : labels.keySet()) {
                    LOG.info("key: [{}]", key);
                    if(key.equals(labelKey)) {
                        String value = labels.get(key);
                        LOG.info("key: [{}], value: [{}]", key, value);
                        if(value.equals(labelValue)) {
                            PodStatus status = pod.getStatus();
                            List<ContainerStatus> containerStatuses = status.getContainerStatuses();
                            LOG.info("containerStatuses: [{}]", containerStatuses.size());
                            if (!containerStatuses.isEmpty()) {
                                ContainerStatus containerStatus = containerStatuses.get(0);
                                ContainerState state = containerStatus.getState();
                                LOG.info("state: [{}]", state.toString());
                                ContainerStateRunning containerStateRunning = state.getRunning();
                                if(containerStateRunning != null) {
                                    LOG.info("{} has running status now.", componentName);
                                    running = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if(count < MAX_COUNT) {
                count++;
                try {
                    Thread.sleep(5000);
                    continue;
                } catch (Exception e) {
                    System.err.println(e);
                }
            } else {
                throw new IllegalStateException("[" + componentName + "] has no running status!");
            }
        }
    }


    public static boolean isRunning(KubernetesClient kubernetesClient, String componentName, String namespace, String labelKey, String labelValue) {
        PodList podList = kubernetesClient.pods().inNamespace(namespace).list();
        for(Pod pod : podList.getItems()) {
            ObjectMeta metadata = pod.getMetadata();
            LOG.info("metadata: [{}]", JsonUtils.toJson(metadata));
            Map<String, String> labels = metadata.getLabels();
            LOG.info("labels: [{}]", JsonUtils.toJson(labels));
            for(String key : labels.keySet()) {
                LOG.info("key: [{}]", key);
                if(key.equals(labelKey)) {
                    String value = labels.get(key);
                    LOG.info("key: [{}], value: [{}]", key, value);
                    if(value.equals(labelValue)) {
                        PodStatus status = pod.getStatus();
                        List<ContainerStatus> containerStatuses = status.getContainerStatuses();
                        LOG.info("containerStatuses: [{}]", containerStatuses.size());
                        if (!containerStatuses.isEmpty()) {
                            ContainerStatus containerStatus = containerStatuses.get(0);
                            ContainerState state = containerStatus.getState();
                            LOG.info("state: [{}]", state.toString());
                            ContainerStateRunning containerStateRunning = state.getRunning();
                            if(containerStateRunning != null) {
                                LOG.info("{} has running status now.", componentName);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
