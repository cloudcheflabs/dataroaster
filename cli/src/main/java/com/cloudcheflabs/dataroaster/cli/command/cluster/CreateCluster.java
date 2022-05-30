package com.cloudcheflabs.dataroaster.cli.command.cluster;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Kubernetes Cluster Metadata.")
public class CreateCluster implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Cluster parent;

    @CommandLine.Option(names = {"--name"}, description = "Cluster Name", required = true)
    private String name;

    @CommandLine.Option(names = {"--description"}, description = "Cluster Description", required = true)
    private String description;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        // create kubernetes cluster.
        return CommandUtils.createCluster(configProps, name, description);
    }
}
