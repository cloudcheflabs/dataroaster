package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.QueryEngineService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.*;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.QueryEngineHandler;
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
public class QueryEngineServiceImpl implements QueryEngineService {
    private static Logger LOG = LoggerFactory.getLogger(QueryEngineServiceImpl.class);

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
                       String sparkThriftServerStorageClass,
                       int sparkThriftServerExecutors,
                       int sparkThriftServerExecutorMemory,
                       int sparkThriftServerExecutorCores,
                       int sparkThriftServerDriverMemory,
                       int trinoWorkers,
                       int trinoServerMaxMemory,
                       int trinoCores,
                       int trinoTempDataStorage,
                       int trinoDataStorage,
                       String trinoStorageClass) {
        ServiceDef serviceDef = serviceDefDao.findOne(serviceDefId);
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        Project project = projectDao.findOne(projectId);

        String originalCreator = project.getUsers().getUserName();
        if(!userName.equals(originalCreator)) {
            throw new RuntimeException("user [" + userName + "] not allowed to update.");
        }

        // spark thrift server.
        K8sNamespace sparkThriftServerNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_SPARK_THRIFT_SERVER, k8sCluster.getId());
        if(sparkThriftServerNamespace == null) {
            sparkThriftServerNamespace = new K8sNamespace();
            sparkThriftServerNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_SPARK_THRIFT_SERVER);
            sparkThriftServerNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(sparkThriftServerNamespace);
        }
        else {
            throw new RuntimeException("spark thrift server already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // trino.
        K8sNamespace trinoNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_TRINO, k8sCluster.getId());
        if(trinoNamespace == null) {
            trinoNamespace = new K8sNamespace();
            trinoNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_TRINO);
            trinoNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(trinoNamespace);
        }
        else {
            throw new RuntimeException("trino already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }


        // service.
        Set<Services> servicesSet = sparkThriftServerNamespace.getServicesSet();
        if(servicesSet.size() == 0) {
            Services queryEngineService = new Services();
            queryEngineService.setServiceDef(serviceDef);
            // just indicate that the service will be installed in the cluster which spark thrift server namespace belongs to
            queryEngineService.setK8sNamespace(sparkThriftServerNamespace);
            queryEngineService.setProject(project);

            servicesDao.create(queryEngineService);
        }
        else {
            throw new RuntimeException("query engine service already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // build map.
        Map<String, Object> map = new HashMap<>();
        map.put("s3Bucket", s3Bucket);
        map.put("s3AccessKey", s3AccessKey);
        map.put("s3SecretKey", s3SecretKey);
        map.put("s3Endpoint", s3Endpoint);
        map.put("sparkThriftServerStorageClass", sparkThriftServerStorageClass);
        map.put("sparkThriftServerExecutors", sparkThriftServerExecutors);
        map.put("sparkThriftServerExecutorMemory", sparkThriftServerExecutorMemory);
        map.put("sparkThriftServerExecutorCores", sparkThriftServerExecutorCores);
        map.put("sparkThriftServerDriverMemory", sparkThriftServerDriverMemory);
        map.put("trinoWorkers", trinoWorkers);
        map.put("trinoServerMaxMemory", trinoServerMaxMemory);
        map.put("trinoCores", trinoCores);
        map.put("trinoTempDataStorage", trinoTempDataStorage);
        map.put("trinoDataStorage", trinoDataStorage);
        map.put("trinoStorageClass", trinoStorageClass);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return QueryEngineHandler.create(kubeconfig, map);
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
        K8sNamespace sparkThriftServerNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_SPARK_THRIFT_SERVER, k8sCluster.getId());
        k8sNamespaceDao.delete(sparkThriftServerNamespace);
        K8sNamespace trinoNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_TRINO, k8sCluster.getId());
        k8sNamespaceDao.delete(trinoNamespace);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return QueryEngineHandler.delete(kubeconfig);
                });

                return;
            }
        }
        throw new RuntimeException("user [" + userName + "] has no secret for this project [" + k8sCluster.getClusterName() + "]");
    }
}
