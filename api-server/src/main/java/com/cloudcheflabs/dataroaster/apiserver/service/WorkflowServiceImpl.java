package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.WorkflowService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.*;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.WorkflowHandler;
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
public class WorkflowServiceImpl implements WorkflowService {
    private static Logger LOG = LoggerFactory.getLogger(WorkflowServiceImpl.class);

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
                       String storageClass,
                       int storageSize,
                       String s3Bucket,
                       String s3AccessKey,
                       String s3SecretKey,
                       String s3Endpoint) {
        ServiceDef serviceDef = serviceDefDao.findOne(serviceDefId);
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        Project project = projectDao.findOne(projectId);

        String originalCreator = project.getUsers().getUserName();
        if(!userName.equals(originalCreator)) {
            throw new RuntimeException("user [" + userName + "] not allowed to update.");
        }

        // argo workflow.
        K8sNamespace argoWorkflowNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_ARGO_WORKFLOW, k8sCluster.getId());
        if(argoWorkflowNamespace == null) {
            argoWorkflowNamespace = new K8sNamespace();
            argoWorkflowNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_ARGO_WORKFLOW);
            argoWorkflowNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(argoWorkflowNamespace);
        }
        else {
            throw new RuntimeException("argo workflow already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // service.
        Set<Services> servicesSet = argoWorkflowNamespace.getServicesSet();
        if(servicesSet.size() == 0) {
            Services workflowService = new Services();
            workflowService.setServiceDef(serviceDef);
            // just indicate that the service will be installed in the cluster which argo workflow namespace belongs to
            workflowService.setK8sNamespace(argoWorkflowNamespace);
            workflowService.setProject(project);

            servicesDao.create(workflowService);
        }
        else {
            throw new RuntimeException("workflow service already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // build map.
        Map<String, Object> map = new HashMap<>();
        map.put("storageClass", storageClass);
        map.put("storageSize", storageSize);
        map.put("s3Bucket", s3Bucket);
        map.put("s3AccessKey", s3AccessKey);
        map.put("s3SecretKey", s3SecretKey);
        map.put("s3Endpoint", s3Endpoint);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return WorkflowHandler.create(kubeconfig, map);
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
        K8sNamespace argoWorkflowNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_ARGO_WORKFLOW, k8sCluster.getId());
        k8sNamespaceDao.delete(argoWorkflowNamespace);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return WorkflowHandler.delete(kubeconfig);
                });

                return;
            }
        }
        throw new RuntimeException("user [" + userName + "] has no secret for this project [" + k8sCluster.getClusterName() + "]");
    }
}
