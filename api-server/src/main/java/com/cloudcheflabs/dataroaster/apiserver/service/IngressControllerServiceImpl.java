package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.IngressControllerService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.*;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.IngressControllerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class IngressControllerServiceImpl implements IngressControllerService {
    private static Logger LOG = LoggerFactory.getLogger(IngressControllerServiceImpl.class);

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
    public void create(long projectId, long serviceDefId, long clusterId, String userName) {
        ServiceDef serviceDef = serviceDefDao.findOne(serviceDefId);
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        Project project = projectDao.findOne(projectId);

        String originalCreator = project.getUsers().getUserName();
        if(!userName.equals(originalCreator)) {
            throw new RuntimeException("user [" + userName + "] not allowed to update.");
        }

        // cert manager namespace.
        K8sNamespace certManagerNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_CERT_MANAGER, k8sCluster.getId());
        if(certManagerNamespace == null) {
            certManagerNamespace = new K8sNamespace();
            certManagerNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_CERT_MANAGER);
            certManagerNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(certManagerNamespace);
        }
        else {
            throw new RuntimeException("cert manager already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // ingress nginx namespace.
        K8sNamespace ingressNginxNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_INGRESS_CONTROLLER_NGINX, k8sCluster.getId());
        if(ingressNginxNamespace == null) {
            ingressNginxNamespace = new K8sNamespace();
            ingressNginxNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_INGRESS_CONTROLLER_NGINX);
            ingressNginxNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(ingressNginxNamespace);
        }
        else {
            throw new RuntimeException("ingress controller nginx already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // service.
        Set<Services> servicesSet = ingressNginxNamespace.getServicesSet();
        if(servicesSet.size() == 0) {
            Services ingressControllerService = new Services();
            ingressControllerService.setServiceDef(serviceDef);
            // just indicate that the service will be installed in the cluster which the ingress controller namespaces belong to
            ingressControllerService.setK8sNamespace(ingressNginxNamespace);
            ingressControllerService.setProject(project);

            servicesDao.create(ingressControllerService);
        }
        else {
            throw new RuntimeException("ingress controller service already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return IngressControllerHandler.create(kubeconfig);
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
        K8sNamespace certManagerNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_CERT_MANAGER, k8sCluster.getId());
        k8sNamespaceDao.delete(certManagerNamespace);
        K8sNamespace ingressNginxNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_INGRESS_CONTROLLER_NGINX, k8sCluster.getId());
        k8sNamespaceDao.delete(ingressNginxNamespace);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return IngressControllerHandler.delete(kubeconfig);
                });

                return;
            }
        }
        throw new RuntimeException("user [" + userName + "] has no secret for this project [" + k8sCluster.getClusterName() + "]");

    }
}
