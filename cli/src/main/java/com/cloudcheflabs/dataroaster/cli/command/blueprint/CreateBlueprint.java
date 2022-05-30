package com.cloudcheflabs.dataroaster.cli.command.blueprint;

import com.cloudcheflabs.dataroaster.cli.api.dao.ClusterDao;
import com.cloudcheflabs.dataroaster.cli.api.dao.ProjectDao;
import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.BlueprintGraph;
import com.cloudcheflabs.dataroaster.cli.domain.CLIConstants;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Blueprint Deployment.")
public class CreateBlueprint implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Blueprint parent;

    @CommandLine.Option(names = {"--blueprint"}, description = "Blueprint Yaml File Path.", required = true)
    private File blueprintFile;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        java.io.Console cnsl = System.console();
        if (cnsl == null) {
            System.out.println("No console available");
            return -1;
        }


        // read blueprint yaml.
        String blueprintFileAbsolutePath = blueprintFile.getAbsolutePath();
        String blueprint = FileUtils.fileToString(blueprintFileAbsolutePath, false);

        // parset blueprint yaml.
        BlueprintGraph blueprintGraph = BlueprintUtils.parseBlueprintYaml(blueprint);

        // project.
        BlueprintGraph.Project project = blueprintGraph.getProject();

        // cluster.
        BlueprintGraph.Cluster cluster = blueprintGraph.getCluster();

        // service list with regard to dependencies.
        List<BlueprintGraph.Service> serviceDependencyList = blueprintGraph.getServiceDependencyList();

        // create project.
        System.out.println("creating project...");
        int ret = CommandUtils.createProject(configProps, project.getName(), project.getDescription());
        if(ret != 0) {
            throw new RuntimeException("error with creating project.");
        }

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

        String projectId = null;
        for(Map<String, Object> map : projectLists) {
            String tempProjectId = String.valueOf(map.get("id"));
            String projectName = (String) map.get("name");
            if(projectName.equals(project.getName())) {
                projectId = tempProjectId;
                System.out.printf("project id [%s] obtained\n", projectId);
                break;
            }
        }

        // create cluster.
        System.out.println("creating cluster...");
        ret = CommandUtils.createCluster(configProps, cluster.getName(), cluster.getDescription());
        if(ret != 0) {
            throw new RuntimeException("error with creating cluster.");
        }

        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);
        restResponse = clusterDao.listClusters(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> clusterLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String clusterId = null;
        for(Map<String, Object> map : clusterLists) {
            String tempClusterId = String.valueOf(map.get("id"));
            String clusterName = (String) map.get("name");
            if(clusterName.equals(cluster.getName())) {
                clusterId = tempClusterId;
                System.out.printf("cluster id [%s] obtained\n", clusterId);
                break;
            }
        }

        // register kubeconfig.
        System.out.println("upload kubeconfig...");
        File kubeconfigFile = new File(cluster.getKubeconfig());
        String kubeconfigPath = kubeconfigFile.getAbsolutePath();
        String kubeconfig = FileUtils.fileToString(kubeconfigPath, false);
        ret = CommandUtils.createKubeconfig(configProps, clusterId, kubeconfig);
        if(ret != 0) {
            throw new RuntimeException("error with uploading kubeconfig.");
        }


        // create services.
        for(BlueprintGraph.Service service : serviceDependencyList) {
            String serviceName = service.getName();
            String depends = service.getDepends();
            boolean dependsOnIngressController = (depends != null) ? depends.equals(CLIConstants.SERVICE_INGRESS_CONTROLLER) : false;
            // ingress controller.
            if(serviceName.equals(CLIConstants.SERVICE_INGRESS_CONTROLLER)) {
                System.out.println("creating ingress controller...");
                ret = CommandUtils.createIngressController(configProps, projectId, clusterId);
                if(ret != 0) {
                    throw new RuntimeException("error with creating ingress controller.");
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_BACKUP)) {
                // params.
                ConcurrentHashMap<String, Object> params = service.getParams();
                String s3Bucket = (String) params.get("s3-bucket");
                String s3AccessKey = (String) params.get("s3-access-key");
                String s3SecretKey = (String) params.get("s3-secret-key");
                String s3Endpoint = (String) params.get("s3-endpoint");

                // show external ip of ingress controller nginx to register ingress host with the external ip of it
                // to public dns server.
                if(dependsOnIngressController) {
                    ret = CommandUtils.makeSureIngressHostRegistered(configProps, clusterId, null, cnsl);
                    if(ret != 0) {
                        throw new RuntimeException("error with registering ingress host.");
                    }
                }

                System.out.println("creating backup...");
                ret = CommandUtils.createBackup(
                        configProps,
                        projectId,
                        clusterId,
                        s3Bucket,
                        s3AccessKey,
                        s3SecretKey,
                        s3Endpoint);
                if(ret != 0) {
                    throw new RuntimeException("error with creating backup.");
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_ANALYTICS)) {

                // params.
                ConcurrentHashMap<String, Object> params = service.getParams();
                String jupyterhubGithubClientId = (String) params.get("jupyterhub-github-client-id");
                String jupyterhubGithubClientSecret = (String) params.get("jupyterhub-github-client-secret");
                String jupyterhubIngressHost = (String) params.get("jupyterhub-ingress-host");
                String jupyterhubStorageSize = String.valueOf(params.get("jupyterhub-storage-size"));
                String redashStorageSize = String.valueOf(params.get("redash-storage-size"));

                // extra params.
                ConcurrentHashMap<String, Object> extraParams = service.getExtraParams();
                String storageClass = (String) extraParams.get("storage-class");

                // show external ip of ingress controller nginx to register ingress host with the external ip of it
                // to public dns server.
                if(dependsOnIngressController) {
                    ret = CommandUtils.makeSureIngressHostRegistered(configProps, clusterId, Arrays.asList(jupyterhubIngressHost), cnsl);
                    if(ret != 0) {
                        throw new RuntimeException("error with registering ingress host.");
                    }
                }

                System.out.println("creating analytics...");
                ret = CommandUtils.createAnalytics(
                        configProps,
                        projectId,
                        clusterId,
                        jupyterhubGithubClientId,
                        jupyterhubGithubClientSecret,
                        jupyterhubIngressHost,
                        storageClass,
                        Integer.valueOf(jupyterhubStorageSize),
                        Integer.valueOf(redashStorageSize));
                if(ret != 0) {
                    throw new RuntimeException("error with creating analytics.");
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_CICD)) {

                // params.
                ConcurrentHashMap<String, Object> params = service.getParams();
                String argocdIngressHost = (String) params.get("argocd-ingress-host");
                String jenkinsIngressHost = (String) params.get("jenkins-ingress-host");

                // extra params.
                ConcurrentHashMap<String, Object> extraParams = service.getExtraParams();
                String storageClass = (String) extraParams.get("storage-class");

                // show external ip of ingress controller nginx to register ingress host with the external ip of it
                // to public dns server.
                if(dependsOnIngressController) {
                    ret = CommandUtils.makeSureIngressHostRegistered(configProps, clusterId, Arrays.asList(argocdIngressHost, jenkinsIngressHost), cnsl);
                    if(ret != 0) {
                        throw new RuntimeException("error with registering ingress host.");
                    }
                }

                System.out.println("creating cicd...");
                ret = CommandUtils.createCiCd(
                        configProps,
                        projectId,
                        clusterId,
                        argocdIngressHost,
                        jenkinsIngressHost,
                        storageClass);
                if(ret != 0) {
                    throw new RuntimeException("error with creating cicd.");
                }

            } else if(serviceName.equals(CLIConstants.SERVICE_DATA_CATALOG)) {

                // params.
                ConcurrentHashMap<String, Object> params = service.getParams();
                String s3Bucket = (String) params.get("s3-bucket");
                String s3AccessKey = (String) params.get("s3-access-key");
                String s3SecretKey = (String) params.get("s3-secret-key");
                String s3Endpoint = (String) params.get("s3-endpoint");
                String storageSize = String.valueOf(params.get("storage-size"));

                // extra params.
                ConcurrentHashMap<String, Object> extraParams = service.getExtraParams();
                String storageClass = (String) extraParams.get("storage-class");

                // show external ip of ingress controller nginx to register ingress host with the external ip of it
                // to public dns server.
                if(dependsOnIngressController) {
                    ret = CommandUtils.makeSureIngressHostRegistered(configProps, clusterId, null, cnsl);
                    if(ret != 0) {
                        throw new RuntimeException("error with registering ingress host.");
                    }
                }

                System.out.println("creating data catalog...");
                ret = CommandUtils.createDataCatalog(
                        configProps,
                        projectId,
                        clusterId,
                        s3Bucket,
                        s3AccessKey,
                        s3SecretKey,
                        s3Endpoint,
                        storageClass,
                        Integer.valueOf(storageSize));

                if(ret != 0) {
                    throw new RuntimeException("error with creating data catalog.");
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_DISTRIBUTED_TRACING)) {

                // params.
                ConcurrentHashMap<String, Object> params = service.getParams();
                String ingressHost = (String) params.get("ingress-host");
                String elasticsearchHostPort = (String) params.get("elasticsearch-host-port");

                // extra params.
                ConcurrentHashMap<String, Object> extraParams = service.getExtraParams();
                String storageClass = (String) extraParams.get("storage-class");

                // show external ip of ingress controller nginx to register ingress host with the external ip of it
                // to public dns server.
                if(dependsOnIngressController) {
                    ret = CommandUtils.makeSureIngressHostRegistered(configProps, clusterId, Arrays.asList(ingressHost), cnsl);
                    if(ret != 0) {
                        throw new RuntimeException("error with registering ingress host.");
                    }
                }

                System.out.println("creating distributed tracing...");
                ret = CommandUtils.createDistributedTracing(
                        configProps,
                        projectId,
                        clusterId,
                        storageClass,
                        ingressHost,
                        elasticsearchHostPort);
                if(ret != 0) {
                    throw new RuntimeException("error with creating distributed tracing.");
                }

            } else if(serviceName.equals(CLIConstants.SERVICE_METRICS_MONITORING)) {
                // params.
                ConcurrentHashMap<String, Object> params = service.getParams();
                String storageSize = String.valueOf(params.get("storage-size"));

                // extra params.
                ConcurrentHashMap<String, Object> extraParams = service.getExtraParams();
                String storageClass = (String) extraParams.get("storage-class");

                // show external ip of ingress controller nginx to register ingress host with the external ip of it
                // to public dns server.
                if(dependsOnIngressController) {
                    ret = CommandUtils.makeSureIngressHostRegistered(configProps, clusterId, null, cnsl);
                    if(ret != 0) {
                        throw new RuntimeException("error with registering ingress host.");
                    }
                }

                System.out.println("creating metrics monitoring...");
                ret = CommandUtils.createMetricsMonitoring(
                        configProps,
                        projectId,
                        clusterId,
                        storageClass,
                        storageSize);
                if(ret != 0) {
                    throw new RuntimeException("error with creating metrics monitoring.");
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_POD_LOG_MONITORING)) {

                // params.
                ConcurrentHashMap<String, Object> params = service.getParams();
                String elasticsearchHosts = (String) params.get("elasticsearch-hosts");

                // show external ip of ingress controller nginx to register ingress host with the external ip of it
                // to public dns server.
                if(dependsOnIngressController) {
                    ret = CommandUtils.makeSureIngressHostRegistered(configProps, clusterId, null, cnsl);
                    if(ret != 0) {
                        throw new RuntimeException("error with registering ingress host.");
                    }
                }

                System.out.println("creating pod log monitoring...");
                ret = CommandUtils.createPodLogMonitoring(
                        configProps,
                        projectId,
                        clusterId,
                        elasticsearchHosts);
                if(ret != 0) {
                    throw new RuntimeException("error with creating pod log monitoring.");
                }

            } else if(serviceName.equals(CLIConstants.SERVICE_PRIVATE_REGISTRY)) {
                // params.
                ConcurrentHashMap<String, Object> params = service.getParams();
                String coreHost = (String) params.get("core-host");
                String notaryHost = (String) params.get("notary-host");
                String registryStorageSize = String.valueOf(params.get("registry-storage-size"));
                String chartmuseumStorageSize = String.valueOf(params.get("chartmuseum-storage-size"));
                String jobserviceStorageSize = String.valueOf(params.get("jobservice-storage-size"));
                String databaseStorageSize = String.valueOf(params.get("database-storage-size"));
                String redisStorageSize = String.valueOf(params.get("redis-storage-size"));
                String trivyStorageSize = String.valueOf(params.get("trivy-storage-size"));
                String s3Bucket = (String) params.get("s3-bucket");
                String s3AccessKey = (String) params.get("s3-access-key");
                String s3SecretKey = (String) params.get("s3-secret-key");
                String s3Endpoint = (String) params.get("s3-endpoint");

                // extra params.
                ConcurrentHashMap<String, Object> extraParams = service.getExtraParams();
                String storageClass = (String) extraParams.get("storage-class");

                // show external ip of ingress controller nginx to register ingress host with the external ip of it
                // to public dns server.
                if(dependsOnIngressController) {
                    ret = CommandUtils.makeSureIngressHostRegistered(
                            configProps,
                            clusterId,
                            Arrays.asList(coreHost, notaryHost),
                            cnsl);
                    if(ret != 0) {
                        throw new RuntimeException("error with registering ingress host.");
                    }
                }

                System.out.println("creating private registry...");
                ret = CommandUtils.createPrivateRegistry(
                        configProps,
                        projectId,
                        clusterId,
                        coreHost,
                        notaryHost,
                        storageClass,
                        Integer.valueOf(registryStorageSize),
                        Integer.valueOf(chartmuseumStorageSize),
                        Integer.valueOf(jobserviceStorageSize),
                        Integer.valueOf(databaseStorageSize),
                        Integer.valueOf(redisStorageSize),
                        Integer.valueOf(trivyStorageSize),
                        s3Bucket,
                        s3AccessKey,
                        s3SecretKey,
                        s3Endpoint);
                if(ret != 0) {
                    throw new RuntimeException("error with creating private registry.");
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_QUERY_ENGINE)) {

                // params.
                ConcurrentHashMap<String, Object> params = service.getParams();
                String s3Bucket = (String) params.get("s3-bucket");
                String s3AccessKey = (String) params.get("s3-access-key");
                String s3SecretKey = (String) params.get("s3-secret-key");
                String s3Endpoint = (String) params.get("s3-endpoint");
                String sparkThriftServerExecutors = String.valueOf(params.get("spark-thrift-server-executors"));
                String sparkThriftServerExecutorMemory = String.valueOf(params.get("spark-thrift-server-executor-memory"));
                String sparkThriftServerExecutorCores = String.valueOf(params.get("spark-thrift-server-executor-cores"));
                String sparkThriftServerDriverMemory = String.valueOf(params.get("spark-thrift-server-driver-memory"));
                String trinoWorkers = String.valueOf(params.get("trino-workers"));
                String trinoServerMaxMemory = String.valueOf(params.get("trino-server-max-memory"));
                String trinoCores = String.valueOf(params.get("trino-cores"));
                String trinoTempDataStorage = String.valueOf(params.get("trino-temp-data-storage"));
                String trinoDataStorage = String.valueOf(params.get("trino-data-storage"));


                // extra params.
                ConcurrentHashMap<String, Object> extraParams = service.getExtraParams();
                String sparkThriftServerStorageClass = (String) extraParams.get("spark-thrift-server-storage-class");
                String trinoStorageClass = (String) extraParams.get("trino-storage-class");

                // show external ip of ingress controller nginx to register ingress host with the external ip of it
                // to public dns server.
                if(dependsOnIngressController) {
                    ret = CommandUtils.makeSureIngressHostRegistered(configProps, clusterId, null, cnsl);
                    if(ret != 0) {
                        throw new RuntimeException("error with registering ingress host.");
                    }
                }

                System.out.println("creating query engine...");
                ret = CommandUtils.createQueryEngine(
                        configProps,
                        projectId,
                        clusterId,
                        s3Bucket,
                        s3AccessKey,
                        s3SecretKey,
                        s3Endpoint,
                        sparkThriftServerStorageClass,
                        Integer.valueOf(sparkThriftServerExecutors),
                        Integer.valueOf(sparkThriftServerExecutorMemory),
                        Integer.valueOf(sparkThriftServerExecutorCores),
                        Integer.valueOf(sparkThriftServerDriverMemory),
                        Integer.valueOf(trinoWorkers),
                        Integer.valueOf(trinoServerMaxMemory),
                        Integer.valueOf(trinoCores),
                        Integer.valueOf(trinoTempDataStorage),
                        Integer.valueOf(trinoDataStorage),
                        trinoStorageClass);
                if(ret != 0) {
                    throw new RuntimeException("error with creating query engine.");
                }

            } else if(serviceName.equals(CLIConstants.SERVICE_STREAMING)) {

                // params.
                ConcurrentHashMap<String, Object> params = service.getParams();
                String kafkaReplicaCount = String.valueOf(params.get("kafka-replica-count"));
                String kafkaStorageSize = String.valueOf(params.get("kafka-storage-size"));
                String zkReplicaCount = String.valueOf(params.get("zk-replica-count"));

                // extra params.
                ConcurrentHashMap<String, Object> extraParams = service.getExtraParams();
                String storageClass = (String) extraParams.get("storage-class");

                // show external ip of ingress controller nginx to register ingress host with the external ip of it
                // to public dns server.
                if(dependsOnIngressController) {
                    ret = CommandUtils.makeSureIngressHostRegistered(configProps, clusterId, null, cnsl);
                    if(ret != 0) {
                        throw new RuntimeException("error with registering ingress host.");
                    }
                }

                System.out.println("creating streaming...");
                ret = CommandUtils.createStreaming(
                        configProps,
                        projectId,
                        clusterId,
                        Integer.valueOf(kafkaReplicaCount),
                        Integer.valueOf(kafkaStorageSize),
                        storageClass,
                        Integer.valueOf(zkReplicaCount));
                if(ret != 0) {
                    throw new RuntimeException("error with creating streaming.");
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_WORKFLOW)) {
                // params.
                ConcurrentHashMap<String, Object> params = service.getParams();
                String s3Bucket = (String) params.get("s3-bucket");
                String s3AccessKey = (String) params.get("s3-access-key");
                String s3SecretKey = (String) params.get("s3-secret-key");
                String s3Endpoint = (String) params.get("s3-endpoint");
                String storageSize = String.valueOf(params.get("storage-size"));

                // extra params.
                ConcurrentHashMap<String, Object> extraParams = service.getExtraParams();
                String storageClass = (String) extraParams.get("storage-class");

                // show external ip of ingress controller nginx to register ingress host with the external ip of it
                // to public dns server.
                if(dependsOnIngressController) {
                    ret = CommandUtils.makeSureIngressHostRegistered(configProps, clusterId, null, cnsl);
                    if(ret != 0) {
                        throw new RuntimeException("error with registering ingress host.");
                    }
                }

                System.out.println("creating workflow...");
                ret = CommandUtils.createWorkflow(
                        configProps,
                        projectId,
                        clusterId,
                        storageClass,
                        Integer.valueOf(storageSize),
                        s3Bucket,
                        s3AccessKey,
                        s3SecretKey,
                        s3Endpoint);
                if(ret != 0) {
                    throw new RuntimeException("error with creating workflow.");
                }
            }
        }

        System.out.println("all the services in blueprint created successfully.");

        return 0;
    }
}
