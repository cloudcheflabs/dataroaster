package com.cloudcheflabs.dataroaster.cli.command.metricsmonitoring;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Metrics Monitoring.")
public class CreateMetricsMonitoring implements Callable<Integer> {

    @CommandLine.ParentCommand
    private MetricsMonitoring parent;

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

        String storageSize = cnsl.readLine("Enter Storage Size in GiB : ");
        while(storageSize.equals("")) {
            System.err.println("storage size is required!");
            storageSize = cnsl.readLine("Enter Storage Size in GiB : ");
            if(!storageSize.equals("")) {
                break;
            }
        }
       
        System.out.printf("\n");

        // create.
        return CommandUtils.createMetricsMonitoring(
                configProps,
                projectId,
                clusterId,
                storageClass,
                storageSize);
    }
}
