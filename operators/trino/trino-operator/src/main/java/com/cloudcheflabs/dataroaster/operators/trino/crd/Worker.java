package com.cloudcheflabs.dataroaster.operators.trino.crd;

import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Toleration;

import java.util.List;
import java.util.Map;

public class Worker {
    private int replicas;
    private Autoscaler autoscaler;
    private Resources resources;
    private Map<String, String> nodeSelector;
    private Affinity affinity;
    private List<Toleration> tolerations;

    private List<Container> initContainers;
    private List<Config> configs;

    public List<Container> getInitContainers() {
        return initContainers;
    }

    public void setInitContainers(List<Container> initContainers) {
        this.initContainers = initContainers;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public Autoscaler getAutoscaler() {
        return autoscaler;
    }

    public void setAutoscaler(Autoscaler autoscaler) {
        this.autoscaler = autoscaler;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }


    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    public List<Toleration> getTolerations() {
        return tolerations;
    }

    public void setTolerations(List<Toleration> tolerations) {
        this.tolerations = tolerations;
    }

    public List<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Config> configs) {
        this.configs = configs;
    }
}
