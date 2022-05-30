package com.cloudcheflabs.dataroaster.cli.command.distributedtracing;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.CLIConstants;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = CLIConstants.SERVICE_DISTRIBUTED_TRACING,
        subcommands = {
                CreateDistributedTracing.class,
                DeleteDistributedTracing.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Distributed Tracing Service.")
public class DistributedTracing implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
