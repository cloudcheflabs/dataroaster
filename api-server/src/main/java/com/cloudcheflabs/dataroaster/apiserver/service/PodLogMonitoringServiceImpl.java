package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.PodLogMonitoringService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.*;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.PodLogMonitoringHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class PodLogMonitoringServiceImpl implements PodLogMonitoringService {
    private static Logger LOG = LoggerFactory.getLogger(PodLogMonitoringServiceImpl.class);

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
    public void create(long projectId, long serviceDefId, long clusterId, String userName, List<String> elasticsearchHosts) {
        ServiceDef serviceDef = serviceDefDao.findOne(serviceDefId);
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        Project project = projectDao.findOne(projectId);

        String originalCreator = project.getUsers().getUserName();
        if(!userName.equals(originalCreator)) {
            throw new RuntimeException("user [" + userName + "] not allowed to update.");
        }

        // filebeat.
        K8sNamespace filebeatNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_FILEBEAT, k8sCluster.getId());
        if(filebeatNamespace == null) {
            filebeatNamespace = new K8sNamespace();
            filebeatNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_FILEBEAT);
            filebeatNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(filebeatNamespace);
        }
        else {
            throw new RuntimeException("filebeat already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // logstash namespace.
        K8sNamespace logstashNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_LOGSTASH, k8sCluster.getId());
        if(logstashNamespace == null) {
            logstashNamespace = new K8sNamespace();
            logstashNamespace.setNamespaceName(K8sNamespace.DEFAULT_NAMESPACE_LOGSTASH);
            logstashNamespace.setK8sCluster(k8sCluster);
            k8sNamespaceDao.create(logstashNamespace);
        }
        else {
            throw new RuntimeException("logstash already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // service.
        Set<Services> servicesSet = logstashNamespace.getServicesSet();
        if(servicesSet.size() == 0) {
            Services podLogMonitoringService = new Services();
            podLogMonitoringService.setServiceDef(serviceDef);
            // just indicate that the service will be installed in the cluster which logstash namespaces belong to
            podLogMonitoringService.setK8sNamespace(logstashNamespace);
            podLogMonitoringService.setProject(project);

            servicesDao.create(podLogMonitoringService);
        }
        else {
            throw new RuntimeException("pod log monitoring service already exists in this cluster [" + k8sCluster.getClusterName() + "]");
        }

        // build map.
        Map<String, Object> map = new HashMap<>();
        map.put("elasticsearchHosts", elasticsearchHosts);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return PodLogMonitoringHandler.create(kubeconfig, map);
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
        K8sNamespace filebeatNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_FILEBEAT, k8sCluster.getId());
        k8sNamespaceDao.delete(filebeatNamespace);
        K8sNamespace logstashNamespace = k8sNamespaceDao.findByNameAndClusterId(K8sNamespace.DEFAULT_NAMESPACE_LOGSTASH, k8sCluster.getId());
        k8sNamespaceDao.delete(logstashNamespace);

        // get kubeconfig.
        for(K8sKubeconfig k8sKubeconfig : k8sCluster.getK8sKubeconfigSet()) {
            if(k8sKubeconfig.getUsers().getUserName().equals(userName)) {
                Kubeconfig kubeconfig = secretDao.readSecret(k8sKubeconfig.getSecretPath(), Kubeconfig.class);
                ExecutorUtils.runTask(() -> {
                    return PodLogMonitoringHandler.delete(kubeconfig);
                });

                return;
            }
        }
        throw new RuntimeException("user [" + userName + "] has no secret for this project [" + k8sCluster.getClusterName() + "]");

    }
}
