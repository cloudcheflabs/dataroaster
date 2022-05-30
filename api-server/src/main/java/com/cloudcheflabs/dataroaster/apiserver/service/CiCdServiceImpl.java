package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.CiCdService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.*;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.CiCdHandler;
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
public class CiCdServiceImpl implements CiCdService {
    private static Logger LOG = LoggerFactory.getLogger(CiCdServiceImpl.class);

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
                       String argocdIngressHost,
                       String jenkinsIngressHost,
                       String storageClass) {
        ServiceDef serviceDef = serviceDefDao.findOne(serviceDefId);
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        Project project = projectDao.findOne(projectId);

        String originalCreator = project.getUsers().getUserName();
        if(!userName.equals(originalCreator)) {
            throw new RuntimeException("user [" + userName + "] not allowed to update.");
        }

        // argocd.
        K8sNamespace argocdNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_ARGOCD, k8sCluster.getId());
        if(argocdNamespace == null) {
            argocdNamespace = new K8sNamespace();
            argocdNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_ARGOCD);
            argocdNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(argocdNamespace);
        }
        else {
            throw new RuntimeException("argocd already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // jenkins namespace.
        K8sNamespace jenkinsNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_JENKINS, k8sCluster.getId());
        if(jenkinsNamespace == null) {
            jenkinsNamespace = new K8sNamespace();
            jenkinsNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_JENKINS);
            jenkinsNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(jenkinsNamespace);
        }
        else {
            throw new RuntimeException("jenkins already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // service.
        Set<Services> servicesSet = jenkinsNamespace.getServicesSet();
        if(servicesSet.size() == 0) {
            Services ciCdService = new Services();
            ciCdService.setServiceDef(serviceDef);
            // just indicate that the service will be installed in the cluster which jenkins namespaces belong to
            ciCdService.setK8sNamespace(jenkinsNamespace);
            ciCdService.setProject(project);

            servicesDao.create(ciCdService);
        }
        else {
            throw new RuntimeException("ci cd service already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // build map.
        Map<String, Object> map = new HashMap<>();
        map.put("argocdIngressHost", argocdIngressHost);
        map.put("jenkinsIngressHost", jenkinsIngressHost);
        map.put("storageClass", storageClass);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return CiCdHandler.create(kubeconfig, map);
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
        K8sNamespace argocdNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_ARGOCD, k8sCluster.getId());
        k8sNamespaceDao.delete(argocdNamespace);
        K8sNamespace jenkinsNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_JENKINS, k8sCluster.getId());
        k8sNamespaceDao.delete(jenkinsNamespace);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return CiCdHandler.delete(kubeconfig);
                });

                return;
            }
        }
        throw new RuntimeException("user [" + userName + "] has no secret for this project [" + k8sCluster.getClusterName() + "]");

    }
}
