package com.cloudcheflabs.dataroaster.cli.command.datacatalog;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Data Catalog.")
public class CreateDataCatalog implements Callable<Integer> {

    @CommandLine.ParentCommand
    private DataCatalog parent;

    @CommandLine.Option(names = {"--s3-bucket"}, description = "S3 Bucket Name", required = true)
    private String s3Bucket;

    @CommandLine.Option(names = {"--s3-access-key"}, description = "S3 Access Key", required = true)
    private String s3AccessKey;

    @CommandLine.Option(names = {"--s3-secret-key"}, description = "S3 Secret Key", required = true)
    private String s3SecretKey;

    @CommandLine.Option(names = {"--s3-endpoint"}, description = "S3 Endpoint", required = true)
    private String s3Endpoint;

    @CommandLine.Option(names = {"--storage-size"}, description = "Storage Size in GiB", required = true)
    private int storageSize;

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
        return CommandUtils.createDataCatalog(
                configProps,
                projectId,
                clusterId,
                s3Bucket,
                s3AccessKey,
                s3SecretKey,
                s3Endpoint,
                storageClass,
                Integer.valueOf(storageSize));
    }
}
