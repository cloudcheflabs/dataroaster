package com.cloudcheflabs.dataroaster.operators.dataroaster.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.ComponentsService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.CustomResourceService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.K8sResourceService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.Roles;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.Components;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import com.cloudcheflabs.dataroaster.operators.dataroaster.util.Base64Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SparkThriftServerController {

    private static Logger LOG = LoggerFactory.getLogger(SparkThriftServerController.class);

    public static final String COMPONENT_SPARK_THRIFT_SERVER = "spark-thrift-server";

    public static final String COMPONENT_NFS = "nfs";

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("k8sResourceServiceImpl")
    private K8sResourceService k8sResourceService;


    @Autowired
    @Qualifier("customResourceServiceImpl")
    private CustomResourceService customResourceService;


    @Autowired
    @Qualifier("componentsServiceImpl")
    private ComponentsService componentsService;
    
    @Autowired
    KubernetesClient kubernetesClient;


    @PostMapping("/v1/spark_thrift_server/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {

            String nfsStorageClass = params.get("nfs_storage_class");
            String nfsStorageSize = params.get("nfs_storage_size");

            // base64 encoded.
            String s3AccessKey = params.get("s3_access_key");
            // base64 encoded.
            String s3SecretKey = params.get("s3_secret_key");

            String pvcSize = params.get("pvc_size");

            String yaml = params.get("yaml");

            String decodedYaml = Base64Utils.decodeBase64(yaml);
            LOG.info("decodedYaml: {}", decodedYaml);

            Components components = componentsService.findOne(COMPONENT_SPARK_THRIFT_SERVER);
            if(components == null) {
                components = new Components();
                components.setCompName(COMPONENT_SPARK_THRIFT_SERVER);
                componentsService.create(components);
            }

            // create spark thrift server custom resource.
            CustomResource sparkThriftServerCustomResource = CustomResourceUtils.fromYaml(decodedYaml);
            sparkThriftServerCustomResource.setComponents(components);


            // create nfs on kubernetes.
            Map<String, Object> kv = new HashMap<>();
            kv.put("storageClass", nfsStorageClass);
            kv.put("size", nfsStorageSize);

            Components nfsComponent = componentsService.findOne(COMPONENT_NFS);
            if(nfsComponent == null) {
                nfsComponent = new Components();
                nfsComponent.setCompName(COMPONENT_NFS);
                componentsService.create(nfsComponent);
            }

            // check if nfs is installed and running.
            boolean nfsRunning = ContainerStatusChecker.isRunning(kubernetesClient,
                    "nfs",
                    "nfs",
                    "app",
                    "nfs-server-provisioner");
            LOG.info("nfsRunning: [{}]", nfsRunning);

            // if nfs is not installed.
            if(!nfsRunning) {
                String nfsCustomResourceString =
                        TemplateUtils.replace("/templates/spark-thrift-server/nfs.yaml", true, kv);
                CustomResource nfsCustomResource = CustomResourceUtils.fromYaml(nfsCustomResourceString);
                nfsCustomResource.setComponents(nfsComponent);
                k8sResourceService.createCustomResource(nfsCustomResource);


                // check if nfs is running status.
                ContainerStatusChecker.checkContainerStatus(kubernetesClient,
                        "nfs",
                        "nfs",
                        "app",
                        "nfs-server-provisioner");
                LOG.info("nfs installed...");
            }

            // create namespace.
            String targetNamespace = CustomResourceUtils.getTargetNamespace(sparkThriftServerCustomResource.getYaml());
            kubernetesClient.namespaces().createOrReplace(new NamespaceBuilder().withNewMetadata().withName(targetNamespace).endMetadata().build());
            LOG.info("namespace [{}] created...", targetNamespace);

            // create service account.
            String serviceAccount = targetNamespace;
            kubernetesClient.serviceAccounts().createOrReplace(new ServiceAccountBuilder().withNewMetadata().withName(serviceAccount).endMetadata().build());
            LOG.info("serviceAccount [{}] created...", serviceAccount);


            // create clusterrole.
            kv = new HashMap<>();
            kv.put("namespace", targetNamespace);
            String clusterRoleString =
                    TemplateUtils.replace("/templates/spark-thrift-server/cluster-role.yaml", true, kv);
            kubernetesClient.rbac().clusterRoles().load(new ByteArrayInputStream(clusterRoleString.getBytes())).createOrReplace();
            LOG.info("clusterrole  created...");

            // create cluster role binding.
            String clusterRoleBindingString =
                    TemplateUtils.replace("/templates/spark-thrift-server/cluster-rolebinding.yaml", true, kv);
            kubernetesClient.rbac().clusterRoleBindings().load(new ByteArrayInputStream(clusterRoleBindingString.getBytes())).createOrReplace();
            LOG.info("clusterrole binding  created...");


            // create s3 secret.
            kv = new HashMap<>();
            kv.put("accessKey", s3AccessKey);
            kv.put("secretKey", s3SecretKey);
            kv.put("operatorNamespace", sparkThriftServerCustomResource.getNamespace());
            String s3SecretString =
                    TemplateUtils.replace("/templates/spark-thrift-server/s3-secret.yaml", true, kv);
            kubernetesClient.secrets().inNamespace(sparkThriftServerCustomResource.getNamespace())
                    .load(new ByteArrayInputStream(s3SecretString.getBytes())).createOrReplace();
            LOG.info("s3 secret created...");

            // create pvc.
            List<String> pvcList = CustomResourceUtils.getSparkApplicationPvcNames(sparkThriftServerCustomResource.getYaml());
            for(String pvcName : pvcList) {
                kv = new HashMap<>();
                kv.put("pvcName", pvcName);
                kv.put("size", pvcSize);
                kv.put("namespace", targetNamespace);
                String pvcString =
                        TemplateUtils.replace("/templates/spark-thrift-server/spark-thrift-server-pvc.yaml", true, kv);
                kubernetesClient.persistentVolumeClaims().inNamespace(targetNamespace)
                        .load(new ByteArrayInputStream(pvcString.getBytes())).createOrReplace();
            }
            LOG.info("pvc created...");

            // create spark thrift server.
            k8sResourceService.createCustomResource(sparkThriftServerCustomResource);
            LOG.info("spark thrift server installed...");

            // check spark thrift server is running status.
            ContainerStatusChecker.checkContainerStatus(kubernetesClient,
                    COMPONENT_SPARK_THRIFT_SERVER,
                    targetNamespace,
                    "spark-role",
                    "executor",
                    120);

            // create spark thrift server service.
            kv = new HashMap<>();
            kv.put("namespace", targetNamespace);
            String serviceString =
                    TemplateUtils.replace("/templates/spark-thrift-server/spark-thrift-server-service.yaml", true, kv);
            kubernetesClient.services().inNamespace(targetNamespace)
                    .load(new ByteArrayInputStream(serviceString.getBytes())).createOrReplace();
            LOG.info("spark thrift server service created...");

            return ControllerUtils.successMessage();
        });
    }




    @DeleteMapping("/v1/spark_thrift_server/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            Components components = componentsService.findOne(COMPONENT_SPARK_THRIFT_SERVER);
            if(components != null) {
                for(CustomResource customResource : components.getCustomResourceSet()) {
                    k8sResourceService.deleteCustomResource(customResource.getName(), customResource.getNamespace(), customResource.getKind());
                }
            }
            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/spark_thrift_server/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            List<Map<String, Object>> mapList = new ArrayList<>();

            Components components = componentsService.findOne(COMPONENT_SPARK_THRIFT_SERVER);
            if(components != null) {
                for(CustomResource customResource : components.getCustomResourceSet()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("kind", customResource.getKind());
                    map.put("name", customResource.getName());
                    map.put("namespace", customResource.getNamespace());
                    map.put("components", COMPONENT_SPARK_THRIFT_SERVER);

                    mapList.add(map);
                }
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
