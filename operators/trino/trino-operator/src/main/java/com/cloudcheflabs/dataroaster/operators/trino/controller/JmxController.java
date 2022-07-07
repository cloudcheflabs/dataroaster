package com.cloudcheflabs.dataroaster.operators.trino.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import com.cloudcheflabs.dataroaster.operators.trino.domain.Roles;
import com.cloudcheflabs.dataroaster.operators.trino.util.Base64Utils;
import com.cloudcheflabs.dataroaster.operators.trino.util.JmxUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cloudcheflabs.dataroaster.operators.trino.config.TrinoConfiguration.*;

@RestController
public class JmxController implements InitializingBean {

    private static Logger LOG = LoggerFactory.getLogger(JmxController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;


    @Autowired
    private KubernetesClient client;

    private MixedOperation<TrinoCluster, KubernetesResourceList<TrinoCluster>, Resource<TrinoCluster>> trinoClusterClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        trinoClusterClient = client.resources(TrinoCluster.class);
    }


    @GetMapping("/v1/cluster/list_clusters")
    public String listClusters(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);

            List<Map<String, Object>> mapList = new ArrayList<>();

            KubernetesResourceList<TrinoCluster> resourceList = trinoClusterClient.inNamespace(namespace).list();
            List<TrinoCluster> trinoClusters = resourceList.getItems();
            for(TrinoCluster trinoCluster : trinoClusters) {
                String clusterName = trinoCluster.getMetadata().getName();
                String clusterNamespace = trinoCluster.getSpec().getNamespace();

                Map<String, Object> map = new HashMap<>();
                map.put("clusterName", clusterName);
                map.put("clusterNamespace", clusterNamespace);
                map.put("coordinatorRmiRegistryEndpoints",
                        getEndpointAddresses(client.endpoints().inNamespace(clusterNamespace).withName(COORDINATOR_RMI_REGISTRY_SERVICE).get()));
                map.put("coordinatorJmxExporterEndpoints",
                        getEndpointAddresses(client.endpoints().inNamespace(clusterNamespace).withName(COORDINATOR_JMX_EXPORTER_SERVICE).get()));
                map.put("workerRmiRegistryEndpoints",
                        getEndpointAddresses(client.endpoints().inNamespace(clusterNamespace).withName(WORKER_RMI_REGISTRY_SERVICE).get()));
                map.put("workerJmxExporterEndpoints",
                        getEndpointAddresses(client.endpoints().inNamespace(clusterNamespace).withName(WORKER_JMX_EXPORTER_SERVICE).get()));

                mapList.add(map);
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }


    private List<Map<String, Object>> getEndpointAddresses(Endpoints endPoints) {
        List<Map<String, Object>> endpointList = new ArrayList<>();
        if(endPoints != null) {
            for(EndpointSubset endpointSubset : endPoints.getSubsets()) {
                int port = endpointSubset.getPorts().get(0).getPort();
                for(EndpointAddress endpointAddress : endpointSubset.getAddresses()) {
                    String ip = endpointAddress.getIp();
                    Map<String, Object> endpointAddressMap = new HashMap<>();
                    endpointAddressMap.put("address", ip + ":" + port);
                    endpointList.add(endpointAddressMap);
                }
            }
        }
        return endpointList;
    }


    @GetMapping("/v1/jmx/list_mbeans")
    public String listMBeans(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);
            String clusterName = params.get("cluster_name");
            LOG.info("clusterName: {}", clusterName);

            List<Map<String, Object>> mapList = new ArrayList<>();

            TrinoCluster trinoCluster = trinoClusterClient.inNamespace(namespace).withName(clusterName).get();
            String clusterNamespace= trinoCluster.getSpec().getNamespace();

            List<Map<String, Object>> coordinatorRmiRegistryEndpoints =
                    getEndpointAddresses(client.endpoints().inNamespace(clusterNamespace).withName(COORDINATOR_RMI_REGISTRY_SERVICE).get());
            for(Map<String, Object> addressMap : coordinatorRmiRegistryEndpoints) {
                String address = (String) addressMap.get("address");
                String[] addressTokens = address.split(":");
                String host = addressTokens[0];
                String port = addressTokens[1];

                List<Map<String, Object>> mbeanLists = JmxUtils.listAllMBeanValues(host, port);

                Map<String, Object> mbeanMap = new HashMap<>();
                mbeanMap.put("clusterNamespace", clusterNamespace);
                mbeanMap.put("role", "coordinator");
                mbeanMap.put("address", address);
                mbeanMap.put("mbeans", mbeanLists);

                mapList.add(mbeanMap);
            }


            List<Map<String, Object>> workerRmiRegistryEndpoints =
                    getEndpointAddresses(client.endpoints().inNamespace(clusterNamespace).withName(WORKER_RMI_REGISTRY_SERVICE).get());
            for(Map<String, Object> addressMap : workerRmiRegistryEndpoints) {
                String address = (String) addressMap.get("address");
                String[] addressTokens = address.split(":");
                String host = addressTokens[0];
                String port = addressTokens[1];

                List<Map<String, Object>> mbeanLists = JmxUtils.listAllMBeanValues(host, port);

                Map<String, Object> mbeanMap = new HashMap<>();
                mbeanMap.put("clusterNamespace", clusterNamespace);
                mbeanMap.put("role", "worker");
                mbeanMap.put("address", address);
                mbeanMap.put("mbeans", mbeanLists);

                mapList.add(mbeanMap);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }



    @GetMapping("/v1/jmx/get_value")
    public String getMBeanValue(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespace = params.get("namespace");
            LOG.info("namespace: {}", namespace);
            String clusterName = params.get("cluster_name");
            LOG.info("clusterName: {}", clusterName);

            String objectName = System.getProperty("object_name");
            LOG.info("objectName: {}", objectName);

            String attribute = System.getProperty("attribute");
            LOG.info("attribute: {}", attribute);

            // optional.
            String compositeKey = System.getProperty("composite_key");
            LOG.info("compositeKey: {}", compositeKey);

            List<Map<String, Object>> mapList = new ArrayList<>();

            TrinoCluster trinoCluster = trinoClusterClient.inNamespace(namespace).withName(clusterName).get();
            String clusterNamespace= trinoCluster.getSpec().getNamespace();

            List<Map<String, Object>> coordinatorRmiRegistryEndpoints =
                    getEndpointAddresses(client.endpoints().inNamespace(clusterNamespace).withName(COORDINATOR_RMI_REGISTRY_SERVICE).get());
            for(Map<String, Object> addressMap : coordinatorRmiRegistryEndpoints) {
                String address = (String) addressMap.get("address");
                String[] addressTokens = address.split(":");
                String host = addressTokens[0];
                String port = addressTokens[1];

                String value = JmxUtils.getValue(host, port, objectName, attribute, compositeKey);

                Map<String, Object> map = new HashMap<>();
                map.put("clusterNamespace", clusterNamespace);
                map.put("role", "coordinator");
                map.put("address", address);
                map.put("objectName", objectName);
                map.put("attribute", attribute);
                map.put("compositeKey", compositeKey);
                map.put("value", value);

                mapList.add(map);
            }


            List<Map<String, Object>> workerRmiRegistryEndpoints =
                    getEndpointAddresses(client.endpoints().inNamespace(clusterNamespace).withName(WORKER_RMI_REGISTRY_SERVICE).get());
            for(Map<String, Object> addressMap : workerRmiRegistryEndpoints) {
                String address = (String) addressMap.get("address");
                String[] addressTokens = address.split(":");
                String host = addressTokens[0];
                String port = addressTokens[1];

                String value = JmxUtils.getValue(host, port, objectName, attribute, compositeKey);

                Map<String, Object> map = new HashMap<>();
                map.put("clusterNamespace", clusterNamespace);
                map.put("role", "worker");
                map.put("address", address);
                map.put("objectName", objectName);
                map.put("attribute", attribute);
                map.put("compositeKey", compositeKey);
                map.put("value", value);

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
