package com.cloudcheflabs.dataroaster.cli.command;

import com.cloudcheflabs.dataroaster.cli.api.dao.*;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.cli.domain.ServiceDef;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

public class CommandUtils {

    public static int createProject(ConfigProps configProps,
                                    String name,
                                    String description) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ProjectDao projectDao = applicationContext.getBean(ProjectDao.class);
        RestResponse restResponse = projectDao.createProject(configProps, name, description);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("project created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createCluster(ConfigProps configProps,
                                    String name,
                                    String description) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);

        RestResponse restResponse = clusterDao.createCluster(configProps, name, description);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("cluster created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createKubeconfig(ConfigProps configProps,
                                       String clusterId,
                                       String kubeconfig) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        KubeconfigDao kubeconfigDao = applicationContext.getBean(KubeconfigDao.class);
        RestResponse restResponse = kubeconfigDao.createKubeconfig(configProps, Long.valueOf(clusterId), kubeconfig);
        if(restResponse.getStatusCode() == 200) {
            System.out.println("kubeconfig created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createIngressController(ConfigProps configProps,
                                              String projectId,
                                              String clusterId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.INGRESS_CONTROLLER.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }


        // create.
        IngressControllerDao ingressControllerDao = applicationContext.getBean(IngressControllerDao.class);
        restResponse = ingressControllerDao.createIngressController(
                configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("ingress controller service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int makeSureIngressHostRegistered(ConfigProps configProps,
                                                    String clusterId,
                                                    List<String> ingressHosts,
                                                    java.io.Console cnsl) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        IngressControllerDao ingressControllerDao = applicationContext.getBean(IngressControllerDao.class);

        int count = 0;
        int MAX = 20;
        String externalIp = null;
        for(int i = 0; i < MAX; i++) {
            RestResponse restResponse = ingressControllerDao.getExternalIpOfIngressControllerNginx(configProps, Long.valueOf(clusterId));

            // if response status code is not ok.
            if (restResponse.getStatusCode() != RestResponse.STATUS_OK) {
                count++;
                if(count == MAX) {
                    throw new RuntimeException(restResponse.getErrorMessage());
                } else {
                    System.out.println("service of ingress controller nginx is not yet ready...");
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {}
                }
            } else {
                externalIp = restResponse.getSuccessMessage();
                break;
            }
        }

        System.out.println("\n");
        System.out.printf("External IP Address of Ingress Controller NGINX Service: %s\n", externalIp);
        System.out.println("\n");
        if(ingressHosts != null) {
            for (String ingressHost : ingressHosts) {
                System.out.printf("Your ingress host: %s\n", ingressHost);
            }
        }
        System.out.println("\n");
        System.out.println("Before moving on, make sure that your Ingress Hosts have been registered \nwith the external IP Address of Ingress Controller NGINX Service to your public DNS server.");

        String yN = cnsl.readLine("Continue(y/N) : ");
        while(yN.equals("")) {
            System.err.println("y/N ?");
            yN = cnsl.readLine("Continue(y/N) : ");
            if(!yN.equals("")) {
                break;
            }
        }

        if(yN.toLowerCase().equals("n") || yN.toLowerCase().equals("no")) {
            System.err.println("blueprint cancelled...");
            return -1;
        } else if(yN.toLowerCase().equals("y") || yN.toLowerCase().equals("yes")) {
            System.out.println("ok...");
        }

        return 0;
    }

    public static int createBackup(ConfigProps configProps,
                                   String projectId,
                                   String clusterId,
                                   String s3Bucket,
                                   String s3AccessKey,
                                   String s3SecretKey,
                                   String s3Endpoint) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);
        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.BACKUP.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }

        // create.
        BackupDao backupDao = applicationContext.getBean(BackupDao.class);
        restResponse = backupDao.createBackup(
                configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                s3Bucket,
                s3AccessKey,
                s3SecretKey,
                s3Endpoint);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("backup service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createAnalytics(ConfigProps configProps,
                                      String projectId,
                                      String clusterId,
                                      String jupyterhubGithubClientId,
                                      String jupyterhubGithubClientSecret,
                                      String jupyterhubIngressHost,
                                      String storageClass,
                                      int jupyterhubStorageSize,
                                      int redashStorageSize) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.ANALYTICS.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }

        // create.
        AnalyticsDao analyticsDao = applicationContext.getBean(AnalyticsDao.class);
        restResponse = analyticsDao.createAnalytics(
                configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                jupyterhubGithubClientId,
                jupyterhubGithubClientSecret,
                jupyterhubIngressHost,
                storageClass,
                jupyterhubStorageSize,
                redashStorageSize);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("analytics service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createCiCd(ConfigProps configProps,
                          String projectId,
                          String clusterId,
                          String argocdIngressHost,
                          String jenkinsIngressHost,
                          String storageClass) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.CI_CD.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }

        // create.
        CiCdDao ciCdDao = applicationContext.getBean(CiCdDao.class);
        restResponse = ciCdDao.createCiCd(
                configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                argocdIngressHost,
                jenkinsIngressHost,
                storageClass);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("ci/cd service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createDataCatalog(ConfigProps configProps,
                                        String projectId,
                                        String clusterId,
                                        String s3Bucket,
                                        String s3AccessKey,
                                        String s3SecretKey,
                                        String s3Endpoint,
                                        String storageClass,
                                        int storageSize) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.DATA_CATALOG.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }


        // create.
        DataCatalogDao dataCatalogDao = applicationContext.getBean(DataCatalogDao.class);
        restResponse = dataCatalogDao.createDataCatalog(
                configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                s3Bucket,
                s3AccessKey,
                s3SecretKey,
                s3Endpoint,
                storageClass,
                storageSize);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("data catalog service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createDistributedTracing(ConfigProps configProps,
                                               String projectId,
                                               String clusterId,
                                               String storageClass,
                                               String ingressHost,
                                               String elasticsearchHostPort) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);
        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.DISTRIBUTED_TRACING.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }


        // create.
        DistributedTracingDao distributedTracingDao = applicationContext.getBean(DistributedTracingDao.class);
        restResponse = distributedTracingDao.createDistributedTracing(configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                storageClass,
                ingressHost,
                elasticsearchHostPort);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("distributed tracing service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createMetricsMonitoring(ConfigProps configProps,
                                              String projectId,
                                              String clusterId,
                                              String storageClass,
                                              String storageSize) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);
        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.METRICS_MONITORING.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }

        // create.
        MetricsMonitoringDao metricsMonitoringDao = applicationContext.getBean(MetricsMonitoringDao.class);
        restResponse = metricsMonitoringDao.createMetricsMonitoring(configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                storageClass,
                Integer.valueOf(storageSize));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("metrics monitoring service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createPodLogMonitoring(ConfigProps configProps,
                                             String projectId,
                                             String clusterId,
                                             String elasticsearchHosts) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);
        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.POD_LOG_MONITORING.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }

        // create.
        PodLogMonitoringDao podLogMonitoringDao = applicationContext.getBean(PodLogMonitoringDao.class);
        restResponse = podLogMonitoringDao.createPodLogMonitoring(configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                elasticsearchHosts);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("pod log monitoring service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createPrivateRegistry(ConfigProps configProps,
                                            String projectId,
                                            String clusterId,
                                            String coreHost,
                                            String notaryHost,
                                            String storageClass,
                                            int registryStorageSize,
                                            int chartmuseumStorageSize,
                                            int jobserviceStorageSize,
                                            int databaseStorageSize,
                                            int redisStorageSize,
                                            int trivyStorageSize,
                                            String s3Bucket,
                                            String s3AccessKey,
                                            String s3SecretKey,
                                            String s3Endpoint) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);
        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.PRIVATE_REGISTRY.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }

        // create.
        PrivateRegistryDao privateRegistryDao = applicationContext.getBean(PrivateRegistryDao.class);
        restResponse = privateRegistryDao.createPrivateRegistry(
                configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                coreHost,
                notaryHost,
                storageClass,
                registryStorageSize,
                chartmuseumStorageSize,
                jobserviceStorageSize,
                databaseStorageSize,
                redisStorageSize,
                trivyStorageSize,
                s3Bucket,
                s3AccessKey,
                s3SecretKey,
                s3Endpoint);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("private registry service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createQueryEngine(ConfigProps configProps,
                                        String projectId,
                                        String clusterId,
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
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.QUERY_ENGINE.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }

        // create.
        QueryEngineDao queryEngineDao = applicationContext.getBean(QueryEngineDao.class);
        restResponse = queryEngineDao.createQueryEngine(
                configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                s3Bucket,
                s3AccessKey,
                s3SecretKey,
                s3Endpoint,
                sparkThriftServerStorageClass,
                sparkThriftServerExecutors,
                sparkThriftServerExecutorMemory,
                sparkThriftServerExecutorCores,
                sparkThriftServerDriverMemory,
                trinoWorkers,
                trinoServerMaxMemory,
                trinoCores,
                trinoTempDataStorage,
                trinoDataStorage,
                trinoStorageClass);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("query engine service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createStreaming(ConfigProps configProps,
                                      String projectId,
                                      String clusterId,
                                      int kafkaReplicaCount,
                                      int kafkaStorageSize,
                                      String storageClass,
                                      int zkReplicaCount) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.STREAMING.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }

        // create.
        StreamingDao streamingDao = applicationContext.getBean(StreamingDao.class);
        restResponse = streamingDao.createStreaming(
                configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                kafkaReplicaCount,
                kafkaStorageSize,
                storageClass,
                zkReplicaCount);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("streaming service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createWorkflow(ConfigProps configProps,
                                     String projectId,
                                     String clusterId,
                                     String storageClass,
                                     int storageSize,
                                     String s3Bucket,
                                     String s3AccessKey,
                                     String s3SecretKey,
                                     String s3Endpoint) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.WORKFLOW.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }

        // create.
        WorkflowDao workflowDao = applicationContext.getBean(WorkflowDao.class);
        restResponse = workflowDao.createWorkflow(
                configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                storageClass,
                storageSize,
                s3Bucket,
                s3AccessKey,
                s3SecretKey,
                s3Endpoint);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("workflow service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static String getServiceId(List<Map<String, Object>> servicesList,
                                    String projectName,
                                    String clusterName,
                                    String serviceType) {
        for(Map<String, Object> map : servicesList) {
            String serviceDefType = (String) map.get("serviceDefType");
            if(serviceDefType.equals(serviceType)) {
                String tempProjectName = (String) map.get("projectName");
                String tempCluserName = (String) map.get("clusterName");
                if(projectName.equals(tempProjectName) && clusterName.equals(tempCluserName)) {
                    return String.valueOf(map.get("id"));
                }
            }
        }
        return null;
    }

    public static int deleteIngressController(ConfigProps configProps,
                                              String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        IngressControllerDao ingressControllerDao = applicationContext.getBean(IngressControllerDao.class);
        RestResponse restResponse = ingressControllerDao.deleteIngressController(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("ingress controller service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deleteBackup(ConfigProps configProps,
                                   String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        BackupDao backupDao = applicationContext.getBean(BackupDao.class);
        RestResponse restResponse = backupDao.deleteBackup(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("backup service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deleteAnalytics(ConfigProps configProps,
                                      String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        AnalyticsDao analyticsDao = applicationContext.getBean(AnalyticsDao.class);
        RestResponse restResponse = analyticsDao.deleteAnalytics(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("analytics service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deleteCiCd(ConfigProps configProps,
                                 String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        CiCdDao ciCdDao = applicationContext.getBean(CiCdDao.class);
        RestResponse restResponse = ciCdDao.deleteCiCd(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("ci/cd service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deleteDataCatalog(ConfigProps configProps,
                                        String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        DataCatalogDao dataCatalogDao = applicationContext.getBean(DataCatalogDao.class);
        RestResponse restResponse = dataCatalogDao.deleteDataCatalog(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("data catalog service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deleteDistributedTracing(ConfigProps configProps,
                                               String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        DistributedTracingDao distributedTracingDao = applicationContext.getBean(DistributedTracingDao.class);
        RestResponse restResponse = distributedTracingDao.deleteDistributedTracing(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("distributed tracing service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deleteMetricsMonitoring(ConfigProps configProps,
                                              String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        MetricsMonitoringDao metricsMonitoringDao = applicationContext.getBean(MetricsMonitoringDao.class);
        RestResponse restResponse = metricsMonitoringDao.deleteMetricsMonitoring(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("metrics monitoring service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deletePodLogMonitoring(ConfigProps configProps,
                                             String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        PodLogMonitoringDao podLogMonitoringDao = applicationContext.getBean(PodLogMonitoringDao.class);
        RestResponse restResponse = podLogMonitoringDao.deletePodLogMonitoring(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("pod log monitoring service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deletePrivateRegistry(ConfigProps configProps,
                                            String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        PrivateRegistryDao privateRegistryDao = applicationContext.getBean(PrivateRegistryDao.class);
        RestResponse restResponse = privateRegistryDao.deletePrivateRegistry(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("private registry service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deleteQueryEngine(ConfigProps configProps,
                                        String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        QueryEngineDao queryEngineDao = applicationContext.getBean(QueryEngineDao.class);
        RestResponse restResponse = queryEngineDao.deleteQueryEngine(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("query engine service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deleteStreaming(ConfigProps configProps,
                                      String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        StreamingDao streamingDao = applicationContext.getBean(StreamingDao.class);
        RestResponse restResponse = streamingDao.deleteStreaming(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("streaming service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deleteWorkflow(ConfigProps configProps,
                                     String serviceId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        WorkflowDao workflowDao = applicationContext.getBean(WorkflowDao.class);
        RestResponse restResponse = workflowDao.deleteWorkflow(configProps, Long.valueOf(serviceId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("workflow service deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static String getProjectId(ConfigProps configProps, String projectName) {
        // show project list.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ProjectDao projectDao = applicationContext.getBean(ProjectDao.class);
        RestResponse restResponse = projectDao.listProjects(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> projectLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        for(Map<String, Object> map : projectLists) {
            String tempProjectName = (String) map.get("name");
            if(tempProjectName.equals(projectName)) {
                return String.valueOf(map.get("id"));
            }
        }

        return null;
    }

    public static String getClusterId(ConfigProps configProps, String clusterName) {
        // show cluster list.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);
        RestResponse restResponse = clusterDao.listClusters(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> clusterLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        for(Map<String, Object> map : clusterLists) {
            String tempClusterName = (String) map.get("name");
            if(tempClusterName.equals(clusterName)) {
                return String.valueOf(map.get("id"));
            }
        }

        return null;
    }

    public static int deleteProject(ConfigProps configProps,
                                    String projectId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ProjectDao projectDao = applicationContext.getBean(ProjectDao.class);
        RestResponse restResponse = projectDao.deleteProject(configProps, Long.valueOf(projectId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("project deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int deleteCluster(ConfigProps configProps,
                                    String clusterId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);
        RestResponse restResponse = clusterDao.deleteCluster(configProps, Long.valueOf(clusterId));
        if(restResponse.getStatusCode() == 200) {
            System.out.println("cluster deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static String getClusterIdByPrompt(java.io.Console cnsl) {
        String clusterId = cnsl.readLine("Select Cluster ID : ");
        while(clusterId.equals("")) {
            System.err.println("cluster id is required!");
            clusterId = cnsl.readLine("Select Cluster ID : ");
            if(!clusterId.equals("")) {
                break;
            }
        }

        return clusterId;
    }

    public static String getProjectIdByPrompt(java.io.Console cnsl) {
        String projectId = cnsl.readLine("Select Project ID : ");
        while(projectId.equals("")) {
            System.err.println("project id is required!");
            projectId = cnsl.readLine("Select Project ID : ");
            if(!projectId.equals("")) {
                break;
            }
        }

        return projectId;
    }

    public static String getStorageClassByPrompt(java.io.Console cnsl) {
        String storageClass = cnsl.readLine("Select Storage Class : ");
        while(storageClass.equals("")) {
            System.err.println("storage class is required!");
            storageClass = cnsl.readLine("Select Storage Class : ");
            if(!storageClass.equals("")) {
                break;
            }
        }

        return storageClass;
    }

    public static void showProjectList(ConfigProps configProps) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ProjectDao projectDao = applicationContext.getBean(ProjectDao.class);
        RestResponse restResponse = projectDao.listProjects(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> projectLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String format = "%-20s%-20s%-20s%n";

        System.out.printf(format,"PROJECT ID", "PROJECT NAME", "PROJECT DESCRIPTION");
        for(Map<String, Object> map : projectLists) {
            System.out.printf(format, String.valueOf(map.get("id")), (String) map.get("name"), (String) map.get("description"));
        }
    }

    public static void showClusterList(ConfigProps configProps) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);
        RestResponse restResponse = clusterDao.listClusters(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> clusterLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String format = "%-20s%-20s%-20s%n";

        System.out.printf(format,"CLUSTER ID", "CLUSTER NAME", "CLUSTER DESCRIPTION");
        for(Map<String, Object> map : clusterLists) {
            System.out.printf(format, String.valueOf(map.get("id")), (String) map.get("name"), (String) map.get("description"));
        }
    }

    public static void showStorageClasses(ConfigProps configProps, String clusterId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ResourceControlDao resourceControlDao = applicationContext.getBean(ResourceControlDao.class);
        RestResponse restResponse = resourceControlDao.listStorageClasses(configProps, Long.valueOf(clusterId));

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> storageClasses =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String format = "%-20s%-20s%-20s%-20s%n";

        System.out.printf(format,"STORAGE CLASS NAME", "RECLAIM POLICY", "VOLUME BIDING MODE", "PROVISIONER");
        for(Map<String, Object> map : storageClasses) {
            System.out.printf(format,
                    String.valueOf(map.get("name")),
                    (String) map.get("reclaimPolicy"),
                    (String) map.get("volumeBindingMode"),
                    (String) map.get("provisioner"));
        }
    }
}
