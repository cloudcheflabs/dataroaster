package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.AnalyticsService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.*;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.AnalyticsHandler;
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
public class AnalyticsServiceImpl implements AnalyticsService {
    private static Logger LOG = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

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
                       String jupyterhubGithubClientId,
                       String jupyterhubGithubClientSecret,
                       String jupyterhubIngressHost,
                       String storageClass,
                       int jupyterhubStorageSize,
                       int redashStorageSize) {
        ServiceDef serviceDef = serviceDefDao.findOne(serviceDefId);
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        Project project = projectDao.findOne(projectId);

        String originalCreator = project.getUsers().getUserName();
        if(!userName.equals(originalCreator)) {
            throw new RuntimeException("user [" + userName + "] not allowed to update.");
        }

        // jupyterhub.
        K8sNamespace jupyterhubNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_JUPYTERHUB, k8sCluster.getId());
        if(jupyterhubNamespace == null) {
            jupyterhubNamespace = new K8sNamespace();
            jupyterhubNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_JUPYTERHUB);
            jupyterhubNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(jupyterhubNamespace);
        }
        else {
            throw new RuntimeException("jupyterhub already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // redash.
        K8sNamespace redashNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_REDASH, k8sCluster.getId());
        if(redashNamespace == null) {
            redashNamespace = new K8sNamespace();
            redashNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_REDASH);
            redashNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(redashNamespace);
        }
        else {
            throw new RuntimeException("redash already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }


        // service.
        Set<Services> servicesSet = jupyterhubNamespace.getServicesSet();
        if(servicesSet.size() == 0) {
            Services analyticsService = new Services();
            analyticsService.setServiceDef(serviceDef);
            // just indicate that the service will be installed in the cluster which jupyterhub namespace belongs to
            analyticsService.setK8sNamespace(jupyterhubNamespace);
            analyticsService.setProject(project);

            servicesDao.create(analyticsService);
        }
        else {
            throw new RuntimeException("analytics service already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // build map.
        Map<String, Object> map = new HashMap<>();
        map.put("jupyterhubGithubClientId", jupyterhubGithubClientId);
        map.put("jupyterhubGithubClientSecret", jupyterhubGithubClientSecret);
        map.put("jupyterhubIngressHost", jupyterhubIngressHost);
        map.put("storageClass", storageClass);
        map.put("jupyterhubStorageSize", jupyterhubStorageSize);
        map.put("redashStorageSize", redashStorageSize);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return AnalyticsHandler.create(kubeconfig, map);
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
        K8sNamespace jupyterhubNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_JUPYTERHUB, k8sCluster.getId());
        k8sNamespaceDao.delete(jupyterhubNamespace);
        K8sNamespace redashNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_REDASH, k8sCluster.getId());
        k8sNamespaceDao.delete(redashNamespace);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return AnalyticsHandler.delete(kubeconfig);
                });

                return;
            }
        }
        throw new RuntimeException("user [" + userName + "] has no secret for this project [" + k8sCluster.getClusterName() + "]");
    }
}
