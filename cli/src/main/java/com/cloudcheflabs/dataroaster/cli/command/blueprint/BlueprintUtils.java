package com.cloudcheflabs.dataroaster.cli.command.blueprint;

import com.cloudcheflabs.dataroaster.cli.domain.BlueprintGraph;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlueprintUtils {

    public static BlueprintGraph parseBlueprintYaml(String blueprint) {
        InputStream inputStream = new ByteArrayInputStream(blueprint.getBytes());
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(inputStream);

        BlueprintGraph blueprintGraph = new BlueprintGraph();

        // project
        Map<String, Object> projectMap = (Map<String, Object>) map.get("project");
        String projectName = (String) projectMap.get("name");
        String projectDescription = (String) projectMap.get("description");

        BlueprintGraph.Project project = new BlueprintGraph.Project();
        project.setName(projectName);
        project.setDescription(projectDescription);
        blueprintGraph.setProject(project);

        // cluster.
        Map<String, Object> clusterMap = (Map<String, Object>) map.get("cluster");
        String clusterName = (String) clusterMap.get("name");
        String clusterDescription = (String) clusterMap.get("description");
        String kubeconfig = (String) clusterMap.get("kubeconfig");

        BlueprintGraph.Cluster cluster = new BlueprintGraph.Cluster();
        cluster.setName(clusterName);
        cluster.setDescription(clusterDescription);
        cluster.setKubeconfig(kubeconfig);
        blueprintGraph.setCluster(cluster);

        // properties.
        List<Map<String, Object>> propertiesList = (List<Map<String, Object>>) map.get("properties");

        // property map with the key of property name and the value of kvMap.
        Map<String, Map<String, Object>> keyedPropertyMap = new HashMap<>();
        for(Map<String, Object> propertyMap : propertiesList)
        {
            String propertyName = (String) propertyMap.get("name");

            Map<String, Object> kvMap = (Map<String, Object>) propertyMap.get("kv");
            for(String kvKey : kvMap.keySet()) {
                String kvValue = (String) kvMap.get(kvKey);
            }

            // just kvMap interested.
            keyedPropertyMap.put(propertyName, kvMap);
        }

        blueprintGraph.setPropertyMap(keyedPropertyMap);


        // ======== services ==========

        List<Map<String, Object>> servicesList = (List<Map<String, Object>>) map.get("services");

        // service map.
        Map<String, BlueprintGraph.Service> keyedServiceMap = new HashMap<>();
        for(Map<String, Object> serviceMap : servicesList) {
            String serviceName = (String) serviceMap.get("name");

            BlueprintGraph.Service service = new BlueprintGraph.Service();
            service.setName(serviceName);

            if(serviceMap.containsKey("params")) {
                Map<String, Object> params = (Map<String, Object>) serviceMap.get("params");
                service.setParams(new ConcurrentHashMap<String, Object>(params));
            }

            if(serviceMap.containsKey("extra-params")) {
                Map<String, Object> extraParams = (Map<String, Object>) serviceMap.get("extra-params");
                service.setExtraParams(new ConcurrentHashMap<String, Object>(extraParams));
            }

            String depends = null;
            if(serviceMap.containsKey("depends")) {
                depends = (String) serviceMap.get("depends");
            }
            service.setDepends(depends);

            keyedServiceMap.put(serviceName, service);
        }

        blueprintGraph.setServiceMap(keyedServiceMap);

        Map<String, BlueprintGraph.Service> serviceMap = blueprintGraph.getServiceMap();
        for(String serviceName: serviceMap.keySet()) {
            BlueprintGraph.Service service = serviceMap.get(serviceName);

            // update params with properties.
            ConcurrentHashMap<String, Object> params = service.getParams();
            if(params != null) {
                for(String paramKey : params.keySet()) {
                    if(paramKey.equals("properties")) {
                        List<String> propertyNameList = (List<String>) params.get("properties");

                        for(String propertyName : propertyNameList) {
                            Map<String, Object> property = blueprintGraph.getPropertyMapByName(propertyName);
                            params.putAll(property);
                        }
                        params.remove("properties");
                    }
                    Object paramValueObj = params.get(paramKey);
                    if(paramValueObj instanceof Map) {
                        Map<String, Object> paramValueMap = (Map<String, Object>) paramValueObj;
                        for(String paramValueKey : paramValueMap.keySet()) {
                            if(paramValueKey.equals("property-ref")) {
                                String propertyName = (String) paramValueMap.get(paramValueKey);
                                Map<String, Object> property = blueprintGraph.getPropertyMapByName(propertyName);
                                String propertyMapKey = (String) paramValueMap.get("key");
                                String paramValue = (String) property.get(propertyMapKey);

                                // set the real param value obtained from property value with the referenced key.
                                params.put(paramKey, paramValue);
                            }
                        }
                    }
                }
            }

            // update extra-params with properties.
            ConcurrentHashMap<String, Object> extraParams = service.getExtraParams();
            if(extraParams != null) {
                for(String paramKey : extraParams.keySet()) {
                    if(paramKey.equals("properties")) {
                        List<String> propertyNameList = (List<String>) params.get("properties");

                        for(String propertyName : propertyNameList) {
                            Map<String, Object> property = blueprintGraph.getPropertyMapByName(propertyName);
                            extraParams.putAll(property);
                        }
                        extraParams.remove("properties");
                    }
                    Object paramValueObj = extraParams.get(paramKey);
                    if(paramValueObj instanceof Map) {
                        Map<String, Object> paramValueMap = (Map<String, Object>) paramValueObj;
                        for(String paramValueKey : paramValueMap.keySet()) {
                            if(paramValueKey.equals("property-ref")) {
                                String propertyName = (String) paramValueMap.get(paramValueKey);
                                Map<String, Object> property = blueprintGraph.getPropertyMapByName(propertyName);
                                String propertyMapKey = (String) paramValueMap.get("key");
                                String paramValue = (String) property.get(propertyMapKey);

                                // set the real param value obtained from property value with the referenced key.
                                extraParams.put(paramKey, paramValue);
                            }
                        }
                    }
                }
            }
        }
        // update service map.
        blueprintGraph.setServiceMap(serviceMap);

        Set<String> registeredServiceSet = new HashSet<>();
        LinkedList<BlueprintGraph.Service> serviceDependencyList = new LinkedList<>();

        for(BlueprintGraph.Service service : serviceMap.values()) {
            String depends = service.getDepends();
            BlueprintGraph.Service parentService = (depends == null) ? null : blueprintGraph.getServiceByName(depends);
            if(parentService != null) {
                if(!registeredServiceSet.contains(service.getName())) {
                    registeredServiceSet.add(service.getName());
                    serviceDependencyList.add(service);
                }
                setDependentServiceRecursively(registeredServiceSet, serviceDependencyList, blueprintGraph, parentService);
            } else {
                if(!registeredServiceSet.contains(service.getName())) {
                    registeredServiceSet.add(service.getName());
                    serviceDependencyList.addFirst(service);
                }
            }
        }

        blueprintGraph.setServiceDependencyList(serviceDependencyList);

        return blueprintGraph;
    }

    private static void setDependentServiceRecursively(Set<String> registeredServiceSet,
                                                LinkedList<BlueprintGraph.Service> serviceDependencyList,
                                                BlueprintGraph blueprintGraph,
                                                BlueprintGraph.Service service) {
        String depends = service.getDepends();
        BlueprintGraph.Service parentService = (depends == null) ? null : blueprintGraph.getServiceByName(depends);
        if(parentService != null) {
            if(!registeredServiceSet.contains(service.getName())) {
                registeredServiceSet.add(service.getName());
                serviceDependencyList.add(service);
            }
            setDependentServiceRecursively(registeredServiceSet, serviceDependencyList, blueprintGraph, parentService);
        }
        else {
            if(!registeredServiceSet.contains(service.getName())) {
                registeredServiceSet.add(service.getName());
                serviceDependencyList.addFirst(service);
            }
            return ;
        }
    }
}
