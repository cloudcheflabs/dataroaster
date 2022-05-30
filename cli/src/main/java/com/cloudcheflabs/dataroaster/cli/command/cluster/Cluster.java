package com.cloudcheflabs.dataroaster.cli.command.cluster;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "cluster",
        subcommands = {
                CreateCluster.class,
                UpdateCluster.class,
                DeleteCluster.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Kubernetes Cluster Metadata.")
public class Cluster implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
