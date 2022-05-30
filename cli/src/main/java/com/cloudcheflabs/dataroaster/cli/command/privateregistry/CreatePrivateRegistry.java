package com.cloudcheflabs.dataroaster.cli.command.privateregistry;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Private Registry.")
public class CreatePrivateRegistry implements Callable<Integer> {

    @CommandLine.ParentCommand
    private PrivateRegistry parent;

    @CommandLine.Option(names = {"--core-host"}, description = "Ingress Core Host Name", required = true)
    private String coreHost;

    @CommandLine.Option(names = {"--notary-host"}, description = "Ingress Notary Host Name", required = true)
    private String notaryHost;

    @CommandLine.Option(names = {"--registry-storage-size"}, description = "Registry Storage Size in GiB", required = true)
    private int registryStorageSize;

    @CommandLine.Option(names = {"--chartmuseum-storage-size"}, description = "Chartmuseum Storage Size in GiB", required = true)
    private int chartmuseumStorageSize;

    @CommandLine.Option(names = {"--jobservice-storage-size"}, description = "Jobservice Storage Size in GiB", required = true)
    private int jobserviceStorageSize;

    @CommandLine.Option(names = {"--database-storage-size"}, description = "Database Storage Size in GiB", required = true)
    private int databaseStorageSize;

    @CommandLine.Option(names = {"--redis-storage-size"}, description = "Redis Storage Size in GiB", required = true)
    private int redisStorageSize;

    @CommandLine.Option(names = {"--trivy-storage-size"}, description = "Trivy Storage Size in GiB", required = true)
    private int trivyStorageSize;

    @CommandLine.Option(names = {"--s3-bucket"}, description = "S3 Bucket Name", required = true)
    private String s3Bucket;

    @CommandLine.Option(names = {"--s3-access-key"}, description = "S3 Access Key", required = true)
    private String s3AccessKey;

    @CommandLine.Option(names = {"--s3-secret-key"}, description = "S3 Secret Key", required = true)
    private String s3SecretKey;

    @CommandLine.Option(names = {"--s3-endpoint"}, description = "S3 Endpoint URL", required = true)
    private String s3Endpoint;


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

        String storageClass = CommandUtils.getStorageClassByPrompt(cnsl);
       
        System.out.printf("\n");

        // create.
        return CommandUtils.createPrivateRegistry(
                configProps,
                projectId,
                clusterId,
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

    }
}
