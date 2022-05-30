package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.DataCatalogService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.*;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.DataCatalogHandler;
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
public class DataCatalogServiceImpl implements DataCatalogService {
    private static Logger LOG = LoggerFactory.getLogger(DataCatalogServiceImpl.class);

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
                       String s3Bucket,
                       String s3AccessKey,
                       String s3SecretKey,
                       String s3Endpoint,
                       String storageClass,
                       int storageSize) {
        ServiceDef serviceDef = serviceDefDao.findOne(serviceDefId);
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        Project project = projectDao.findOne(projectId);

        String originalCreator = project.getUsers().getUserName();
        if(!userName.equals(originalCreator)) {
            throw new RuntimeException("user [" + userName + "] not allowed to update.");
        }

        // hivemetastore.
        K8sNamespace hivemetastoreNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_HIVE_METASTORE, k8sCluster.getId());
        if(hivemetastoreNamespace == null) {
            hivemetastoreNamespace = new K8sNamespace();
            hivemetastoreNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_HIVE_METASTORE);
            hivemetastoreNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(hivemetastoreNamespace);
        }
        else {
            throw new RuntimeException("hive metastore already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }


        // service.
        Set<Services> servicesSet = hivemetastoreNamespace.getServicesSet();
        if(servicesSet.size() == 0) {
            Services dataCatalogService = new Services();
            dataCatalogService.setServiceDef(serviceDef);
            // just indicate that the service will be installed in the cluster which hive metastore namespace belongs to
            dataCatalogService.setK8sNamespace(hivemetastoreNamespace);
            dataCatalogService.setProject(project);

            servicesDao.create(dataCatalogService);
        }
        else {
            throw new RuntimeException("data catalog service already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // build map.
        Map<String, Object> map = new HashMap<>();
        map.put("s3Bucket", s3Bucket);
        map.put("s3AccessKey", s3AccessKey);
        map.put("s3SecretKey", s3SecretKey);
        map.put("s3Endpoint", s3Endpoint);
        map.put("storageClass", storageClass);
        map.put("storageSize", storageSize);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return DataCatalogHandler.create(kubeconfig, map);
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
        K8sNamespace hivemetastoreNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_HIVE_METASTORE, k8sCluster.getId());
        k8sNamespaceDao.delete(hivemetastoreNamespace);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return DataCatalogHandler.delete(kubeconfig);
                });

                return;
            }
        }
        throw new RuntimeException("user [" + userName + "] has no secret for this project [" + k8sCluster.getClusterName() + "]");
    }
}
