package com.cloudcheflabs.dataroaster.cli.command.streaming;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Streaming.")
public class CreateStreaming implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Streaming parent;

    @CommandLine.Option(names = {"--kafka-replica-count"}, description = "Kafka Replica Count", required = true)
    private int kafkaReplicaCount;

    @CommandLine.Option(names = {"--kafka-storage-size"}, description = "Kafka Storage Size in GiB", required = true)
    private int kafkaStorageSize;

    @CommandLine.Option(names = {"--zk-replica-count"}, description = "Zookeeper Replica Count", required = true)
    private int zkReplicaCount;

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
        return CommandUtils.createStreaming(
                configProps,
                projectId,
                clusterId,
                kafkaReplicaCount,
                kafkaStorageSize,
                storageClass,
                zkReplicaCount);
    }
}
