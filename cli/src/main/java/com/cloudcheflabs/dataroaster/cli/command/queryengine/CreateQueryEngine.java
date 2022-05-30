package com.cloudcheflabs.dataroaster.cli.command.queryengine;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Data Catalog.")
public class CreateQueryEngine implements Callable<Integer> {

    @CommandLine.ParentCommand
    private QueryEngine parent;

    @CommandLine.Option(names = {"--s3-bucket"}, description = "S3 Bucket Name", required = true)
    private String s3Bucket;

    @CommandLine.Option(names = {"--s3-access-key"}, description = "S3 Access Key", required = true)
    private String s3AccessKey;

    @CommandLine.Option(names = {"--s3-secret-key"}, description = "S3 Secret Key", required = true)
    private String s3SecretKey;

    @CommandLine.Option(names = {"--s3-endpoint"}, description = "S3 Endpoint", required = true)
    private String s3Endpoint;

    @CommandLine.Option(names = {"--spark-thrift-server-executors"}, description = "Spark Thrift Server Executor Count", required = true)
    private int sparkThriftServerExecutors;

    @CommandLine.Option(names = {"--spark-thrift-server-executor-memory"}, description = "Spark Thrift Server Executor Memory in GB", required = true)
    private int sparkThriftServerExecutorMemory;

    @CommandLine.Option(names = {"--spark-thrift-server-executor-cores"}, description = "Spark Thrift Server Executor Core Count", required = true)
    private int sparkThriftServerExecutorCores;

    @CommandLine.Option(names = {"--spark-thrift-server-driver-memory"}, description = "Spark Thrift Server Driver Memory in GB", required = true)
    private int sparkThriftServerDriverMemory;

    @CommandLine.Option(names = {"--trino-workers"}, description = "Trino Worker Count", required = true)
    private int trinoWorkers;

    @CommandLine.Option(names = {"--trino-server-max-memory"}, description = "Trino Server Max. Memory in GB", required = true)
    private int trinoServerMaxMemory;

    @CommandLine.Option(names = {"--trino-cores"}, description = "Trino Server Core Count", required = true)
    private int trinoCores;

    @CommandLine.Option(names = {"--trino-temp-data-storage"}, description = "Trino Temporary Data Storage in GB", required = true)
    private int trinoTempDataStorage;

    @CommandLine.Option(names = {"--trino-data-storage"}, description = "Trino Data Storage in GiB", required = true)
    private int trinoDataStorage;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        java.io.Console cnsl = System.console();
        if (cnsl == null) {
            System.out.println("No console available");
            return -1;
        }

        // show project list.
        CommandUtils.showProjectList(configProps);

        String projectId = CommandUtils.getProjectIdByPrompt(cnsl);
     
        System.out.printf("\n");


        // show cluster list.
        CommandUtils.showClusterList(configProps);

        System.out.printf("\n");

        String clusterId = CommandUtils.getClusterIdByPrompt(cnsl);
      
        System.out.printf("\n");

        // show storage classes.
        CommandUtils.showStorageClasses(configProps, clusterId);

        System.out.printf("\n");

        String sparkThriftServerStorageClass = cnsl.readLine("Select Storage Class for Spark Thrift Server(for instance, nfs) : ");
        while(sparkThriftServerStorageClass.equals("")) {
            System.err.println("spark thrift server storage class is required!");
            sparkThriftServerStorageClass = cnsl.readLine("Select Storage Class for Spark Thrift Server(for instance, nfs) : ");
            if(!sparkThriftServerStorageClass.equals("")) {
                break;
            }
        }
       

        System.out.printf("\n");


        CommandUtils.showStorageClasses(configProps, clusterId);

        System.out.printf("\n");

        String trinoStorageClass = cnsl.readLine("Select Storage Class for Trino : ");
        while(trinoStorageClass.equals("")) {
            System.err.println("trino storage class is required!");
            trinoStorageClass = cnsl.readLine("Select Storage Class for Trino : ");
            if(!trinoStorageClass.equals("")) {
                break;
            }
        }
     
        System.out.printf("\n");

        // create.
        return CommandUtils.createQueryEngine(
                configProps,
                projectId,
                clusterId,
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
    }
}
