package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.StreamingService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.*;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.StreamingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class StreamingServiceImpl implements StreamingService {
    private static Logger LOG = LoggerFactory.getLogger(StreamingServiceImpl.class);

    @Autowired
    @Qualifier("hibernateK8sClusterDao")
    private K8sClusterDao k8sClusterDao;

    @Autowired
    @Qualifier("hibernateK8sNamespaceDao")
    private K8sNamespaceDao k8sNamespaceDao;

    @Autowired
    @Qualifier("hibernateServicesDao")
    private ServicesDao servicesDao;

    @Autowired
    @Qualifier("hibernateServiceDefDao")
    private ServiceDefDao serviceDefDao;

    @Autowired
    @Qualifier("hibernateProjectDao")
    private ProjectDao projectDao;

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    @Override
    public void create(long projectId,
                       long serviceDefId,
                       long clusterId,
                       String userName,
                       int kafkaReplicaCount,
                       int kafkaStorageSize,
                       String storageClass,
                       int zkReplicaCount) {
        ServiceDef serviceDef = serviceDefDao.findOne(serviceDefId);
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        Project project = projectDao.findOne(projectId);

        String originalCreator = project.getUsers().getUserName();
        if(!userName.equals(originalCreator)) {
            throw new RuntimeException("user [" + userName + "] not allowed to update.");
        }

        // kafka.
        K8sNamespace kafkaNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_KAFKA, k8sCluster.getId());
        if(kafkaNamespace == null) {
            kafkaNamespace = new K8sNamespace();
            kafkaNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_KAFKA);
            kafkaNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(kafkaNamespace);
        }
        else {
            throw new RuntimeException("kafka already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // service.
        Set<Services> servicesSet = kafkaNamespace.getServicesSet();
        if(servicesSet.size() == 0) {
            Services kafkaService = new Services();
            kafkaService.setServiceDef(serviceDef);
            // just indicate that the service will be installed in the cluster which kafka namespace belongs to
            kafkaService.setK8sNamespace(kafkaNamespace);
            kafkaService.setProject(project);

            servicesDao.create(kafkaService);
        }
        else {
            throw new RuntimeException("streaming service already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // build map.
        Map<String, Object> map = new HashMap<>();
        map.put("kafkaReplicaCount", kafkaReplicaCount);
        map.put("kafkaStorageSize", kafkaStorageSize);
        map.put("storageClass", storageClass);
        map.put("zkReplicaCount", zkReplicaCount);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return StreamingHandler.create(kubeconfig, map);
                });

                return;
            }
        }
        throw new RuntimeException("user [" + userName + "] has no secret for this project [" + k8sCluster.getClusterName() + "]");
    }

    @Override
    public void delete(long serviceId, String userName) {
        Services services = servicesDao.findOne(serviceId);
        K8sCluster k8sCluster = services.getK8sNamespace().getK8sCluster();
        Project project = services.getProject();

        String originalCreator = project.getUsers().getUserName();
        if(!userName.equals(originalCreator)) {
            throw new RuntimeException("user [" + userName + "] not allowed to update.");
        }

        // delete services.
        servicesDao.delete(services);

        // delete namespaces.
        K8sNamespace kafkaNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_KAFKA, k8sCluster.getId());
        k8sNamespaceDao.delete(kafkaNamespace);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return StreamingHandler.delete(kubeconfig);
                });

                return;
            }
        }
        throw new RuntimeException("user [" + userName + "] has no secret for this project [" + k8sCluster.getClusterName() + "]");
    }
}
