package com.cloudcheflabs.dataroaster.cli;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.cli.domain.BlueprintGraph;
import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlueprintTestRunner {

    @Test
    public void readBlueprint() throws Exception {

        String blueprintYaml = System.getProperty("blueprintYaml", "blueprint/blueprint.yaml");
        boolean fromClasspath = Boolean.valueOf(System.getProperty("fromClasspath", "true"));
        String blueprint = FileUtils.fileToString(blueprintYaml, fromClasspath);

        InputStream inputStream = new ByteArrayInputStream(blueprint.getBytes());
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(inputStream);

        //System.out.printf("blueprint.yaml: \n%s", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), map)));

        BlueprintGraph blueprintGraph = new BlueprintGraph();

        // project
        Map<String, Object> projectMap = (Map<String, Object>) map.get("project");
        String projectName = (String) projectMap.get("name");
        String projectDescription = (String) projectMap.get("description");

        //System.out.printf("project name: %s, project description: %s\n", projectName, projectDescription);

        BlueprintGraph.Project project = new BlueprintGraph.Project();
        project.setName(projectName);
        project.setDescription(projectDescription);
        blueprintGraph.setProject(project);

        // cluster.
        Map<String, Object> clusterMap = (Map<String, Object>) map.get("cluster");
        String clusterName = (String) clusterMap.get("name");
        String clusterDescription = (String) clusterMap.get("description");
        String kubeconfig = (String) clusterMap.get("kubeconfig");

        //System.out.printf("cluster name: %s, cluster description: %s, kubeconfig: %s\n", clusterName, clusterDescription, kubeconfig);

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
            //System.out.printf("propertyName: %s\n", propertyName);

            Map<String, Object> kvMap = (Map<String, Object>) propertyMap.get("kv");
            for(String kvKey : kvMap.keySet()) {
                String kvValue = (String) kvMap.get(kvKey);
                //System.out.printf("kvKey: %s, kvValue: %s\n", kvKey, kvValue);
            }

            // just kvMap interested.
            keyedPropertyMap.put(propertyName, kvMap);
        }
        //System.out.printf("keyedPropertyMap: \n%s", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), keyedPropertyMap)));

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

        //System.out.printf("blueprint graph: \n%s", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), blueprintGraph)));


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

        //System.out.printf("blueprint graph: \n%s", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), blueprintGraph)));

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

        System.out.printf("serviceDependencyList - size: %d\n%s", blueprintGraph.getServiceDependencyList().size(), JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), blueprintGraph.getServiceDependencyList())));
    }

    private void setDependentServiceRecursively(Set<String> registeredServiceSet,
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

    @Test
    public void readKubeconfig() throws Exception {
        String filePath = "/home/opc/.kube/config";
        File kubeconfigFile = new File(filePath);
        String kubeconfigPath = kubeconfigFile.getAbsolutePath();
        String kubeconfig = FileUtils.fileToString(kubeconfigPath, false);
        System.out.printf("kubeconfig: \n%s", kubeconfig);
    }
}
