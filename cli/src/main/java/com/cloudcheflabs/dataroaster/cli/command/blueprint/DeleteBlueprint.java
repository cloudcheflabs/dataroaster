package com.cloudcheflabs.dataroaster.cli.command.blueprint;

import com.cloudcheflabs.dataroaster.cli.api.dao.ServicesDao;
import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.*;
import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "delete",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Delete Blueprint Deployment.")
public class DeleteBlueprint implements Callable<Integer> {

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

        String projectName = project.getName();
        String clusterName = cluster.getName();

        System.out.println("\n");
        System.out.printf("All the services defined in blueprint will be deleted in project [%s] and cluster [%s]...\n", projectName, clusterName);
        System.out.println("\n");
        String yN = cnsl.readLine("Continue(y/N) : ");
        while(yN.equals("")) {
            System.err.println("y/N ?");
            yN = cnsl.readLine("Continue(y/N) : ");
            if(!yN.equals("")) {
                break;
            }
        }

        if(yN.toLowerCase().equals("n") || yN.toLowerCase().equals("no")) {
            System.err.println("blueprint deletion cancelled...");
            return -1;
        } else if(yN.toLowerCase().equals("y") || yN.toLowerCase().equals("yes")) {
            System.out.println("ok...");
        }

        // show services list.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ServicesDao servicesDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = servicesDao.listServices(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> servicesList =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        int ret = -1;

        for(BlueprintGraph.Service service : serviceDependencyList) {
            String serviceName = service.getName();
            String depends = service.getDepends();
            boolean dependsOnIngressController = (depends != null) ? depends.equals(CLIConstants.SERVICE_INGRESS_CONTROLLER) : false;
            // ingress controller.
            if(serviceName.equals(CLIConstants.SERVICE_INGRESS_CONTROLLER)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.INGRESS_CONTROLLER.name());

                    System.out.println("deleting ingress controller...");
                    ret = CommandUtils.deleteIngressController(
                            configProps,
                            serviceId);
                } catch (Exception e) {
                   System.err.printf("error: %s\n", e.getMessage());
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_BACKUP)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.BACKUP.name());

                    System.out.println("deleting backup...");
                    ret = CommandUtils.deleteBackup(
                            configProps,
                            serviceId);
                } catch (Exception e) {
                    System.err.printf("error: %s\n", e.getMessage());
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_ANALYTICS)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.ANALYTICS.name());

                    System.out.println("deleting analytics...");
                    ret = CommandUtils.deleteAnalytics(configProps, serviceId);
                } catch (Exception e) {
                    System.err.printf("error: %s\n", e.getMessage());
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_CICD)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.CI_CD.name());

                    System.out.println("deleting cicd...");
                    ret = CommandUtils.deleteCiCd(configProps, serviceId);
                } catch (Exception e) {
                    System.err.printf("error: %s\n", e.getMessage());
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_DATA_CATALOG)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.DATA_CATALOG.name());

                    System.out.println("deleting data catalog...");
                    ret = CommandUtils.deleteDataCatalog(configProps, serviceId);
                } catch (Exception e) {
                    System.err.printf("error: %s\n", e.getMessage());
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_DISTRIBUTED_TRACING)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.DISTRIBUTED_TRACING.name());

                    System.out.println("deleting distributed tracing...");
                    ret = CommandUtils.deleteDistributedTracing(configProps, serviceId);
                } catch (Exception e) {
                    System.err.printf("error: %s\n", e.getMessage());
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_METRICS_MONITORING)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.METRICS_MONITORING.name());

                    System.out.println("deleting metrics monitoring...");
                    ret = CommandUtils.deleteMetricsMonitoring(configProps, serviceId);
                } catch (Exception e) {
                    System.err.printf("error: %s\n", e.getMessage());
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_POD_LOG_MONITORING)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.POD_LOG_MONITORING.name());

                    System.out.println("deleting pod log monitoring...");
                    ret = CommandUtils.deletePodLogMonitoring(configProps, serviceId);
                } catch (Exception e) {
                    System.err.printf("error: %s\n", e.getMessage());
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_PRIVATE_REGISTRY)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.PRIVATE_REGISTRY.name());

                    System.out.println("deleting private registry...");
                    ret = CommandUtils.deletePrivateRegistry(configProps, serviceId);
                } catch (Exception e) {
                    System.err.printf("error: %s\n", e.getMessage());
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_QUERY_ENGINE)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.QUERY_ENGINE.name());

                    System.out.println("deleting query engine...");
                    ret = CommandUtils.deleteQueryEngine(configProps, serviceId);
                } catch (Exception e) {
                    System.err.printf("error: %s\n", e.getMessage());
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_STREAMING)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.STREAMING.name());

                    System.out.println("deleting streaming...");
                    ret = CommandUtils.deleteStreaming(configProps, serviceId);
                } catch (Exception e) {
                    System.err.printf("error: %s\n", e.getMessage());
                }
            } else if(serviceName.equals(CLIConstants.SERVICE_WORKFLOW)) {
                try {
                    String serviceId = CommandUtils.getServiceId(
                            servicesList,
                            projectName,
                            clusterName,
                            ServiceDef.ServiceTypeEnum.WORKFLOW.name());

                    System.out.println("deleting workflow...");
                    ret = CommandUtils.deleteWorkflow(configProps, serviceId);
                } catch (Exception e) {
                    System.err.printf("error: %s\n", e.getMessage());
                }
            }
        }

        try {
            // delete project.
            String projectId = CommandUtils.getProjectId(configProps, projectName);
            CommandUtils.deleteProject(configProps, projectId);

            // delete cluster.
            String clusterId = CommandUtils.getClusterId(configProps, clusterName);
            CommandUtils.deleteCluster(configProps, clusterId);
        } catch (Exception e) {
            System.err.printf("error: %s\n", e.getMessage());
        }

        System.out.println("all the services in blueprint deleted successfully.");

        return 0;
    }
}
