package com.cloudcheflabs.dataroaster.cli.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlueprintGraph {

    private Project project;
    private Cluster cluster;
    private Map<String, Map<String, Object>> propertyMap;
    private Map<String, Service> serviceMap;
    private List<Service> serviceDependencyList;

    public List<Service> getServiceDependencyList() {
        return serviceDependencyList;
    }

    public void setServiceDependencyList(List<Service> serviceDependencyList) {
        this.serviceDependencyList = serviceDependencyList;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Map<String, Map<String, Object>> getPropertyMap() {
        return propertyMap;
    }

    public Map<String, Object> getPropertyMapByName(String name) {
        return (propertyMap.containsKey(name)) ? propertyMap.get(name) : null;
    }

    public void setPropertyMap(Map<String, Map<String, Object>> propertyMap) {
        this.propertyMap = propertyMap;
    }

    public Map<String, Service> getServiceMap() {
        return serviceMap;
    }

    public Service getServiceByName(String name) {
        Service service = null;
        if(serviceMap.containsKey(name)) {
            service = serviceMap.get(name);
        }
        return service;
    }

    public void setServiceMap(Map<String, Service> serviceMap) {
        this.serviceMap = serviceMap;
    }

    public static class Project {
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Cluster {
        private String name;
        private String description;
        private String kubeconfig;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getKubeconfig() {
            return kubeconfig;
        }

        public void setKubeconfig(String kubeconfig) {
            this.kubeconfig = kubeconfig;
        }
    }

    public static class Service {
        private String name;
        private ConcurrentHashMap<String, Object> params;
        private ConcurrentHashMap<String, Object> extraParams;
        private String depends;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ConcurrentHashMap<String, Object> getParams() {
            return params;
        }

        public void setParams(ConcurrentHashMap<String, Object> params) {
            this.params = params;
        }

        public ConcurrentHashMap<String, Object> getExtraParams() {
            return extraParams;
        }

        public void setExtraParams(ConcurrentHashMap<String, Object> extraParams) {
            this.extraParams = extraParams;
        }

        public String getDepends() {
            return depends;
        }

        public void setDepends(String depends) {
            this.depends = depends;
        }
    }
}
